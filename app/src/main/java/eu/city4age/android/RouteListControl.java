package eu.city4age.android;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.city4age.android.model.AllRoutes;
import eu.city4age.android.model.Route;
import eu.city4age.android.utils.Log;
import eu.city4age.android.utils.Utils;

public class RouteListControl extends FrameLayout {

    public static int catID = -1;
    public static final String PREF_FILE_NAME = "SantanderARPrefFile";

    public class Item {
        private String offer;
        private int offerType;
        private String header;
        private String subheader;
        private String img;
        private String lat;
        private String lon;
        private String viewStyle;
        private int index;
        private HashMap offerDetailsMap;


        public Item(String offer, int offerType, String header, String subheader, int selectedIndex, String img, String lat, String lon, String viewStyle, boolean isHead, HashMap offerDetailsMap) {
            this.offer = offer.trim();
            this.offerType = offerType;
            this.header = header;
            this.subheader = subheader;
            this.img = img;
            this.lat = lat;
            this.lon = lon;
            this.viewStyle = viewStyle;
            this.index = selectedIndex;
            this.offerDetailsMap = offerDetailsMap;
        }


        public String getOffer() {
            return offer;
        }

        public HashMap getOfferDetails() {
            return offerDetailsMap;
        }

        public String getHeader() {
            return header;
        }

        public String getSubheader() {
            return subheader;
        }

        public String getImg() {
            return img;
        }

        public String getLat() {
            return lat;
        }

        public String getLon() {
            return lon;
        }

        public String getViewStyle() {
            return viewStyle;
        }

        public int getIndex() {
            return index;
        }
    }

    private List<String> items;
    //public RouteListAdapter adapter;
    private List<Integer> bookmarksIndexList;
    private ListView listView;
    //public static ListView listView;
    public static int firstVisibleItem = 0;
    public static int lastVisibleItem = 0;

    public static final String PREFERENCE_FILENAME = "FirstClubPrefs";
    public static String mLat;
    public static String mLong;
    public static String query = "";

    public static boolean isScrolling = false;

    public HashMap storesMap = new HashMap();
    public static HashMap<String, HashMap<String, Object>> markerMap = new HashMap<String, HashMap<String, Object>>();

    public RouteListControl(Context context) {
        super(context);

        init(null);
    }

    public RouteListControl(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(attrs);
    }

    public RouteListControl(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(attrs);
    }

    private void init(AttributeSet attrs) {
        ((Activity)getContext()).getLayoutInflater()
                .inflate(R.layout.list_control, this, true);

        //final DrawableManager dm = new DrawableManager();

        listView = (ListView) findViewById(R.id.routes_list);
        listView.setBackgroundColor(Color.WHITE);
    }


    public void setIssue(HashMap feedMap) {

        Context context = ((Activity)getContext());

        items = new ArrayList<String>();
        bookmarksIndexList = new ArrayList<Integer>();
        markerMap = new HashMap<String, HashMap<String, Object>>();
        int countListItems = 0;


        try
        {
            AllRoutes allRoutes = Utils.loadRoutes(this.getContext());
            ArrayList<Route> routes = (ArrayList)allRoutes.allRoutes;

            DecimalFormat newFormat = new DecimalFormat("###.#");
            String valueRounded = null;

            for (Route route : routes)
            {
                if(route == null)
                    continue;

                Log.d("Routes", "routeID: " + String.valueOf(countListItems));

                HashMap<String, Object> routeDetails = new HashMap<String, Object>();
                routeDetails.put("routeID", String.valueOf(countListItems));

                valueRounded = newFormat.format(route.getDistanceCovered()/1000);
                routeDetails.put("distance", valueRounded + " " + getResources().getString(R.string.kmMovingOn) + " " + route.getPOIS().get(0).getTimestamp().substring(0, 10));

                valueRounded = newFormat.format(route.getAverageSpeed());
                routeDetails.put("avSpeed", valueRounded);

                valueRounded = newFormat.format(route.getMaximumSpeed());
                routeDetails.put("maxSpeed", valueRounded);

                valueRounded = newFormat.format(route.getRouteDuration());
                routeDetails.put("routeDuration", valueRounded);

                routeDetails.put("route", route);
                routeDetails.put("weather", route.getWeather());
                markerMap.put(String.valueOf(countListItems), routeDetails);
                items.add(String.valueOf(countListItems));
                countListItems ++;
            }
        }
        catch (Exception ex0)
        {

        }


        RouteListAdapter adapter = new RouteListAdapter(context, items, markerMap);
        listView.setAdapter(adapter);
    }



    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        final OnItemClickListener listener = onItemClickListener;
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
            {
                listener.onItemClick(parent, view, pos, id);
            }
        });
    }

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener onItemLongClickListener) {
        final AdapterView.OnItemLongClickListener listener = onItemLongClickListener;
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
            {
                listener.onItemLongClick(parent, view, pos, id);
                return true;
            }
        });
    }


}