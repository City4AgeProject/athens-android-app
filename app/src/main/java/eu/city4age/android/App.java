package eu.city4age.android;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import eu.city4age.android.services.LocationTrackerService;

/**
 * Created by ipapas on 08/02/17.
 */

public class App extends Application{


    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        context = this;

        //Start the LocationTrackerService
        startService(new Intent(this,LocationTrackerService.class));
    }

    //Get context
    public static Context getContext(){
        return context;
    }

}
