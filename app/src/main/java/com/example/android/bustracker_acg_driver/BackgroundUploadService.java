package com.example.android.bustracker_acg_driver;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.example.android.bustracker_acg_driver.geofence.GeofenceErrorMessages;
import com.example.android.bustracker_acg_driver.geofence.GeofenceTransitionsIntentService;
import com.example.android.bustracker_acg_driver.select_route_activity.SelectRouteActivity;
import com.example.android.bustracker_acg_driver.splash_screen.JSONParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BackgroundUploadService extends Service implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener,
        ResultCallback<Status> {

    // LOG_TAG
    private final String TAG = "BackgroundService";

    // GoogleApiClient
    protected GoogleApiClient mGoogleApiClient;
    // LocationRequest
    protected LocationRequest mLocationRequest;
    // LastLocation
    protected Location mLastLocation;
    // Geofences in an ArrayList
    protected ArrayList<Geofence> mGeofenceList;
    // JSON parser class
    JSONParser jsonParser = new JSONParser();
    // routeID
    private int routeID;
    // SharedPreferences
    public static final String PREFS_FILE = "DriverPreferencesFile";
    // Geofence
    public static final HashMap<String, LatLng> GEOFENCE_LANDMARKS = new HashMap<String, LatLng>();

    /**
     * Used to set an expiration time for a geofence.
     * After this amount of time Location Services
     * stops tracking the geofence.
     */
    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 4;

    /**
     * There, geofences expire after 4 hours.
     */
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    public static final float GEOFENCE_RADIUS_IN_METERS = 100;


    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate() ========");
        // Build the GooleApiClient
        buildGoogleApiClient();

        // Empty list for storing geofences.
        mGeofenceList = new ArrayList<Geofence>();
        // Route's name and last RouteStop's lat/lng
        GEOFENCE_LANDMARKS.put(
                getSharedPreferences(PREFS_FILE, MODE_PRIVATE).getString(SelectRouteActivity.ROUTE_NAME, "DEREE"),
                new LatLng(
                        Double.parseDouble(getSharedPreferences(PREFS_FILE, MODE_PRIVATE).getString(SelectRouteActivity.END_LAT, "38.00367")),
                        Double.parseDouble(getSharedPreferences(PREFS_FILE, MODE_PRIVATE).getString(SelectRouteActivity.END_LNG, "23.830351"))));

        Log.e(TAG, GEOFENCE_LANDMARKS.toString());

        // Get the geofences used. Geofence data is hard coded in this sample.
        populateGeofenceList();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand() ========");
        routeID = getSharedPreferences(PREFS_FILE, MODE_PRIVATE).getInt(SelectRouteActivity.ROUTE_ID, 0);

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
            Log.e(TAG, "Upoad Coordinates");

            geofenceTest();

            // Execute the AsyncTask
            UploadCoordinates uploadCoordinatesToDb = new UploadCoordinates();
            uploadCoordinatesToDb.execute(routeID);
        } else {
            Log.e(TAG, "Service has stopped");

            // Disconnect GoogleApiClient
            if (mGoogleApiClient.isConnected()){
                mGoogleApiClient.disconnect();
            }

            // Stop this Service
            stopSelf();
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


    // Populate the Geofences ArrayList
    public void populateGeofenceList() {

        for (Map.Entry<String, LatLng> entry : GEOFENCE_LANDMARKS.entrySet()) {
            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(entry.getKey())

                            // Set the circular region of this geofence.
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            GEOFENCE_RADIUS_IN_METERS
                    )

                            // Set the expiration duration of the geofence. This geofence gets automatically
                            // removed after this period of time.
                    .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                            // Set the transition types of interest. Alerts are only generated for these
                            // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)

                            // Create the geofence.
                    .build());
        }

    }


    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            Log.e(TAG, "Geofences Added");
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    status.getStatusCode());
            Log.e(TAG, errorMessage);
        }
    }

    public void geofenceTest(){
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.e("Security Exception: ", securityException.toString());
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already in that geofence
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest
        return builder.build();
    }


    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addgeoFences()
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    class UploadCoordinates extends AsyncTask<Integer, String, String> {

        // LOG_TAG
        private static final String TAG = "UploadCoordinates";
        // AsyncTask to upload the location of the bus
        private static final String UPLOAD_COORDINATES_URL = "http://ashoka.students.acg.edu/BusTrackerAndroid/webServices/uploadCoordinates.php";
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
                Log.e(TAG, e.getMessage());
            } catch (NullPointerException e) {
                Log.e(TAG, e.toString());
            }

            return null;

        }

        protected void onPostExecute(String result) {
            if (result != null) {

            }

        }

    }


}
