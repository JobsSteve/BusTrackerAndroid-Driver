package com.example.android.bustracker_acg_driver;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.example.android.bustracker_acg_driver.database.BusTrackerDBHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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


public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener, OnMapReadyCallback {

    // LOG TAG
    protected static final String TAG = "MainActivity";
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set a Toolbar to replace the ActionBar/AppBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher);

        // Check if there are arguments passed to the fragment
        Bundle bundleExtras = getIntent().getExtras();
        if (bundleExtras != null) {
            routeID = bundleExtras.getInt(SelectRouteActivity.ROUTE_ID);

            if (routeID != 0){
                // BackgroundUploadService - Keep tracking even the app is closed
//                Intent service = new Intent(this, BackgroundUploadService.class);
//                startService(service);
            } else {
                routeID = getSharedPreferences(SelectRouteActivity.PREFS_FILE, Activity.MODE_PRIVATE).getInt(SelectRouteActivity.ROUTE_ID, 0);
            }

            PrepareDataAsyncTask prepareDataAsyncTask = new PrepareDataAsyncTask();
            prepareDataAsyncTask.execute();

            Log.e(TAG, "selectedRouteID in main: " + routeID);

        }

        buildGoogleApiClient();
        


//        // OPEN THE GPS
//        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        boolean enabled = locationManager
//                .isProviderEnabled(LocationManager.GPS_PROVIDER);
//
//        // check if enabled and if not send user to the GSP settings
//        // Better solution would be to display a dialog and suggesting to
//        // go to the settings
//        if (!enabled) {
//            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            startActivity(intent);
//        }

        // Find the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.driver_map);
        // A googleMap must be acquired using getMapAsync(OnMapReadyCallback).
        // This class automatically initializes the maps system and the view.
        mapFragment.getMapAsync(this);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause ========");
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
        mLocationRequest = LocationRequest.create();

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationRequest.setInterval(1000);

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

        if (gMapReady){
            // Add a marker and Fly camera there
//            setMarker(mPosition);
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

    /*
        Add a marker to
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


    /*
        AsyncTask to prepare the array lists with the information for the map
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
                // get the language
                String language = sharedPreferences.getString(SelectRouteActivity.LANGUAGE, SelectRouteActivity.GR);
                // Database Helper
                BusTrackerDBHelper db = new BusTrackerDBHelper(MainActivity.this);

                if (language.equals(SelectRouteActivity.GR)) {
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
            } catch (Exception e){
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
            } catch (Exception e){
                moveTo(ATHENS);

                Log.e(TAG, "NullPointerException: " + e.getMessage());
            }


        }

    }



}
