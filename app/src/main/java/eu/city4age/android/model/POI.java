package eu.city4age.android.model;

import java.io.Serializable;

/**
 * Created by lgiampouras on 2/3/15.
 */
public class POI implements Serializable {

    public double lat;
    public double lng;
    public float speed;
    public String timestamp;
    public long time;

    //constructor
    public POI() {

    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

}
