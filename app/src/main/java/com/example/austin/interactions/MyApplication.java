package com.example.austin.interactions;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;

/**
 * Created by Austin on 2/13/2016.
 */
public class MyApplication extends Application {
    private BeaconManager beaconManager;

    @Override
    public void onCreate() {
        super.onCreate();

        beaconManager = new BeaconManager(getApplicationContext());

        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                showNotification(
                        "You entered range of a new beacon,",
                        region.getMajor().toString());
                updateStatusDB("enter", region.getProximityUUID().toString(), region.getMajor().toString(), region.getMinor().toString());
            }
            @Override
            public void onExitedRegion(Region region) {
                // could add an "exit" notification too if you want (-:
                showNotification(
                        "You exited the range of a beacon",
                        region.getMajor().toString());
                updateStatusDB("exit", region.getProximityUUID().toString(), region.getMajor().toString(), region.getMinor().toString());
            }
        });

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                // Beacon 1
                beaconManager.startMonitoring(new Region(
                        "monitored region",
                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
                        38813, 15738));
                // Beacon 2
                beaconManager.startMonitoring(new Region(
                        "monitored region",
                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
                        62225, 40962));
                // Beacon 3
                beaconManager.startMonitoring(new Region(
                        "monitored region",
                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
                        30194, 9178));
                // Beacon 4
                beaconManager.startMonitoring(new Region(
                        "monitored region",
                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
                        7122, 62286));
            }
        });
    }

    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, BeaconActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    static public <T> void executeAsyncTask(AsyncTask<T, ?, ?> task,
                                            T... params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        }
        else {
            task.execute(params);
        }
    }

    public void updateStatusDB(String status, String prox_uuid, String major, String minor) {
        JSONObject jsonObject = new JSONObject();
        IMEI imei = new IMEI();
        try {
            jsonObject.put("status", status);
            jsonObject.put("device_id", imei.get_dev_id(this));
            jsonObject.put("prox_uuid", prox_uuid);
            jsonObject.put("major", major);
            jsonObject.put("minor", minor);

            executeAsyncTask(new BeaconClient(), "http://abgundry.pythonanywhere.com/beacon?reading="+jsonObject.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class BeaconClient extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            URL url;
            String u = params[0];
            HttpURLConnection urlConnection = null;
            JSONArray response = new JSONArray();

            try {
                url = new URL(u);
                urlConnection = (HttpURLConnection) url.openConnection();
                int responseCode = urlConnection.getResponseCode();

                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("GET");
                OutputStream os = urlConnection.getOutputStream();
                os.write(params[1].getBytes());
                os.flush();
                os.close();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }

            return null;
        }
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

    public void addUser(String email, String deviceId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
            jsonObject.put("device_id", deviceId);
            executeAsyncTask(new BeaconClient(), "http://abgundry.pythonanywhere.com/addUser?newUser=" + jsonObject.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
