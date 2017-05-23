package eu.city4age.android.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import eu.city4age.android.utils.Utils;

/**
 * Created by lgiampouras on 2/3/15.
 */
public class Route implements Serializable {

    public Boolean savedOnServer;                //In case that it didn't exist network connection when user saved the route locally.
                                                 //The app will try to save the route on server as soon as it exists network connection.

    public float distanceCovered;                //meters
    public float routeDuration;                  //minutes
    public float averageSpeed;                   //m/s
    public float maximumSpeed;                   //m/s
    public String routeDate;
    public List<POI> POIS;
    public CurrentWeather weather;
    public String created;


    public float getDistanceCovered() {
        return distanceCovered;
    }

    public void setDistanceCovered(float distanceCovered) {
        this.distanceCovered = distanceCovered;
    }

    //constructor
    public Route() {

        POIS = new ArrayList<POI>();
        weather = new CurrentWeather();
        savedOnServer = null;

        Date date = new Date(System.currentTimeMillis());
        created = Utils.dateToString(date);
    }

    public long getCreatedTimestamp(){
        Date date = Utils.stringToDate(created);
        return date.getTime();
    }

    public float getRouteDuration() {
        return routeDuration;
    }

    public void setRouteDuration(float routeDuration) {
        this.routeDuration = routeDuration;
    }

    public void setEndLocation(){

        long endTimestamp = System.currentTimeMillis();

        POI penultimatePOI = POIS.get(POIS.size() - 1);
        POI endPOI = new POI();

        //Set poi data based on location
        endPOI.setLat(penultimatePOI.getLat());
        endPOI.setLng(penultimatePOI.getLng());

        endPOI.setSpeed(0f);

        Date date = new Date(endTimestamp);
        endPOI.setTimestamp(Utils.dateToString(date));
        endPOI.setTime(endTimestamp);

        //Add poi to route
        POIS.add(endPOI);

    }

    public float getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(float averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public float getMaximumSpeed() {
        return maximumSpeed;
    }

    public void setMaximumSpeed(float maximumSpeed) {
        this.maximumSpeed = maximumSpeed;
    }

    public List<POI> getPOIS() {
        return POIS;
    }

    public void setPOIS(List<POI> POIS) {
        this.POIS = POIS;
    }

    public CurrentWeather getWeather() {
        return weather;
    }

    public void setWeather(CurrentWeather weather) {
        this.weather = weather;
    }
}
