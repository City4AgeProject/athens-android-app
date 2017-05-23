package eu.city4age.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import eu.city4age.android.App;
import eu.city4age.android.apiClient.RepositoryEngine;
import eu.city4age.android.model.AllRoutes;
import eu.city4age.android.model.Route;

/**
 * Created by lgiampouras on 2/3/15.
 */

public final class Utils {

    public static SimpleDateFormat dateFormater = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");

    /************/
    //Handler keys. Don't touch
    public static final int SUCCESS = 0;
    public static final int FAILURE = -1;
    /************/

    /************/
    //SharedPreferences keys. Don't touch
    public static String cycleSafeData = "CYCLESAFEDATA";
    public static String savedRoutes = "SAVEDROUTES";
    /************/

    //List of routes that are saved only locally (never sent to server)
    public static AllRoutes allRoutes;
    public static List<Route> unsynchronizedRoutes;
    public static int unsynchronizedRoutesIndex;

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }


    public static boolean isValidEmail(CharSequence target) {

        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }


    /* Route saving and loading to disk */

    public static void saveRoutes(AllRoutes routes, Context context) {

        Gson gson = new Gson();

        //Mode private, visible only from the app
        SharedPreferences sharedPref = context.getSharedPreferences(Utils.cycleSafeData, Context.MODE_PRIVATE);

        final SharedPreferences.Editor editor = sharedPref.edit();

        try {
            JSONObject routesJSON = new JSONObject(gson.toJson(routes));  Log.d("Routes storage", "saveRoutes: " + routesJSON);

            //Convert it to String
            String routesJSONString = routesJSON.toString();

            //Save it
            editor.putString(Utils.savedRoutes, routesJSONString);

            editor.apply();
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.d("Routes storage", e.getLocalizedMessage());
        }
    }

    public static AllRoutes loadRoutes(Context context) {

        Gson gson = new Gson();

        //Mode private, visible only from the app
        SharedPreferences sharedPref = context.getSharedPreferences(Utils.cycleSafeData, Context.MODE_PRIVATE);

        //Read the disk and get them all
        String savedRoutes = sharedPref.getString(Utils.savedRoutes, null);

        if (savedRoutes!=null && !savedRoutes.isEmpty()) {
            Log.d("Saved Roots JSON: ", savedRoutes);
        }

        AllRoutes routes = null;
//        try {
            routes = gson.fromJson(savedRoutes, AllRoutes.class);
//        }
//        catch (Exception e) {
//            Log.d("Routes storage", e.getLocalizedMessage());
//        }

        return routes;
    }

    //Save on server locally-saved routes that haven't being saved on server yet
    public static void synchronizeRoutes(Context context) {

        AllRoutes routes = loadRoutes(context);

        //No local saved routes
        if(routes == null)
            return;

        allRoutes = routes;
        unsynchronizedRoutes = new ArrayList<>();
        for(Route route : routes.allRoutes)
            if(route != null && route.savedOnServer != null && route.savedOnServer == false)
                unsynchronizedRoutes.add(route);

        //All local routes have being saved on server too
        if(unsynchronizedRoutes.size() == 0)
            return;

        unsynchronizedRoutesIndex = 0;
        Route firstUnsynchronizedRoute = unsynchronizedRoutes.get(unsynchronizedRoutesIndex);
        synchronizeRoute(firstUnsynchronizedRoute);

    }

    //Save on server locally-saved route that hasn't being saved on server yet
    public static void synchronizeRoute(final Route route){

        Log.d("synchronize", "synchronizeRoute");

        route.savedOnServer = null;
        RepositoryEngine repEngine = new RepositoryEngine();
        repEngine.registerObservation(route, App.getContext(), new RepositoryEngine.Listener() {
            @Override
            public void onComplete(RepositoryEngine.Message message) {

                if(message == RepositoryEngine.Message.SUCCESS){

                    //Mark route as synchronized
                    route.savedOnServer = true;
                    Utils.saveRoutes(allRoutes, App.getContext());

                    //Synchronize next un-synchronized route
                    unsynchronizedRoutesIndex += 1;
                    if(unsynchronizedRoutesIndex < unsynchronizedRoutes.size()){
                        Route nextUnsynchornizedRoute = unsynchronizedRoutes.get(unsynchronizedRoutesIndex);
                        synchronizeRoute(nextUnsynchornizedRoute);
                    }

                    return;
                }

                //Free resources
                allRoutes = null;
                unsynchronizedRoutes = null;

            }
        });

    }

    public static void clearDataStorage(Context context) {

        //Node private, visible only from the app
        SharedPreferences sharedPref = context.getSharedPreferences(Utils.cycleSafeData, Context.MODE_PRIVATE);

        //Clear all data
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Utils.savedRoutes, "");
        editor.apply();
    }

    public static Date stringToDate(String date){

        try {
            return dateFormater.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String dateToString(Date date){

        return dateFormater.format(date);
    }

}
