package eu.city4age.android;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import eu.city4age.android.fragments.RouteMap;
import eu.city4age.android.utils.Log;


public class RouteMapActivity extends Activity {

    private RouteMap mapFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_map);

        Log.d("RouteMapActivity");

        //getActionBar().setBackgroundDrawable(new ColorDrawable(R.color.dark_blue));

        final Bundle extras = getIntent().getExtras();

        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setCustomView(R.layout.custom_actionbar);
        TextView duration = (TextView)getActionBar().getCustomView().findViewById(R.id.duration);
        duration.setText((Long.parseLong(extras.getString("routeDuration"))/1000)/60 + " " + getResources().getString(R.string.minutes));

        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setTitle(R.string.app_name);

        if (savedInstanceState == null) {

            mapFrag = new RouteMap();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.routemapfragment, mapFrag);
            fragmentTransaction.commit();
            getFragmentManager().executePendingTransactions();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_route_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}
