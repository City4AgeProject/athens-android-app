package eu.city4age.android.apiClient;

import android.content.Context;
import android.location.Location;

import eu.city4age.android.R;
import eu.city4age.android.model.POI;
import eu.city4age.android.model.Route;
import eu.city4age.android.utils.CycleSafeSSLSocketFactory;
import eu.city4age.android.utils.DeviceID;
import eu.city4age.android.utils.Log;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lgiampouras on 2/3/15.
 */
public class CycleSafeApiClient {

    /* Network Settings */
    // Milliseconds until a connection is established.
    public static final int NETWORK_CONNECTION_TIMEOUT = 60000;

    // Milliseconds to wait for data.
    public static final int NETWORK_SOCKET_TIMEOUT = 10000;


    /***************************/
    //Services URL
    private static final String servicesURL = "http://santander.radical-project.eu:3000/";
    private static final String weatherServiceURL = "http://api.openweathermap.org/data/2.5/weather";
    //private static final String loginServiceURL = "https://santander.radical-project.eu:8181/Radical/rest/dataapi/getApiKey";
    /***************************/

    /***************************/
    //Needed variables
    public static String apiKey;
    public static String devideID;
    /***************************/


    private Gson gson;
    private DefaultHttpClient client;
    private HttpClient secureClient;

    // Setup a single response handler for all network operations to use
    private ResponseHandler<String> resposneHandler = new ResponseHandler<String>() {

        @Override
        public String handleResponse(HttpResponse response)
                throws ClientProtocolException, IOException {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200 && statusCode != 201) {
                Log.d("ApiClient", "Server returned STATUS " + String.valueOf(statusCode));
                return "";
            }
            return EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
        }
    };


    //Constructor
    public CycleSafeApiClient() {

        gson = new Gson();

        client = new DefaultHttpClient();

        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, NETWORK_CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParameters, NETWORK_SOCKET_TIMEOUT);

        client.setParams(httpParameters);
    }


    /***************************/
    //client for HTTPs (with SSL) calls
    public HttpClient getSecureHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new CycleSafeSSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }
    /***************************/


    /***************************/
    /* Service URLs getters */
    public String getWeatherURLForLocation(Location loc) {
        return String.format("%s?%s=%f&%s=%f&units=metric", weatherServiceURL, "lat", loc.getLatitude(), "lon", loc.getLongitude());
    }


    /*public String getLoginServiceURL(String username, String password) {
        return String.format("%s?%s=%s&%s=%s", loginServiceURL, "name", username, "password", password);
    }

    public String getRegisterServiceURL() {
        return String.format("%sregisterDevice?%s=%s", servicesURL, "api_key", apiKey);
    }

    public String getRegisterObservationURL() {
        return String.format("%sregisterObservation?%s=%s", servicesURL, "api_key", apiKey);
    }*/
    /***************************/



    /***************************/
    //Weather service

    public String getWeatherForLocation(Location loc) {

        try {
            String url = getWeatherURLForLocation(loc);
            Log.d("ApiClient", "Http GET to " + url );

            HttpGet getMethod = new HttpGet(url);

            getMethod.setHeader("Accept", "application/json");

            String responseBody = client.execute(getMethod, resposneHandler);
            Log.d("ApiClient", " Response: " + responseBody);

            return responseBody;
        }
        catch (Exception e) {
            Log.e("NETWORK ERROR", e.toString());
            return null;
        }
    }
    /***************************/



    /***************************/
    //CycleSafe services

    /*public String loginWithCredentials(String username, String password) {

        try {
            String url = getLoginServiceURL(username, password);

            HttpGet getMethod = new HttpGet(url);

            getMethod.setHeader("Accept", "text/plain");

            Log.d("ApiClient", "Http GET to " + url );

            secureClient = getSecureHttpClient();

            String responseBody = secureClient.execute(getMethod, resposneHandler);
            Log.d("ApiClient", " Response: " + responseBody);

            return responseBody;
        }
        catch (Exception e) {
            Log.e("NETWORK ERROR", e.toString());
            return null;
        }
    }*/


    /*public String registerDevice(Context context) {

        if (apiKey==null || apiKey.isEmpty()) {
            return null;
        }

        try {
            String url = getRegisterServiceURL();

            HttpPut putMethod = new HttpPut(url);

            putMethod.setHeader("Accept", "application/json");

            Log.d("ApiClient", "Http PUT to " + url );

            devideID = DeviceID.id(context);

            JSONObject json = new JSONObject();
            json.put("Device_UUID", devideID);
            json.put("Model","Android Phone");
            json.put("Manufacturer","Google SA");
            json.put("Latitude",String.format("%s",LocationTracker.getInstance().getCurrentLatitude()));
            json.put("Longitude",String.format("%s",LocationTracker.getInstance().getCurrentLongitude()));
            json.put("Status","1");
            json.put("Type","Device");

            JSONArray array = new JSONArray();
            JSONObject item = new JSONObject();
            item.put("Name", "");
            item.put("Unit", "");
            item.put("Data_Type", "");
            array.put(item);

            json.put("Capabilities", array);

            String args = json.toString();

            StringEntity putData = new StringEntity(args);
            putData.setContentType("application/json");
            putMethod.setEntity(putData);

            String responseBody = client.execute(putMethod, resposneHandler);
            Log.d("ApiClient", " Response: " + responseBody);

            return responseBody;
        }
        catch (Exception e) {
            Log.e("NETWORK ERROR", e.toString());
            return null;
        }
    }*/



    public String registerObservation(Route route, Context context) {

        /*if (apiKey==null || apiKey.isEmpty()) {
            return null;
        }*/

        try {
            //Get last POI's date
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");

            Date lastDate = dateFormatter.parse(((POI)route.getPOIS().get(route.getPOIS().size()-1)).getTimestamp());

            String unixDate = Long.toString(lastDate.getTime()*1000);

            String jsonRouteString = gson.toJson(route);

            JSONObject measurementsObject = new JSONObject();
            //measurementsObject.put("Type","Route");
            measurementsObject.put("value",jsonRouteString.toLowerCase());
            //measurementsObject.put("Unit","City4Age");

            JSONArray measurementsArray = new JSONArray();
            measurementsArray.put(measurementsObject);
            devideID = DeviceID.id(context);

            JSONObject json = new JSONObject();
            json.put("device_uuid",devideID);
            //json.put("Latitude",String.format("%s",LocationTracker.getInstance().getCurrentLatitude()));
            //json.put("Longitude",String.format("%s",LocationTracker.getInstance().getCurrentLongitude()));
            //json.put("ObservationDate",unixDate);
            json.put("measurements",measurementsArray);

            String args = json.toString();
            args = args.replace("\\", "");
            args = args.replace("\"{", "{");
            args = args.replace("}\"", "}");

            //Service URL
            //String url = getRegisterObservationURL();
            final String CITY4AGE_SERVICES_URL = context.getResources().getString(R.string.CITY4AGE_SERVICES_URL);
            String url = CITY4AGE_SERVICES_URL + "/platform/routes/addRoutes";

            HttpPost postMethod = new HttpPost(url);

            postMethod.setHeader("Accept", "application/json");
            postMethod.setHeader("Content-type", "application/json");

            StringEntity postData = new StringEntity(args);
            //postData.setContentType("application/json");
            postMethod.setEntity(postData);

            Log.d("ApiClient2", "Url: " + url );
            Log.d("ApiClient2", "Body: " + args );

            String responseBody = client.execute(postMethod, resposneHandler);

            Log.d("ApiClient2", " Response: " + responseBody);

            return responseBody;

        }
        catch (Exception e) {
            Log.e("ApiClient2", e.toString());
            return null;
        }
    }
    /***************************/

}