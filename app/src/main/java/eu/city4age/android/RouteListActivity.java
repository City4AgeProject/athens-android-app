package eu.city4age.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.HashMap;

import eu.city4age.android.model.AllRoutes;
import eu.city4age.android.model.CurrentWeather;
import eu.city4age.android.model.Route;
import eu.city4age.android.utils.Log;
import eu.city4age.android.utils.Utils;


public class RouteListActivity extends Activity {

    private RouteListControl listControl;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_list);

        getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setTitle(R.string.routes_list);

        initListControl();
    }


    private void initListControl() {

        progressDialog = ProgressDialog.show(RouteListActivity.this, "", getString(R.string.please_wait), true);
        progressDialog.setCancelable(false);

        HashMap<String, HashMap<String, String>> poisMap = new HashMap<String, HashMap<String, String>>();

        listControl = (RouteListControl) findViewById(R.id.route_list_control);
        listControl.setIssue(poisMap);

        listControl.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int pos, long id) {

                Intent intent = new Intent(RouteListActivity.this, RouteMapActivity.class);
                HashMap tmpMap = (HashMap)listControl.markerMap.get(String.valueOf(pos));

                intent.putExtra("weather", (CurrentWeather)tmpMap.get("weather"));
                intent.putExtra("route", (Route)tmpMap.get("route"));
                intent.putExtra("avSpeed", (String)tmpMap.get("avSpeed"));
                intent.putExtra("maxSpeed", (String)tmpMap.get("maxSpeed"));
                intent.putExtra("distance", (String)tmpMap.get("distance"));
                intent.putExtra("routeDuration", (String)tmpMap.get("routeDuration"));

                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

            }
        });

        listControl.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id) {

                Log.d("Cycle", "Long click");

                //Show message
                showDeleteRouteConfirmMessage(pos);

                return true;

            }
        });

        if(progressDialog !=null && progressDialog.isShowing()) progressDialog.dismiss();
    }

    public void showDeleteRouteConfirmMessage(final int position){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getText(R.string.delete))
                .setMessage(getText(R.string.deleteConfirmMessage))
                .setCancelable(true)
                .setPositiveButton(getText(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteRoute(position);
                    }
                })
                .setNegativeButton(getText(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       dialog.dismiss();
                    }
                });

        final AlertDialog alert = builder.create();
        alert.show();

    }

    public void deleteRoute(int position){
        Log.d("Cycle", "Delete route: " + position);

        Context context = RouteListActivity.this;

        progressDialog = ProgressDialog.show((Activity)context, "", ((Activity)context).getString(R.string.please_wait), true);
        progressDialog.setCancelable(false);
        AllRoutes allRoutes = Utils.loadRoutes(context);
        ArrayList<Route> routes = (ArrayList)allRoutes.allRoutes;
        routes.remove(position);
        allRoutes.allRoutes = routes;
        Utils.clearDataStorage(context);
        Utils.saveRoutes(allRoutes, context);
        context.startActivity(new Intent(context, RouteListActivity.class));
        ((Activity)context).finish();
        if(progressDialog !=null && progressDialog.isShowing()) progressDialog.dismiss();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_route_list, menu);
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
}
