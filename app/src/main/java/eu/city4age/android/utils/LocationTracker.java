package eu.city4age.android.utils;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;

import java.util.Calendar;
import java.util.Date;

import java.util.Timer;
import java.util.TimerTask;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import eu.city4age.android.App;
import eu.city4age.android.R;
import eu.city4age.android.apiClient.RepositoryEngine;
import eu.city4age.android.model.AllRoutes;
import eu.city4age.android.model.POI;
import eu.city4age.android.model.Route;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationParams;

/**
 * Created by lgiampouras on 1/30/15.
 */
//Singleton class
public class LocationTracker{

    //Interface that will be used as a  poi tracker callback
    public interface RouteTrackerListener {
        //public void routeUpdated(Route route, float currentSpeed);
        public void onLocationChanged(Location location);
        public void onLocationTracked(Location location, double currentDistance, double averageSpeed, double maximumSpeed);
        public void onTrackingStarted();
        public void onTrackingStopped();
    }

    public RouteTrackerListener routeTrackerListener;
    public LocationManager locationManager;
    private LocationListener locationListener;
    private Context context;
    private Activity activity;

    public boolean isTracking = false;
    public Timer automaticStopEndOfDayTimer = null; //Route tracking stops automatically at 23:59:59
    public Timer automaticStopAfterPeriodTimer = null;  //Route tracking stops automatically after a period of time
    public Timer automaticStartTimer = null;  //Start route tracking again automatically
    public Handler stopTrackingTaskHandler;
    public Handler startTrackingTaskHandler;

    public AllRoutes routes = null;
    public Route route = null;         //Current route we're tracking
    public Route tmp_route = new Route();

    public Location lastVisitedLocation;

    private Location currentLocation;
    private double currentAltitude;
    private double currentLatitude;
    private double currentLongitude;
    public double currentSpeed;
    public double currentDistance;
    public double averageSpeed;
    public double maximumSpeed;
    private float currentBearing;
    //private Timestamp lastPOITracked = null;
    private Location lastTrackedLocation = null;

    private static final int TWO_MINUTES = 1000 * 60 * 2;
    public Location previousBestLocation = null;

    private static LocationTracker instance = null;

    protected LocationTracker() {
        //Just to cancel out the default constructor
    }

    public static LocationTracker getInstance() {
        if(instance == null) {
            Log.d("Cycle", "new LocationTracker");
            instance = new LocationTracker();
        }
        return instance;
    }

    //Compute if it's possible to walk/run/drive the distance in the duration
    public boolean isPossibleToCompleteDistanceInDuration(float distance, long duration){

        final int MOVEMENT_MAX_SPEED = App.getContext().getResources().getInteger(R.integer.MOVEMENT_MAX_SPEED);

        //Compute speed based on provided distance & time frame
        float speed = distance / duration;

        Log.d("GPS", "computed speed: " + speed);

        return speed <= MOVEMENT_MAX_SPEED;
    }

    public void startLocationTracker(Activity activity) {
        Log.d("GPS", "startLocationTracker");

        this.context = activity.getApplicationContext();
        this.activity = activity;

        SmartLocation.with(context).location().config(LocationParams.NAVIGATION).start(new OnLocationUpdatedListener() {
            @Override
            public void onLocationUpdated(Location location) {
                Log.d("GPS", "LIB: " + location.getLatitude() + ", " + location.getLongitude());
                trackLocation(location);
            }
        });

    }

    //Stop location tracker
    public void stopLocationTracker() {

        SmartLocation.with(context).location().stop();
    }


    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }


    public double getCurrentAltitude() {
        return currentAltitude;
    }


    public double getCurrentLatitude() {
        return currentLatitude;
    }


    public void setCurrentLatitude(double currentLatitude) {
        this.currentLatitude = currentLatitude;
    }


    public double getCurrentLongitude() {
        return currentLongitude;
    }


    public void setCurrentLongitude(double currentLongitude) {
        this.currentLongitude = currentLongitude;
    }

    public float getCurrentBearing() {
        return currentBearing;
    }

    public void setCurrentBearing(float currentBearing) {
        this.currentBearing = currentBearing;
    }

    //Stop route tracking automatically and save the route
    public void startAutomaticStopTimers(){

        automaticStopAfterPeriodTimer = new Timer();
        automaticStopEndOfDayTimer = new Timer();

        //Tonight at 23:59:59
        Date endOfDay = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(endOfDay);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 0);
        endOfDay = cal.getTime();
        //Log.d("Cycle", "Tonight: " + endOfDay.toString());

        final TimerTask startTrackingTask = new TimerTask() {

            @Override
            public void run() {
                startTrackingTaskHandler.obtainMessage().sendToTarget();
            }
        };
        TimerTask stopTrackingAfterPeriodTask = new TimerTask() {

            @Override
            public void run() {
                stopTrackingTaskHandler.obtainMessage().sendToTarget();
            }
        };
        TimerTask stopTrackingEndOfDayTask = new TimerTask() {

            @Override
            public void run() {
                stopTrackingTaskHandler.obtainMessage().sendToTarget();
            }
        };

        startTrackingTaskHandler = new Handler() {
            public void handleMessage(Message msg) {
                Log.d("Cycle", "Start automatically");

                //Start tracking
                LocationTracker.getInstance().startRouteTracking();
            }
        };
        stopTrackingTaskHandler = new Handler() {
            public void handleMessage(Message msg) {
                Log.d("Cycle", "Stop automatically");

                //Stop tracking
                LocationTracker.getInstance().stopRouteTracking();

                //Start it again
                automaticStartTimer = new Timer();
                automaticStartTimer.schedule(startTrackingTask, App.getContext().getResources().getInteger(R.integer.LOCATION_TRACKING_AUTO_START_AFTER) * 1000);
            }
        };

        //automaticStopAfterPeriodTimer.schedule(stopTrackingAfterPeriodTask, 60000);
        automaticStopAfterPeriodTimer.schedule(stopTrackingAfterPeriodTask, App.getContext().getResources().getInteger(R.integer.LOCATION_TRACKING_AUTO_STOP_AFTER) * 60 * 1000);
        automaticStopEndOfDayTimer.schedule(stopTrackingEndOfDayTask, endOfDay);
    }
    public void stopAutomaticStopTimers(){

        if(automaticStopAfterPeriodTimer != null)
            automaticStopAfterPeriodTimer.cancel();

        if(automaticStopEndOfDayTimer != null)
            automaticStopEndOfDayTimer.cancel();

    }
    public void stopAutomaticStartTimers(){
        if(automaticStartTimer != null)
            automaticStartTimer.cancel();
    }


    /**************************************/
    //Route tracking
    public void startRouteTracking() {
        if(activity != null)
            startRouteTracking(activity);
    }
    public void startRouteTracking(Activity activity) {
        Log.d("Cycle", "startRouteTracking");


        //Already tracking
        if(isTracking)
            return;

        if (getCurrentLatitude()==0 && getCurrentLongitude()==0)
        {
            Crouton.makeText(activity, activity.getText(R.string.nolocation), Style.INFO).show();
            isTracking= false;
        }
        else
        {

            Crouton.makeText(activity, activity.getText(R.string.routetrackingstarted), Style.ALERT).show();

            //Get route list object
            routes = Utils.loadRoutes(activity);

            //If nothing is saved yet create one
            if (routes==null) {

                routes = new AllRoutes();
            }

            //Start a new Route
            route = new Route();

            lastTrackedLocation = null;
            isTracking = true;

            //Stop timers
            stopAutomaticStartTimers();
            stopAutomaticStopTimers();

            //Stop route tracking automatically and save the route, after a period of time
            startAutomaticStopTimers();

        }

        if(isTracking && routeTrackerListener !=null)
            routeTrackerListener.onTrackingStarted();

    }

    public void stopRouteTracking(){
        if(activity != null)
            stopRouteTracking(activity);
    }
    public void stopRouteTracking(final Activity activity) {
        Log.d("Cycle", "stopRouteTracking");

        //Already stopped
        if(!isTracking)
            return;

        isTracking = false;

        //Stop timers
        stopAutomaticStartTimers();
        stopAutomaticStopTimers();

        if (routes!=null && route!=null && route.POIS.size() > 0) {

            //Set end location.
            //Since we track the route by intermediate locations' distances, we need to set an end location (with the most recent timestamp),
            //in case that the user was remained in the same location for a long time
            route.setEndLocation();

            route.setRouteDuration(Float.parseFloat(String.valueOf(route.POIS.get(route.POIS.size()-1).getTime())) -
                    Float.parseFloat(String.valueOf(route.POIS.get(0).getTime())));

            //Save route to server
            RepositoryEngine repEngine = new RepositoryEngine();
            repEngine.registerObservation(route, activity.getApplicationContext(), new RepositoryEngine.Listener() {
                @Override
                public void onComplete(RepositoryEngine.Message message) {

                    if(message == RepositoryEngine.Message.SUCCESS){
                        route.savedOnServer = true;
                        Crouton.makeText(activity, activity.getText(R.string.routeSavedOnServerAndLocally), Style.CONFIRM).show();
                    }
                    else if(route != null){
                        route.savedOnServer = false;
                        Crouton.makeText(activity, activity.getText(R.string.routeSavedOnlyLocally), Style.ALERT).show();
                    }

                    Log.d("Route", "save route: " + new Gson().toJson(route));

                    //Add the route we tracked
                    routes.allRoutes.add(route);

                    //Save again all routes
                    Utils.saveRoutes(routes, activity);

                    //Set route to null
                    route = null;

                }
            });
        }
        else {

            Crouton.makeText(activity, activity.getText(R.string.routessavingfailed), Style.ALERT).show();
        }

        if(routeTrackerListener !=null)
            routeTrackerListener.onTrackingStopped();

    }

    public void trackLocation(Location location) {

        if (!isTracking || route == null)
            return;

        final int LOCATION_TRACKING_MIN_STEP = App.getContext().getResources().getInteger(R.integer.LOCATION_TRACKING_MIN_STEP);
        boolean shouldTrackIt = false;


//        if(!isBetterLocation(location, previousBestLocation))
//            return;

        previousBestLocation = location;
        currentLocation = location;
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
        currentAltitude = location.getAltitude();
        currentBearing = location.getBearing();

        //First location
        if(lastTrackedLocation == null){
            shouldTrackIt = true;
        }
        //Intermediate location
        else{

            float distanceMoved = location.distanceTo(lastTrackedLocation);
            long duration = (location.getTime() - lastTrackedLocation.getTime()) / 1000; //seconds

            Log.d("GPS", "accuracy: " + location.getAccuracy());
            Log.d("GPS", "distance: " + distanceMoved);
            Log.d("GPS", "duration: " + duration);

            //Track route based on space intervals
            //if(distanceMoved > LOCATION_TRACKING_MIN_STEP && isPossibleToCompleteDistanceInDuration(distanceMoved, duration))
                if(distanceMoved > LOCATION_TRACKING_MIN_STEP)
                shouldTrackIt = true;
        }

        if(shouldTrackIt){

            Log.d("GPS", "Track POI");

            POI poi = new POI();

            //Set poi data based on location
            poi.setLat(location.getLatitude());
            poi.setLng(location.getLongitude());

            poi.setSpeed(location.getSpeed());
            currentSpeed = location.getSpeed();

            Date date = new Date(location.getTime());
            poi.setTimestamp(Utils.dateToString(date));
            poi.setTime(location.getTime());

            //Add poi to route
            route.getPOIS().add(poi);

            //Get distance from previous location
            if (lastVisitedLocation!=null) {

                float howFar = location.distanceTo(lastVisitedLocation);
                route.distanceCovered += howFar;                 //(meters)
                currentDistance = route.distanceCovered;
            }

            //Refresh last visited location
            lastVisitedLocation = location;

            //Average speed
            float aSpeed = 0;
            for (POI samplePoi : route.POIS) {
                aSpeed += samplePoi.getSpeed();
            }
            aSpeed = aSpeed/route.POIS.size();               //(m/s)
            averageSpeed = aSpeed;

            if (aSpeed>=0)
                route.setAverageSpeed(aSpeed);
            else
                route.setAverageSpeed(0);

            //Current speed
            float cSpeed = 0;

            if (location.getSpeed()>=0)
                cSpeed = location.getSpeed();                //(m/s)
            else
                cSpeed = 0;

            //Minimun speed
            float minSpeed = 0;
            for (POI samplePoi : route.POIS) {
                if (minSpeed > samplePoi.getSpeed())
                    minSpeed = samplePoi.getSpeed();         //(m/s)
            }

            //Maximum speed
            float maxSpeed = 0;
            for (POI samplePoi : route.POIS) {
                if (maxSpeed < samplePoi.getSpeed())
                    maxSpeed = samplePoi.getSpeed();        //(m/s)
            }
            maximumSpeed = maxSpeed;

            if (maxSpeed>=0)
                route.setMaximumSpeed(maxSpeed);
            else
                route.setMaximumSpeed(0);

            //Log it
            Log.d("Route", "POI: "+ poi.getLat() + ", " + poi.getLng() + ", " + poi.getTimestamp());

            //Notify listener that location tracked
            if (routeTrackerListener !=null)
                routeTrackerListener.onLocationTracked(location, currentDistance, averageSpeed, maximumSpeed);


            lastTrackedLocation = location;


        }else{

            //Notify listener that location changed
            if (routeTrackerListener != null)
                routeTrackerListener.onLocationChanged(location);

        }

    }
    /**************************************/

}