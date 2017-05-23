package eu.city4age.android.model;

import java.io.Serializable;

/**
 * Created by lgiampouras on 2/3/15.
 */
public class CurrentWeather implements Serializable {

    public String title;
    public String description;
    public String locationName;
    public double temperature;
    public double humidity;
    public double pressure;
    public double temp_min;
    public double temp_max;
    public double windSpeed;
    public double windDegrees;

    //Constructor
    public  CurrentWeather() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getTemp_min() {
        return temp_min;
    }

    public void setTemp_min(double temp_min) {
        this.temp_min = temp_min;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public double getTemp_max() {
        return temp_max;
    }

    public void setTemp_max(double temp_max) {
        this.temp_max = temp_max;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public double getWindDegrees() {
        return windDegrees;
    }

    public void setWindDegrees(double windDegrees) {
        this.windDegrees = windDegrees;
    }
}
