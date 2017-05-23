package eu.city4age.android.apiClient;

import android.content.Context;
import org.json.JSONObject;

import eu.city4age.android.model.Route;
import eu.city4age.android.utils.Log;

/**
 * Created by lgiampouras on 2/26/15.
 */
public class RepositoryEngine {

    public static enum Message {
        SUCCESS,
        FAIL
    }

    public static interface Listener {
        public void onComplete(Message message);
    }

    /*public void loginWithCredentials(final String username, final String password, Context context) {

        final Context sourceContext = context;

        Thread t = new Thread() {
            public void run() {

                CycleSafeApiClient apiClient = new CycleSafeApiClient();
                String response = apiClient.loginWithCredentials(username, password);

                try {
                    //Set the key
                    CycleSafeApiClient.apiKey = response;
                    Log.d("API Key", "Got Key: " + response);


                    registerDevice(sourceContext);

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        };
        t.start();
    }*/


    /*public void registerDevice(final Context context) {

        Thread t = new Thread() {
            public void run() {

                CycleSafeApiClient apiClient = new CycleSafeApiClient();
                String response = apiClient.registerDevice(context);

                try {

                    JSONArray responseArray = new JSONArray(response);
                    JSONObject responseJSON = responseArray.getJSONObject(0);

                    String message = responseJSON.getString("message");

                    Log.d("Device Repository id %s",message);

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        };
        t.start();
    }*/


    public void registerObservation(final Route route, final Context context, final Listener listener) {

        Thread t = new Thread() {
            public void run() {

                CycleSafeApiClient apiClient = new CycleSafeApiClient();
                String response = apiClient.registerObservation(route, context);

                try {

                    if(response == null) {
                        listener.onComplete(Message.FAIL);
                        return;
                    }

                    //JSONArray responseArray = new JSONArray(response);
                    JSONObject responseJSON = new JSONObject(response);

                    int responseCode = responseJSON.getInt("responseCode");   //0: saved successfully

                    Log.d("ApiClient2", "responseCode: " + responseCode);

                    //Route saved successfully on server
                    if(responseCode == 0)
                        listener.onComplete(Message.SUCCESS);

                    //Route save failed
                    else
                        listener.onComplete(Message.FAIL);

                } catch (Exception e) {
                    //Failed
                    e.printStackTrace();
                    listener.onComplete(Message.FAIL);
                }
            }
        };
        t.start();
    }
}
