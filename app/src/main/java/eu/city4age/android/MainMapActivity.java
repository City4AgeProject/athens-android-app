package eu.city4age.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import android.os.StrictMode;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import eu.city4age.android.fragments.Map;
import eu.city4age.android.utils.DeviceID;
import eu.city4age.android.utils.Utils;

public class MainMapActivity extends Activity {

    private Map mapFrag;

//    private ServiceConnection serviceConnection = new ServiceConnection() {
//        public void onServiceConnected(ComponentName cn, IBinder ib) {
//            Log.d("Cycle", "onServiceConnected");
//
//            LocationTrackerService service = ((LocationTrackerService.MyBinder) ib).getServiceSystem();
//
//            LocationTracker locationTracker = service.locationTracker;
//
//            //Set the delegate for callback
//            try {
//                //LocationTracker.getInstance().routeTrackerListener = Map.this;
//            }
//            catch (ClassCastException e) {
//                throw new ClassCastException(this.toString() + " must implement LocationTracker.RouteTrackerListener");
//            }
//
//            //Start location tracker
//            //LocationTracker.getInstance().startLocationTracker(getActivity());
//        }
//
//        public void onServiceDisconnected(ComponentName cn) {
//            //serviceBound = false;
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_map);

        String deviceID = DeviceID.id(this);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //Show actionbar
        getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setTitle(R.string.app_name);

        //Set title color white
        Spannable text = new SpannableString(getActionBar().getTitle());
        text.setSpan(new ForegroundColorSpan(Color.WHITE), 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        getActionBar().setTitle(text);

        //Set actionbar background color green
        setActionBarColor(getResources().getColor(R.color.green));

        if (savedInstanceState == null) {

            mapFrag = new Map();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.mapfragment, mapFrag);
            fragmentTransaction.commit();
            getFragmentManager().executePendingTransactions();
        }


        //If there is no Internet connection alert the user
        if (!Utils.isNetworkAvailable(this))
        {
            //Show message
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getText(R.string.note))
                    .setMessage(getText(R.string.nointernet))
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Kill the application process
                            //int pid = android.os.Process.myPid();
                            //android.os.Process.killProcess(pid);
                        }
                    });

            final AlertDialog alert = builder.create();
            alert.show();
        }

        //Try to synchronize only-locally saved routes
        else
            Utils.synchronizeRoutes(this);


        //Bind activity with the LocationTrackerService
//        Intent intent = new Intent(this, LocationTrackerService.class);
//        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void setActionBarColor(int resource)
    {
        getActionBar().setBackgroundDrawable(new ColorDrawable(resource));
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(true);
    }

}
