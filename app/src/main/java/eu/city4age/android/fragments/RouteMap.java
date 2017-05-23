package eu.city4age.android.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import eu.city4age.android.R;
import eu.city4age.android.SlidingUpPanelLayout;
import eu.city4age.android.apiClient.WeatherEngine;
import eu.city4age.android.model.CurrentWeather;
import eu.city4age.android.model.POI;
import eu.city4age.android.model.Route;
import eu.city4age.android.utils.LocationTracker;
import eu.city4age.android.utils.Log;
import eu.city4age.android.utils.Utils;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by lgiampouras on 1/29/15.
 */
public class RouteMap extends Fragment implements LocationTracker.RouteTrackerListener, WeatherEngine.WeatherEngineListener {

    private MapView mapView;
    private GoogleMap googleMap;
    private Bundle mBundle;

    private SlidingUpPanelLayout mLayout;
    private FrameLayout fLayout;
    private TextView thermometerView;
    private TextView windView;
    private TextView dropletsView;
    private TextView compassView;
    private TextView pressureView;
    LineChartView chart;
    private final DecimalFormat newFormat = new DecimalFormat("###.#");
    private double temp;
    private double humidity;
    private double pressure;
    private double windspeed;
    private double winddirection;

    //Point for line drawing
    private ArrayList<LatLng> directionPoints = new ArrayList<LatLng>();
    //The actual polyline
    Polyline mapPolyline;

    private boolean loadedforfirsTime = true;

    private MenuItem playMenuBtn = null;

    //Markers array, where markers will be saved
    private List<Marker> markers = new ArrayList<Marker>();


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d("Map Fragment", "Fragment attached");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Enable menu options from this fragment
        setHasOptionsMenu(true);

//        //Set the delegate for callback
//        //If the activity has not implemented the interface, then the fragment throws a ClassCastException.
//        try {
//            LocationTracker.getInstance().routeTrackerListener = this;
//        }
//        catch (ClassCastException e) {
//            throw new ClassCastException(this.toString() + " must implement LocationTracker.RouteTrackerListener");
//        }
//
//        //Start location tracker
//        LocationTracker.getInstance().startLocationTracker(getActivity());
    }



    @Override
    //Called when the UI is ready to draw the Fragment
    //this method must return a View that is the root of your fragment's layout.
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        Log.d("Map Fragment:", "Fragment view ready to be drawn");

        View inflatedView = inflater.inflate(R.layout.route_map_fragment, container, false);

        try
        {
            MapsInitializer.initialize(getActivity());
        }
        finally
        {
            //Do nothing
        }

        mapView = (MapView) inflatedView.findViewById(R.id.route_map);
        mapView.onCreate(mBundle);

        if (googleMap == null) {
           googleMap = ((MapView) inflatedView.findViewById(R.id.route_map)).getMap();
        }

        final Bundle extras = this.getActivity().getIntent().getExtras();
        TextView distance = (TextView) inflatedView.findViewById(R.id.distance);
        TextView avSpeedView = (TextView) inflatedView.findViewById(R.id.avSpeedView);
        TextView maxSpeedView = (TextView) inflatedView.findViewById(R.id.maxSpeedView);

        distance.setText(extras.getString("distance"));
        avSpeedView.setText(extras.getString("avSpeed"));
        maxSpeedView.setText(extras.getString("maxSpeed"));

        CurrentWeather cw = (CurrentWeather)extras.getSerializable("weather");
        Route route = (Route)extras.getSerializable("route");
        temp = cw.getTemperature();
        humidity = cw.getHumidity();
        pressure = cw.getPressure();
        windspeed = cw.getWindSpeed();
        winddirection = cw.getWindDegrees();

        thermometerView = (TextView) inflatedView.findViewById(R.id.thermometerView);
        windView = (TextView) inflatedView.findViewById(R.id.windView);
        dropletsView = (TextView) inflatedView.findViewById(R.id.humidityView);
        compassView = (TextView) inflatedView.findViewById(R.id.compassView);
        pressureView = (TextView) inflatedView.findViewById(R.id.pressureView);
        chart = (LineChartView) inflatedView.findViewById(R.id.chart);
        //chart.setInteractive(true);

        chart.setScrollEnabled(true);
        List<PointValue> values = new ArrayList<PointValue>();

        ArrayList pois = (ArrayList)route.getPOIS();
        List<AxisValue> xvals = new ArrayList<AxisValue>();

        for (int i=0; i<pois.size(); i++)
        {
            POI poi = (POI)pois.get(i);
            values.add(new PointValue(i, poi.getSpeed()));
            xvals.add(new AxisValue(i).setLabel(String.valueOf((int)poi.getSpeed())));
        }

        Axis xAxis = new Axis(xvals).setHasLines(true).setMaxLabelChars(4)
                .setTextColor(ChartUtils.COLOR_RED);



        //In most cased you can call data model methods in builder-pattern-like manner.
        Line line = new Line(values).setColor(Color.YELLOW).setStrokeWidth(1).setFilled(true);
        List<Line> lines = new ArrayList<Line>();
        line.setHasLabels(false).setHasLines(true).setHasPoints(false);
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setLines(lines);
        data.setAxisXBottom(xAxis);

        chart.setLineChartData(data);

        Viewport v = new Viewport(chart.getMaximumViewport());
        v.left = 0;
        v.right = 10;
        //chart.setMaximumViewport(v); //Sorry!, that was mistake, you don't have to change maximum viewport in this case.
        chart.setCurrentViewport(v);

        fLayout = (FrameLayout) inflatedView.findViewById(R.id.frame_layout);
        mLayout = (SlidingUpPanelLayout) inflatedView.findViewById(R.id.sliding_layout);
        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        mLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }
            @Override
            public void onPanelExpanded(View panel) {
                Log.i( "onPanelExpanded");
            }
            @Override
            public void onPanelCollapsed(View panel) {
                Log.i("onPanelCollapsed");
                //fLayout.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onPanelAnchored(View panel) {
                Log.i("onPanelAnchored");
                //fLayout.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onPanelHidden(View panel) {
                Log.i("onPanelHidden");
                //fLayout.setVisibility(View.INVISIBLE);
            }
        });

        return inflatedView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mapView.onResume();

        setUpMap();
    }


    @Override
    public void onPause() {
        super.onPause();

        mapView.onPause();
    }


    private void setUpMap() {

        final Bundle extras = this.getActivity().getIntent().getExtras();

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

        Route route = (Route)extras.getSerializable("route");
        ArrayList pois = (ArrayList)route.getPOIS();

        Log.d("GPS", new Gson().toJson(route));

        for (int i=0; i<pois.size(); i++)
        {
            POI poi = (POI)pois.get(i);

            if (i == 0)
            {
                CameraPosition currentPlace = new CameraPosition.Builder().target(new LatLng(poi.getLat(), poi.getLng())).zoom(18f).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
            }

            LatLng latLng = new LatLng(poi.getLat(), poi.getLng());

            //Add the new location
            directionPoints.add(latLng);
        }

        drawNavigationLine();
    }



    /****************/
    //Call back from LocationTracker


    @Override
    public void onTrackingStarted() {

    }
    @Override
    public void onTrackingStopped() {

    }
    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onLocationTracked(Location location, double currentDistance, double averageSpeed, double maximumSpeed) {

    }

    /****************/



    /****************/
    //weather

    public void loadWeather(Boolean showResult) {

        Location currentLocation = LocationTracker.getInstance().getCurrentLocation();

        WeatherEngine weatherEngine = new WeatherEngine();
        weatherEngine.weatherEngineListener = this;
        weatherEngine.loadCurrentWeather(LocationTracker.getInstance().getCurrentLocation(), showResult);
    }


    //Call back from WeatherEngineListener
    //Updates when new weather information is received
    public void weatherUpdated(CurrentWeather weather, Boolean success, Boolean showResult) {

        if (success) {

            //Add weather info to route if it's tracking the user
            if (LocationTracker.getInstance().route!=null) {
                LocationTracker.getInstance().route.setWeather(weather);
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
    //Navigation line

    private void drawNavigationLine() {

        PolylineOptions rectLine = new PolylineOptions().width(7).color(Color.RED);

        for(int i = 0 ; i < directionPoints.size() ; i++) {
            rectLine.add(directionPoints.get(i));
        }

        googleMap.clear();
        mapPolyline = googleMap.addPolyline(rectLine);
    }
    /****************/


    /****************/
    //MENU

    // Initiating Menu XML file (menu.xml)
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        //Call the super method fist
        super.onCreateOptionsMenu(menu, inflater);

        getActivity().getMenuInflater().inflate(R.menu.menu_route_map, menu);

    }


    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.weather:


                if (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
                {
                    mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                    //fLayout.setVisibility(View.INVISIBLE);
                }
                else
                {
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


                    //fLayout.setVisibility(View.VISIBLE);
                    mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
