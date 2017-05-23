package eu.city4age.android.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import android.os.Handler;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import eu.city4age.android.AboutActivity;
import eu.city4age.android.App;
import eu.city4age.android.MainMapActivity;
import eu.city4age.android.R;
import eu.city4age.android.RouteListActivity;
import eu.city4age.android.SlidingUpPanelLayout;
import eu.city4age.android.apiClient.CycleSafeApiClient;
import eu.city4age.android.apiClient.GMapV2Direction;
import eu.city4age.android.apiClient.WeatherEngine;
import eu.city4age.android.model.CurrentWeather;
import eu.city4age.android.model.POI;
import eu.city4age.android.model.Route;
import eu.city4age.android.services.LocationTrackerService;
import eu.city4age.android.utils.LocationTracker;
import eu.city4age.android.utils.Log;
import eu.city4age.android.utils.Utils;

/**
 * Created by lgiampouras on 1/29/15.
 */
public class Map extends Fragment implements LocationTracker.RouteTrackerListener, WeatherEngine.WeatherEngineListener {

    private LocationTracker locationTracker = null;
    private MapView mapView;
    private GoogleMap googleMap;
    private Bundle mBundle;
    private Polyline polyline;
    private GMapV2Direction md;

    Runnable timerThread;
    Handler handler;

    //Point for line drawing
    private ArrayList<LatLng> directionPoints = new ArrayList<LatLng>();
    //The actual polyline
    Polyline mapPolyline;

    private boolean loadedforfirsTime = true;

    private MenuItem playMenuBtn = null;
    private MenuItem directionsMenuBtn = null;

    //Markers array, where markers will be saved
    private List<Marker> markers = new ArrayList<Marker>();

    private SlidingUpPanelLayout mLayout;
    private FrameLayout fLayout;
    private TableLayout tLayout;

    private TextView currentDistanceView;
    private TextView currentAverageView;
    private TextView currentSpeedView;
    private TextView currentMaxSpeedView;
    private TextView timer;
    private long startTime = 0L;

    private TextView thermometerView;
    private TextView windView;
    private TextView dropletsView;
    private TextView compassView;
    private TextView pressureView;
    private final DecimalFormat newFormat = new DecimalFormat("###.##");
    private double temp;
    private double humidity;
    private double pressure;
    private double windspeed;
    private double winddirection;

    private ServiceConnection locationTrackerServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName cn, IBinder ib) {
            Log.d("Cycle", "onServiceConnected");

            LocationTrackerService service = ((LocationTrackerService.MyBinder) ib).getServiceSystem();
            locationTracker = service.locationTracker;

            //Set the delegate for callback
            try {
                locationTracker.routeTrackerListener = Map.this;
            }
            catch (ClassCastException e) {
                throw new ClassCastException(this.toString() + " must implement LocationTracker.RouteTrackerListener");
            }

            //Start location tracker
            locationTracker.startLocationTracker(getActivity());
        }

        public void onServiceDisconnected(ComponentName cn) {

        }
    };


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d("GPS", "onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Cycle", "onCreate");

        //Enable menu options from this fragment
        setHasOptionsMenu(true);

        //Bind fragment with the LocationTrackerService
        Intent intent = new Intent(getActivity(), LocationTrackerService.class);
        getActivity().bindService(intent, locationTrackerServiceConnection, App.getContext().BIND_AUTO_CREATE);

    }


    @Override
    //Called when the UI is ready to draw the Fragment
    //this method must return a View that is the root of your fragment's layout.
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        Log.d("Map Fragment:", "Fragment view ready to be drawn");

        View inflatedView = inflater.inflate(R.layout.map_fragment, container, false);

        try
        {
            MapsInitializer.initialize(getActivity());
        }
        finally
        {
            //Do nothing
        }

        mapView = (MapView) inflatedView.findViewById(R.id.map);
        mapView.onCreate(mBundle);

        if (googleMap == null) {
            googleMap = ((MapView) inflatedView.findViewById(R.id.map)).getMap();
        }

        tLayout = (TableLayout) inflatedView.findViewById(R.id.tableLayout);

        currentDistanceView = (TextView) inflatedView.findViewById(R.id.current_distance);
        currentAverageView = (TextView) inflatedView.findViewById(R.id.current_average);
        currentSpeedView = (TextView) inflatedView.findViewById(R.id.current_speed);
        currentMaxSpeedView = (TextView) inflatedView.findViewById(R.id.current_max_speed);
        timer = (TextView) inflatedView.findViewById(R.id.timer);

        thermometerView = (TextView) inflatedView.findViewById(R.id.thermometerView);
        windView = (TextView) inflatedView.findViewById(R.id.windView);
        dropletsView = (TextView) inflatedView.findViewById(R.id.humidityView);
        compassView = (TextView) inflatedView.findViewById(R.id.compassView);
        pressureView = (TextView) inflatedView.findViewById(R.id.pressureView);

        fLayout = (FrameLayout) inflatedView.findViewById(R.id.frame_layout);
        mLayout = (SlidingUpPanelLayout) inflatedView.findViewById(R.id.sliding_layout);
        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        mLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                //Log.i(TAG, "onPanelSlide, offset " + slideOffset);
            }
            @Override
            public void onPanelExpanded(View panel) {
                //Log.i(TAG, "onPanelExpanded");
            }
            @Override
            public void onPanelCollapsed(View panel) {
                //Log.i(TAG, "onPanelCollapsed");
            }
            @Override
            public void onPanelAnchored(View panel) {
                //Log.i(TAG, "onPanelAnchored");
            }
            @Override
            public void onPanelHidden(View panel) {
                //Log.i(TAG, "onPanelHidden");
            }
        });

        return inflatedView;
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d("Cycle", "onResume");

        mapView.onResume();

        setUpMap();

    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d("Cycle", "onStart");

    }

    @Override
    public void onPause() {
        super.onPause();

        mapView.onPause();
    }


    private void setUpMap() {

        //Show current location
        googleMap.setMyLocationEnabled(true);

        //Google maps zoom buttons
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        //Zoom gestures
        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        //Set a My Location button on Google Maps
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        //Rotate gestures
        googleMap.getUiSettings().setRotateGesturesEnabled(true);

        //Compass
        googleMap.getUiSettings().setCompassEnabled(true);

        //Map type
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //googleMap.setOnMapLongClickListener(this);

        //Help GPS find your location
        googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

            @Override
            public void onMyLocationChange(Location loc) {

                Log.d("GPS", "GOO: " + loc.getLatitude() + ", " + loc.getLongitude());

                /************************************/
                //Help GPS with location from the map
                locationTracker.setCurrentLocation(loc);
                locationTracker.setCurrentLatitude(loc.getLatitude());
                locationTracker.setCurrentLongitude(loc.getLongitude());
                locationTracker.setCurrentBearing(loc.getBearing());
                /************************************/

                if (loadedforfirsTime) {

                    loadedforfirsTime = false;

                    CameraPosition currentPlace = new CameraPosition.Builder().target(new LatLng(loc.getLatitude(), loc.getLongitude())).zoom(17f).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace));

                    //Get API key for service calls
                    //RepositoryEngine repEngine = new RepositoryEngine();
                    //repEngine.loginWithCredentials("sdr", "passsantander", getActivity());
                }


                //if (LocationTracker.getInstance().isTracking) {

                    //CameraPosition currentPlace = new CameraPosition.Builder().target(new LatLng(loc.getLatitude(), loc.getLongitude())).zoom(17f).build();
                    //googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace));

                    //Draw line while moving
                    //drawNavigationLine(loc);

                    //Track POI from here as well in case GPS is not active yet. (Use maps tracking along with native gps tracking)
                    //Tracking check is done inside
                    //LocationTracker.getInstance().trackLocation(loc);
                //}
            }
        });


        //Populate map with markers
//        addMarkersToMap();
    }



//    @Override
//    public void onMapLongClick(LatLng point) {
//
//        if (null != polyline)
//        {
//            polyline.remove();
//        }
//
//        md = new GMapV2Direction();
//
//        Document doc = md.getDocument(new LatLng(LocationTracker.getInstance().getCurrentLocation().getLatitude(),
//                        LocationTracker.getInstance().getCurrentLocation().getLongitude()), point,
//                GMapV2Direction.MODE_DRIVING);
//
//        ArrayList<LatLng> directionPoint = md.getDirection(doc);
//        PolylineOptions rectLine = new PolylineOptions().width(5).color(
//                Color.BLUE);
//
//        for (int i = 0; i < directionPoint.size(); i++) {
//            rectLine.add(directionPoint.get(i));
//        }
//        polyline = googleMap.addPolyline(rectLine);
//        directionsMenuBtn.setVisible(true);
//    }

    //Call back from LocationTracker


    @Override
    public void onTrackingStarted() {
        switchToTrackingView();
    }

    @Override
    public void onTrackingStopped() {
        switchToNormalView();
    }

    @Override
    public void onLocationChanged(Location location) {
        drawNavigationStatus(null, location.getSpeed(), null, null);
    }

    @Override
    public void onLocationTracked(Location location, double currentDistance, double averageSpeed, double maximumSpeed) {
        drawNavigationLine(location);
        drawNavigationStatus(currentDistance, location.getSpeed(), averageSpeed, maximumSpeed);
    }

    //weather
    public void loadWeather(Boolean showResult) {

        Location currentLocation = locationTracker.getCurrentLocation();

        WeatherEngine weatherEngine = new WeatherEngine();
        weatherEngine.weatherEngineListener = this;
        weatherEngine.loadCurrentWeather(locationTracker.getCurrentLocation(), showResult);
    }


    //Call back from WeatherEngineListener
    //Updates when new weather information is received
    public void weatherUpdated(CurrentWeather weather, Boolean success, Boolean showResult) {

        if (success) {

            //Add weather info to route if it's tracking the user
            if (locationTracker.route!=null) {
                locationTracker.route.setWeather(weather);
            }

            //Service returns from a different thread. A handler is needed for UI information
            if (showResult) {
                weathersHandler.sendEmptyMessage(Utils.SUCCESS);
            }
        }
        else {

            if (showResult) {
                weathersHandler.sendEmptyMessage(Utils.FAILURE);
            }
            else {
                //If it fails give it another try if it's for tracking purposes
                loadWeather(showResult);
            }
        }

    }



    final Handler weathersHandler = new Handler() {
        public void handleMessage(Message msg) {

            switch (msg.what) {

                case Utils.SUCCESS:

                    break;

                case Utils.FAILURE:

                    break;

                default:
                    break;
            }
        }
    };
    /****************/




    /****************/
    //Markers functions.
    private void addMarkersToMap() {

        //Marker to be added to the map
        Marker marker;

        BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);

//        marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(record.getLatitude(), record.getLongitude())).title(record.getUsername()).snippet(record.getProduct().getName()).icon(icon));
//        markers.add(marker);
    }



    //Fit map to pins
    public void fitMapToPins() {

        if (mapView.getViewTreeObserver().isAlive()) {

            mapView.getViewTreeObserver().addOnGlobalLayoutListener(
                    new OnGlobalLayoutListener() {

                        @SuppressWarnings("deprecation")
                        @SuppressLint("NewApi")

                        // We check which build version we are using.
                        @Override
                        public void onGlobalLayout() {

                            LatLngBounds.Builder bc = new LatLngBounds.Builder();

                            for (Marker item : markers) {
                                bc.include(item.getPosition());
                            }

                            if (markers.size() > 0) {
                                LatLngBounds bounds = bc.build();

                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                                    mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                                }
                                else {
                                    mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                }

                                if (googleMap != null)
                                    googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                            }
                        }
                    });
        }
    }
    /****************/

    //Draw navigation line
    private void drawNavigationLine(Route route){

        if(route == null)
            return;

        for(POI poi : route.getPOIS()){
            LatLng latLng = new LatLng(poi.getLat(), poi.getLng());
            drawNavigationLine(latLng);
        }
    }
    private void drawNavigationLine(Location loc) {

        LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
        drawNavigationLine(latLng);
    }
    private void drawNavigationLine(LatLng latLng) {
        Log.d("Cycle", "drawNavigationLine");

        //Animate camera
        CameraPosition currentPlace = new CameraPosition.Builder().target(latLng).zoom(17f).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace));

        //Add the new location
        directionPoints.add(latLng);

        PolylineOptions rectLine = new PolylineOptions().width(5).color(Color.RED);

        for(int i = 0 ; i < directionPoints.size() ; i++) {
            rectLine.add(directionPoints.get(i));
        }

        mapPolyline = googleMap.addPolyline(rectLine);
    }

    //Draw navigation status
    private void drawNavigationStatus(Double currentDistance, Float currentSpeed, Double averageSpeed, Double maximumSpeed){
        //Log.d("Cycle", "drawNavigationStatus");

        if(currentDistance != null)
            currentDistanceView.setText(String.valueOf(newFormat.format(locationTracker.currentDistance/1000)));

        if(currentSpeed != null)
            currentSpeedView.setText(String.valueOf(newFormat.format(locationTracker.currentSpeed)));

        if(averageSpeed != null)
            currentAverageView.setText(String.valueOf(newFormat.format(locationTracker.averageSpeed)));

        if(maximumSpeed != null)
            currentMaxSpeedView.setText(String.valueOf(newFormat.format(locationTracker.maximumSpeed)));
    }




    /****************/
    //MENU

    // Initiating Menu XML file (menu.xml)
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        Log.d("Cycle", "onCreateOptionsMenu");

        //Call the super method fist
        super.onCreateOptionsMenu(menu, inflater);

        getActivity().getMenuInflater().inflate(R.menu.map_menu, menu);
        playMenuBtn = menu.findItem(R.id.play);
        directionsMenuBtn = menu.findItem(R.id.directions);
        directionsMenuBtn.setVisible(false);

        //Tracking in process, so switch to tracking view
        if(locationTracker.isTracking){
            resumeTracking();
        }
    }

    public void resumeTracking() {
        switchToTrackingView();
        drawNavigationLine(locationTracker.route);
    }

    public void stopTracking() {
        getActivity().stopService(new Intent(getActivity(),LocationTracker.class));
        locationTracker.stopRouteTracking(getActivity());
    }

    public void startTracking() {
        getActivity().startService(new Intent(getActivity(),LocationTracker.class));
        locationTracker.startRouteTracking(getActivity());
    }

    public void switchToNormalView(){

        Log.d("Cycle", "switchToNormalView");

        playMenuBtn.setIcon(R.drawable.play);
        tLayout.setVisibility(View.GONE);

        handler.removeCallbacks(timerThread);

        //Set actionbar background color green
        if(getActivity() != null)
            ((MainMapActivity)getActivity()).setActionBarColor(getResources().getColor(R.color.green));
    }


    public void switchToTrackingView(){

        Log.d("Cycle", "switchToTrackingView");

        Long routingStartTime = null;
        Route route = locationTracker.route;
        if(route != null)
            routingStartTime = route.getCreatedTimestamp();

        final long startTime = routingStartTime != null ? routingStartTime : System.currentTimeMillis();

        //Clear map
        googleMap.clear();
        directionPoints.clear();

        playMenuBtn.setIcon(R.drawable.stop);
        tLayout.setVisibility(View.VISIBLE);

        handler = new Handler();
        timerThread = new Runnable() {
            public void run() {
                long millis = System.currentTimeMillis() - startTime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                timer.setText(String.format("%d:%02d", minutes, seconds));
                handler.postDelayed(this, 1000);
            }

        };

        if(getActivity() != null) {

            getActivity().runOnUiThread(timerThread);

            //Set actionbar background color red
            ((MainMapActivity) getActivity()).setActionBarColor(getResources().getColor(R.color.maroon));
        }
    }


    public boolean onOptionsItemSelected(MenuItem item) {

        Intent actionActivity;

        switch (item.getItemId()) {

            case R.id.play:

                if (!locationTracker.isTracking)
                    startTracking();
                else
                    stopTracking();

                return true;

            case R.id.list:

                actionActivity = new Intent(getActivity(), RouteListActivity.class);
                startActivity(actionActivity);
                getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

                return true;

            case R.id.about:

                actionActivity = new Intent(getActivity(), AboutActivity.class);
                startActivity(actionActivity);
                getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

                return true;

            case R.id.directions:

                if (null != polyline)
                {
                    polyline.remove();
                }

                directionsMenuBtn.setVisible(false);

                return true;

            case R.id.weather:

                //loadWeather(false);
                //Reload weather
                final Location loc = locationTracker.getCurrentLocation();


                if (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
                {
                    mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                    fLayout.setVisibility(View.INVISIBLE);
                }
                else
                {
                    if (LocationTracker.getInstance().isTracking)
                    {
                        CurrentWeather cw = LocationTracker.getInstance().route.getWeather();
                        temp = cw.getTemperature();
                        humidity = cw.getHumidity();
                        pressure = cw.getPressure();
                        windspeed = cw.getWindSpeed();
                        winddirection = cw.getWindDegrees();

                        try {
                            //Temperature (in Celcius)
                            thermometerView.setText(String.valueOf(newFormat.format(temp)) + " °C");
                            //Humidity (%)
                            dropletsView.setText(String.valueOf(newFormat.format(humidity)) + " %");
                            //Pressure (mBar)
                            pressureView.setText(String.valueOf(newFormat.format(pressure))  + " mBar");
                            //Wind speed (m/s)
                            windView.setText(String.valueOf(newFormat.format(windspeed))  + " km/h");
                            //Wind degrees
                            compassView.setText(String.valueOf(newFormat.format(winddirection)) + "°");
                        }
                        catch (Exception ex)
                        {

                        }
                    }
                    else
                    {
                        Thread t = new Thread() {
                            public void run() {

                                CycleSafeApiClient apiclient = new CycleSafeApiClient();

                                String response = null;

                                do {
                                    response = apiclient.getWeatherForLocation(loc);

                                    try {

                                        JSONObject json = new JSONObject(response);

                                        JSONArray weather = json.getJSONArray("weather");
                                        JSONObject cWeather = weather.getJSONObject(0);
                                        JSONObject cMain = json.getJSONObject("main");
                                        JSONObject cWind = json.getJSONObject("wind");
                                        temp = cMain.getDouble("temp");
                                        humidity = cMain.getDouble("humidity");
                                        pressure = cMain.getDouble("pressure");
                                        windspeed = cWind.getDouble("speed");
                                        winddirection = cWind.getDouble("deg");

                                        if (cWeather != null && cMain != null && cWind != null) {

                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    try {
                                                        //Temperature (in Celcius)
                                                        thermometerView.setText(String.valueOf(newFormat.format(temp)) + " °C");
                                                        //Humidity (%)
                                                        dropletsView.setText(String.valueOf(newFormat.format(humidity)) + " %");
                                                        //Pressure (mBar)
                                                        pressureView.setText(String.valueOf(newFormat.format(pressure)) + " mBar");
                                                        //Wind speed (m/s)
                                                        windView.setText(String.valueOf(newFormat.format(windspeed)) + " km/h");
                                                        //Wind degrees
                                                        compassView.setText(String.valueOf(newFormat.format(winddirection)) + "°");
                                                    } catch (Exception ex) {

                                                    }
                                                }
                                            });
                                        }
                                    } catch (Exception e) {
                                        //Failed
                                        //e.printStackTrace();
                                    }
                                }while(response == null);
                            }
                        };
                        t.start();
                    }


                    fLayout.setVisibility(View.VISIBLE);
                    mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                }


                return true;

            /*case R.id.share:

                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(sharingIntent, getText(R.string.share)));

                return true;

            case R.id.traffic:

                actionActivity = new Intent(getActivity(), MyRouteActivity.class);
                startActivity(actionActivity);
                getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

                return true;*/

            default:
                return super.onOptionsItemSelected(item);
        }

    }

}
