package eu.city4age.android.apiClient;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.city4age.android.model.CurrentWeather;

/**
 * Created by lgiampouras on 2/19/15.
 */

//Weather services
//http://openweathermap.org/API

public class WeatherEngine {

    //Interface that will be used as a  poi tracker callback
    public interface WeatherEngineListener {
        public void weatherUpdated(CurrentWeather weather, Boolean success, Boolean showResult);
    }

    public WeatherEngineListener weatherEngineListener;


    //Load Weather
    public void loadCurrentWeather(Location location, final Boolean showResult) {

        final Location loc = location;

        Thread t = new Thread() {
            public void run() {

                CurrentWeather currentWeather = new CurrentWeather();

                CycleSafeApiClient apiclient = new CycleSafeApiClient();
                String response = apiclient.getWeatherForLocation(loc);

                try {

                    JSONObject json = new JSONObject(response);

                    JSONArray weather = json.getJSONArray("weather");
                    JSONObject cWeather = weather.getJSONObject(0);
                    JSONObject cMain = json.getJSONObject("main");
                    JSONObject cWind = json.getJSONObject("wind");

                    if (cWeather != null && cMain != null && cWind != null) {
                        currentWeather.setLocationName(json.getString("name"));

                        //Weather title and description (in English)
                        currentWeather.setTitle(cWeather.getString("main"));
                        currentWeather.setDescription(cWeather.getString("description"));

                        //Temperature (in Celcius)
                        currentWeather.setTemperature(cMain.getDouble("temp"));

                        //Humidity (%)
                        currentWeather.setHumidity(cMain.getDouble("humidity"));

                        //Pressure (mBar)
                        currentWeather.setPressure(cMain.getDouble("pressure"));

                        //Temp min (in Celcius)
                        currentWeather.setTemp_min(cMain.getDouble("temp_min"));

                        //Temp max (in Celcius)
                        currentWeather.setTemp_max(cMain.getDouble("temp_max"));

                        //Wind speed (m/s)
                        currentWeather.setWindSpeed(cWind.getDouble("speed"));

                        //Wind degrees
                        currentWeather.setWindDegrees(cWind.getDouble("deg"));

                        /*********************/
                        //Call thge listner
                        if (weatherEngineListener != null)
                            weatherEngineListener.weatherUpdated(currentWeather, true, showResult);
                        /*********************/
                    }
                } catch (Exception e) {
                    //Failed
                    e.printStackTrace();

                    /*********************/
                    //Call the listener
                    if (weatherEngineListener != null)
                        weatherEngineListener.weatherUpdated(currentWeather, false, showResult);
                    /*********************/
                }
            }
        };
        t.start();
    }

}
