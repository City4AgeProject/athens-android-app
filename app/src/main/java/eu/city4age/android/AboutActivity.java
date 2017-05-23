package eu.city4age.android;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import eu.city4age.android.utils.DeviceID;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setContent();
    }

    //Set content
    public void setContent(){

        //Show Device ID
        String deviceID = DeviceID.id(this);
        if(deviceID != null){
            TextView deviceIdTextView = (TextView) findViewById(R.id.activityAbout_deviceId);
            deviceIdTextView.setText(deviceID);
        }
    }
}
