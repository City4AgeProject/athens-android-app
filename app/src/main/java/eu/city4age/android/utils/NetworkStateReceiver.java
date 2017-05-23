package eu.city4age.android.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import eu.city4age.android.App;

/**
 * Created by ipapas on 10/02/17.
 */

public class NetworkStateReceiver extends BroadcastReceiver
{
    public void onReceive(Context context, Intent intent)
    {
        Log.d("app","Network connectivity change");

        if(intent.getExtras() != null)
        {
            NetworkInfo ni = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);

            //Network connected
            if(ni!=null && ni.getState() == NetworkInfo.State.CONNECTED)
            {
                Log.i("app", "Network " + ni.getTypeName() + " connected");

                //Save only-locally saved routes to server
                Utils.synchronizeRoutes(App.getContext());
            }
        }

        //Network disconnected
        if(intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE))
        {
            Log.d("app", "There's no network connectivity");
        }
    }
}