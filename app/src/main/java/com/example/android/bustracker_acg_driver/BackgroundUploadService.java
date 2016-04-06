package com.example.android.bustracker_acg_driver;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.example.android.bustracker_acg_driver.splash_screen.JSONParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by giorgos on 3/5/2016.
 */
public class BackgroundUploadService extends Service implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private final String TAG = "BackgroundService";

    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected Location mLastLocation;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();
    // routeID
    private int routeID;
    // SharedPreferences
    public static final String PREFS_FILE = "DriverPreferencesFile";

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate() ========");
        buildGoogleApiClient();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand() ========");
        routeID = getSharedPreferences(PREFS_FILE, MODE_PRIVATE).getInt("selectedRouteID", 0);

        Log.e(TAG, "routeID " + routeID);
        if (routeID == 0){
            onDestroy();
        }
        mGoogleApiClient.connect();
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationRequest.setInterval(5000);

        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: " + e.toString());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.

        Log.e(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG, location.toString());

        mLastLocation = location;

        // This method is called here because
        // the onLocationChanged is running(!) ~5 minutes after the service has stopped
        if (isServiceRunning(this.getClass())) {
            // Execute the AsyncTask
            UploadCoordinates uploadCoordinatesToDb = new UploadCoordinates();
            uploadCoordinatesToDb.execute(routeID);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.e(TAG, "Service is Running");
                return true;
            }
        }
        Log.e(TAG, "Service is NOT Running");
        return false;
    }


    // AsyncTask to upload the location of the bus
    private static final String UPLOAD_COORDINATES_URL = "http://ashoka.students.acg.edu/BusTrackerAndroid/webServices/uploadCoordinates.php";

    class UploadCoordinates extends AsyncTask<Integer, String, String> {
        //ids
        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... args) {
            // Check for success tag
            int success;

            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("routeID", Integer.toString(args[0])));
                params.add(new BasicNameValuePair("lat", Double.toString(mLastLocation.getLatitude())));
                params.add(new BasicNameValuePair("lng", Double.toString(mLastLocation.getLongitude())));

                Log.d(TAG, "starting");

                //Posting name startingPointdata to script
                JSONObject json = jsonParser.makeHttpRequest(
                        UPLOAD_COORDINATES_URL, "POST", params);

                // full json response
                Log.d(TAG, "Uploading attempt: " + json.toString());

                // json success element
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d(TAG, "JSON response: " + json.toString());
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d(TAG, "Uploading Failure! " + json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }

        protected void onPostExecute(String result) {
            if (result != null) {

            }

        }

    }


}
