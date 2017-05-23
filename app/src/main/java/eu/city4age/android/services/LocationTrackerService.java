package eu.city4age.android.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import eu.city4age.android.utils.LocationTracker;

/**
 * Created by ipapas on 13/03/17.
 */

public class LocationTrackerService extends Service {

    public LocationTracker locationTracker;
    MyBinder binder=new MyBinder();
    LocationTrackerService service;
    static Context context;
    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
        locationTracker = LocationTracker.getInstance();
    }

    public class MyBinder extends Binder
    {
        public LocationTrackerService getServiceSystem()
        {
            return LocationTrackerService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
