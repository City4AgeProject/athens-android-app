package eu.city4age.android;


import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import eu.city4age.android.model.CurrentWeather;

public class RouteListAdapter extends BaseAdapter {

    private Context context;
    public ImageView weatherIcon;
    private ProgressDialog progressDialog;

    private List<String> items;
    private HashMap<String, HashMap<String, Object>> markerMap;

    public RouteListAdapter(Context context, List<String> items, HashMap<String, HashMap<String, Object>> markerMap) {

        this.context = context;
        this.items = items;
        this.markerMap = markerMap;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int pos) {
        return items.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup viewGroup) {

        final String itemPos = items.get(pos);
        HashMap<String, Object> poiDetails = markerMap.get(itemPos);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.route_list_item, null, false);


        TextView distanceView = (TextView) view.findViewById(R.id.distanceView);
        TextView avSpeedView = (TextView) view.findViewById(R.id.avSpeedView);
        TextView maxSpeedView = (TextView) view.findViewById(R.id.maxSpeedView);
        //ImageView deleteItemView = (ImageView) view.findViewById(R.id.deleteItem);
        //weatherIcon = (ImageView) view.findViewById(R.id.weatherStatus);

//        deleteItemView.setOnClickListener(new View.OnClickListener(){
//
//            @Override
//            public void onClick(View v){
//
//                progressDialog = ProgressDialog.show((Activity)context, "", ((Activity)context).getString(R.string.please_wait), true);
//                progressDialog.setCancelable(false);
//                AllRoutes allRoutes = Utils.loadRoutes(context);
//                ArrayList<Route> routes = (ArrayList)allRoutes.allRoutes;
//                routes.remove(Integer.parseInt(itemPos));
//                allRoutes.allRoutes = routes;
//                Utils.clearDataStorage(context);
//                Utils.saveRoutes(allRoutes, context);
//                context.startActivity(new Intent(context, RouteListActivity.class));
//                ((Activity)context).finish();
//                if(progressDialog !=null && progressDialog.isShowing()) progressDialog.dismiss();
//
//            }
//        });

        String weatherTitle = ((CurrentWeather)poiDetails.get("weather")).getTitle();

//        if (null == weatherTitle)
//            weatherIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.questionmark));
//        else if (weatherTitle.compareToIgnoreCase("Clear") == 0)
//            weatherIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.bigsun));
//        else if (weatherTitle.compareToIgnoreCase("Rain") == 0)
//            weatherIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.bigrain));
//        else if (weatherTitle.compareToIgnoreCase("Clouds") == 0)
//            weatherIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.bigfog));
//        else if (weatherTitle.compareToIgnoreCase("Mist") == 0)
//            weatherIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.bigmist));
//        else
//            weatherIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.bigsun));

//        Route route = (Route) poiDetails.get("route");
//        String savedOnServer = route.savedOnServer ? "Sync" : "Unsync";
//        distanceView.setText(savedOnServer);

        distanceView.setText((String)poiDetails.get("distance"));
        avSpeedView.setText((String)poiDetails.get("avSpeed"));
        maxSpeedView.setText((String)poiDetails.get("maxSpeed"));

        return view;
    }
}