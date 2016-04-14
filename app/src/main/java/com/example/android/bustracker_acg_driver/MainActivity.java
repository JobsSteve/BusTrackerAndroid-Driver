package com.example.android.bustracker_acg_driver;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bustracker_acg_driver.database.BusTrackerDBHelper;
import com.example.android.bustracker_acg_driver.network_connection.NetworkUtility;
import com.example.android.bustracker_acg_driver.select_route_activity.SelectRouteActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener, OnMapReadyCallback {

    // LOG TAG
    protected static final String TAG = "MainActivity";
    // ImageButton for the Network Status, EndRoute
    ImageButton networkStatusButton, endRoute;
    // Google API Client
    protected GoogleApiClient mGoogleApiClient;
    // Location Request
    protected LocationRequest mLocationRequest;
    // Last Location
    protected Location mLastLocation;
    // Google Map
    GoogleMap gMap;
    // Flag - to know if googleMap is ready
    boolean gMapReady = false;
    // Marker for driver's last location
    private Marker marker;
    // CameraPosition
    static final CameraPosition ATHENS = CameraPosition.builder()
            .target(new LatLng(37.9755491, 23.7345239))
            .zoom(12)
            .bearing(0)
            .tilt(0)
            .build();
    // The selectedRouteID
    private int routeID;
    // NetworkChangeReceiver
    NetworkChangeReceiver mNetworkChangeReceiver = null;
    // Flag to know if the receiver is registered
    boolean isReceiverRegistered = false;
    // Identifier for the permission request
    private static final int LOCATION_PERMISSIONS_REQUEST = 1;
    // LinearLayout - RootLayout
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Runtime permission for API23
        getPermissionToReadUserLocation();


        // Set a Toolbar to replace the ActionBar/AppBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher);

        // Get the Network Status ImageButton
        networkStatusButton = (ImageButton) findViewById(R.id.network_status_button);
        networkStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, NetworkUtility.getConnectivityStatusString(MainActivity.this), Toast.LENGTH_SHORT).show();
            }
        });

        // Get the Forced End Route ImageButton
        endRoute = (ImageButton) findViewById(R.id.end_route_button);
        endRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmEndOfRouteDialogFragment confirmEndOfRouteDialogFragment = new ConfirmEndOfRouteDialogFragment();
                confirmEndOfRouteDialogFragment.show(getSupportFragmentManager(), "ConfirmRouteEnd");
            }
        });

        // Check if there are arguments passed to the fragment
        Bundle bundleExtras = getIntent().getExtras();
        if (bundleExtras != null) {
            routeID = bundleExtras.getInt(SelectRouteActivity.ROUTE_ID);

            if (routeID != 0) {
                // BackgroundUploadService - Keep tracking even the app is closed
                Intent service = new Intent(this, BackgroundUploadService.class);
                startService(service);
            } else {
                routeID = getSharedPreferences(SelectRouteActivity.PREFS_FILE, Activity.MODE_PRIVATE).getInt(SelectRouteActivity.ROUTE_ID, 0);
            }

            PrepareDataAsyncTask prepareDataAsyncTask = new PrepareDataAsyncTask();
            prepareDataAsyncTask.execute();

            Log.e(TAG, "selectedRouteID in main: " + routeID);

        }

        // Build the GoogleApiClient
        buildGoogleApiClient();
        // And connect
        mGoogleApiClient.connect();
        // Location Request
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);



        // Then create a LocationSettingsRequest.Builder
        // and add all of the LocationRequests that the app will be using:
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        // This is the key ingredient - make the popup dialog a YES or NO (not NEVER) option
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MainActivity.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });



        // Find the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.driver_map);
        // A googleMap must be acquired using getMapAsync(OnMapReadyCallback).
        // This class automatically initializes the maps system and the view.
        mapFragment.getMapAsync(this);
    }


    // Called when the user is performing an action which requires the app to read the
    // user's contacts
    public void getPermissionToReadUserLocation() {
        // 1) Use the support library version ContextCompat.checkSelfPermission(...) to avoid
        // checking the build version since Context.checkSelfPermission(...) is only available
        // in Marshmallow
        // 2) Always check for permission (even if permission has already been granted)
        // since the user can revoke permissions at any time through Settings
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= 23) {
                // Marshmallow+

                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSIONS_REQUEST);
                return;
            } else {
                // Pre-Marshmallow
            }

        }
    }


    // Callback with the request from calling requestPermissions(...)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original LOCATION request
        if (requestCode == LOCATION_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Read Contacts permission granted", Toast.LENGTH_SHORT).show();

                // Restart the activity
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            } else {
//                Toast.makeText(this, "Read Contacts permission denied", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        final LocationSettingsS states = LocationSettingsStates.fromIntent(intent);
        switch (requestCode) {
            case 1000:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        Log.e(TAG, "GPS turned on");
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Log.e(TAG, "GPS is off");
                        finish();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        Log.e(TAG, "onStart ========");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        Log.e(TAG, "onStop ========");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, "onRestart ========");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume ========");

        // Register the NetworkChangedReceiver
        if (!isReceiverRegistered) {
            if (mNetworkChangeReceiver == null)
                mNetworkChangeReceiver = new NetworkChangeReceiver();
            registerReceiver(mNetworkChangeReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
            isReceiverRegistered = true;
        }

        // Check if Route is in progress
        if (!getSharedPreferences(SelectRouteActivity.PREFS_FILE, MODE_PRIVATE).getBoolean(SelectRouteActivity.ROUTE_IN_PROGRESS, true)) {
            ConfirmEndOfRouteDialogFragment confirmEndOfRouteDialogFragment = new ConfirmEndOfRouteDialogFragment();
            confirmEndOfRouteDialogFragment.show(getSupportFragmentManager(), "ConfirmRouteEnd");
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause ========");

        // Unregister the NetworkChangedReceiver
        if (isReceiverRegistered) {
            unregisterReceiver(mNetworkChangeReceiver);
            mNetworkChangeReceiver = null;
            isReceiverRegistered = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy ========");
    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }


    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle bundle) {
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

        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, location.toString());

        mLastLocation = location;
        LatLng mPosition = new LatLng(location.getLatitude(), location.getLongitude());

        if (gMapReady) {
            // Add a marker and Fly camera there
            setMarker(mPosition);
        }

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMapReady = true;
        gMap = googleMap;

        // Sets a button at the top right corner
//        try {
//            gMap.setMyLocationEnabled(true);
//        } catch (SecurityException e) {
//            Log.e("Security Exception: ", e.toString());
//        }

        // Disable the Map Toolbar
        gMap.getUiSettings().setMapToolbarEnabled(false);

        moveTo(ATHENS);

    }

    private void moveTo(CameraPosition target) {
        gMap.moveCamera(CameraUpdateFactory.newCameraPosition(target));
    }

    private void flyTo(CameraPosition target) {
        gMap.animateCamera(CameraUpdateFactory.newCameraPosition(target), 1000, null);
    }

    /**
     * Re-Set the (bus) marker
     */
    private void setMarker(LatLng latLng) {
        // Remove the latest marker
        if (marker != null) {
            marker.remove();
        }
        // Add a new marker on map
        marker = gMap.addMarker(new MarkerOptions().position(latLng));
        // Set an icon
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.bus_marker));
        // Move to its cameraPosition
        moveTo(CameraPosition.builder()
                .target(latLng)
                .zoom(17)
                .bearing(0)
                .tilt(0)
                .build());
    }


    public void updateNetworkStatusButton(int connectivityStatus) {
        Drawable newBackground = null;

        if (connectivityStatus == NetworkUtility.TYPE_WIFI) {
            newBackground = getResources().getDrawable(R.drawable.network_status_button_wifi);
        } else if (connectivityStatus == NetworkUtility.TYPE_MOBILE) {
            newBackground = getResources().getDrawable(R.drawable.network_status_button_mobile);
        } else if (connectivityStatus == NetworkUtility.TYPE_NOT_CONNECTED) {
            newBackground = getResources().getDrawable(R.drawable.network_status_button_not_connected);
        }

        try {
            networkStatusButton.setBackground(newBackground);
            networkStatusButton.invalidate();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage() + ", or NullPointerException");
        }
    }


    /**
     * AsyncTask to prepare the array lists with the information for the map
     */
    private class PrepareDataAsyncTask extends
            AsyncTask<Void, Void, Void> {

        // LOG TAG
        private static final String TAG = "PrepareDataAsyncTask";
        // Route's name
        private String routeName;
        // ArrayList with the Stations' names
        private ArrayList<String> stationNames;
        // ArrayList with the Station's times
        private ArrayList<String> stationTimes;
        // ArrayList with the Stations' latitudes & longitudes
        private ArrayList<LatLng> stationLatLngs;
        // ArrayList with the route's Snapped Points (in latitudes-longitudes)
        private ArrayList<LatLng> stationSnappedLatLngs;


        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Check SharedPreferences for the language
                SharedPreferences sharedPreferences = getSharedPreferences(SelectRouteActivity.PREFS_FILE, Activity.MODE_PRIVATE);
                // Database Helper
                BusTrackerDBHelper db = new BusTrackerDBHelper(MainActivity.this);


                if ( Locale.getDefault().getDisplayLanguage().equals("Ελληνικά") ) {
                    routeName = db.getRouteNameGR_byID(routeID);
                    stationNames = db.getAllRouteStopNamesGR(routeID);
                } else {
                    routeName = db.getRouteNameENG_byID(routeID);
                    stationNames = db.getAllRouteStopNamesENG(routeID);
                }

                // Initialize the array lists with data from the db
                stationTimes = db.getAllRouteStopTimes(routeID);
                stationLatLngs = db.getAllRouteStopLatLngs(routeID);
                stationSnappedLatLngs = db.getAllSnappedPointLatLngs(routeID);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            try {
                // Add markers to google map
                int numberOfStations = stationNames.size();
                for (int i = 0; i < numberOfStations; i++) {
                    gMap.addMarker(new MarkerOptions()
                            .position(stationLatLngs.get(i))
                            .title(stationNames.get(i))
                            .snippet(stationTimes.get(i) + " - " + routeName)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop_red_marker)));
                }

                // Draw the polyline on google map
                PolylineOptions polylineOptions = new PolylineOptions();
                for (LatLng snappedPoint : stationSnappedLatLngs) {
                    polylineOptions.add(snappedPoint);
                }
                polylineOptions.width(10).color(Color.RED);
                gMap.addPolyline(polylineOptions);
            } catch (Exception e) {
                moveTo(ATHENS);

                Log.e(TAG, "NullPointerException: " + e.getMessage());
            }

        }

    }


    public class NetworkChangeReceiver extends BroadcastReceiver {

        // LOG_TAG
        private static final String TAG = "NetworkChangeReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            String status = NetworkUtility.getConnectivityStatusString(context);
            int connectivityStatus = NetworkUtility.getConnectivityStatus(context);

            // Log the new status
            Log.e(TAG, connectivityStatus + ": " + status);

            // Update the UI
            updateNetworkStatusButton(connectivityStatus);
        }
    }


    /**
     * Dialog Fragment for route confirmation
     */
    public static class ConfirmEndOfRouteDialogFragment extends DialogFragment {

        // Route Text View
        private TextView routeTextView;
        // Yes - positive button
        private Button yesButton;
        // No - negative button
        private Button noButton;


        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);

            View view = inflater.inflate(R.layout.dialog_fragment_confirm_route_end, null, false);

            // Find the TextView and setText to it
            routeTextView = (TextView) view.findViewById(R.id.route_text_view);
            routeTextView.setText(getActivity().getSharedPreferences(SelectRouteActivity.PREFS_FILE, MODE_PRIVATE).getString(SelectRouteActivity.ROUTE_NAME, "DEREE"));

            // Setup the YES button
            yesButton = (Button) view.findViewById(R.id.yes_button);
            yesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Dismiss the dialog
                    dismiss();

                    // Store the selectedRouteID as the routeID in sharedPreferences
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences(SelectRouteActivity.PREFS_FILE, MODE_PRIVATE).edit();
                    editor.putBoolean(SelectRouteActivity.ROUTE_IN_PROGRESS, false);
                    editor.commit();

                    // BackgroundUploadService - Keep tracking even the app is closed
                    Intent service = new Intent(getActivity(), BackgroundUploadService.class);
                    getActivity().stopService(service);

                    clearNotification();

                    getActivity().finish();

                }
            });

            // Setup the NO button
            noButton = (Button) view.findViewById(R.id.no_button);
            noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Edit() ROUTE_IN_PROGRESS - Set it to true
                    getActivity().getSharedPreferences(SelectRouteActivity.PREFS_FILE, MODE_PRIVATE).edit().putBoolean(SelectRouteActivity.ROUTE_IN_PROGRESS, true).commit();

                    dismiss();
                }
            });


            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

            return view;
        }

        public void clearNotification(){
            NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(0);
        }

    }

}