package com.example.austin.interactions;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.SystemRequirementsChecker;


public class BeaconActivity extends AppCompatActivity{

    private BeaconManager beaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        beaconManager = new BeaconManager(getApplicationContext());
        beaconManager.setBackgroundScanPeriod(10000, 5000);
        setContentView(R.layout.activity_beacon);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_embedded_sensor, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class IMEI {

        public static String get_dev_id(Context ctx){

            //Getting the Object of TelephonyManager
            TelephonyManager tManager = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);

            //Getting IMEI Number of Devide
            String Imei=tManager.getDeviceId();

            return Imei;
        }
    }

    public void sendEmail(View view) {
        EditText editText = (EditText) findViewById(R.id.editText);
        String email = editText.getText().toString();
        MyApplication myApplication = new MyApplication();
        IMEI thisIMEI = new IMEI();
        String thisId = thisIMEI.get_dev_id(this);
        myApplication.addUser(email, thisId);
    }

}
