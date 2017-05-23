package eu.city4age.android.utils;

import android.content.Context;
import android.provider.Settings;

/**
 * Created by lgiampouras on 2/20/15.
 */
public class DeviceID {

    private static String sID = null;

    public synchronized static String id(Context context) {

        //Get Android ID (which is unique for each user). More info: https://developer.android.com/reference/android/provider/Settings.Secure.html#ANDROID_ID
        if(sID == null)
            sID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        return sID;
    }

}