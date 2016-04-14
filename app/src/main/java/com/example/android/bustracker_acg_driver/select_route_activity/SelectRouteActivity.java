package com.example.android.bustracker_acg_driver.select_route_activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.bustracker_acg_driver.MainActivity;
import com.example.android.bustracker_acg_driver.R;
import com.example.android.bustracker_acg_driver.database.BusTrackerDBHelper;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Locale;

public class SelectRouteActivity extends AppCompatActivity {

    // LOG TAG
    private final String TAG = "SelectRouteActivity";
    // SharedPreferences file name
    public static final String PREFS_FILE = "DriverPreferencesFile";
    // Selected route ID
    public static final String ROUTE_ID = "selectedRouteID";
    // Selected route Name
    public static final String ROUTE_NAME = "selectedRouteName";
    // Selected route's end latitude
    public static final String END_LAT = "Latitude";
    // Selected route's end longitude
    public static final String END_LNG = "Longitude";
    // Selected route in progress
    public static final String ROUTE_IN_PROGRESS = "selectedRouteInProgress";
    // SharedPreferences
    SharedPreferences sharedPreferences;
    static SharedPreferences.Editor editor;
    // The StartingPoint object of the selected route
    protected static StartingPoint selectedStartingPoint;
    // Ending LatLngs ArrayList
    ArrayList<LatLng> listEndingLatLngs;
    // The LatLng of the last station of the selected route
    protected static LatLng selectedEndingLatLng;
    // List View
    private ListView listView;
    // View for the List View Header
    private View listHeaderView;
    // View for the List View Footer
    private View listFooterView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_route);

        // Find the listView
        listView = (ListView) findViewById(R.id.list_view);
        // Initialize the list header and footer
        listHeaderView = getLayoutInflater().inflate(R.layout.select_route_list_header, null);
        listFooterView = getLayoutInflater().inflate(R.layout.select_route_list_footer, null);
        // Add them to the listView
        listView.addHeaderView(listHeaderView, null, false);
        listView.addFooterView(listFooterView, null, false);

        // Adapter must be set after the header , footer
        listView.setAdapter(getSelectRouteListAsapter());

        // Add a listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, "onItemClick: " + position + " = routeID");

                for (int i = 0; i < listEndingLatLngs.size(); i++) {
                    Log.e(TAG, listEndingLatLngs.get(i).toString());
                }

                selectedStartingPoint = (StartingPoint) parent.getItemAtPosition(position);
                selectedEndingLatLng = listEndingLatLngs.get(position - 1);
                Log.e(TAG, selectedStartingPoint.getRouteName() + " endingLatLng: " + selectedEndingLatLng);

                ConfirmRouteDialogFragment confirmRouteDialogFragment = new ConfirmRouteDialogFragment();
                confirmRouteDialogFragment.show(getSupportFragmentManager(), "ConfirmRoute");

            }
        });


    }


    public SelectRouteListAdapter getSelectRouteListAsapter(){
        // Check SharedPreferences for the language
        sharedPreferences = getSharedPreferences(PREFS_FILE, Activity.MODE_PRIVATE);
        // Select Route List Adapter
        SelectRouteListAdapter selectRouteListAdapter;
        // Routes ArrayList
        ArrayList<String> listRoutes;
        // Starting Times ArrayList
        ArrayList<String> listStartingTimes;
        // Route IDs ArrrayList
        ArrayList<Integer> listRouteIDs;
        // StartingPoint Objects ArrayList
        ArrayList<StartingPoint> listStartingPoints;
        // Database Helper
        BusTrackerDBHelper db = new BusTrackerDBHelper(SelectRouteActivity.this);

        /**
         * Preparing the list data
         */

        if ( Locale.getDefault().getDisplayLanguage().equals("Ελληνικά") ) {
            listRoutes = db.getAllRouteNamesGR();
        } else {
            listRoutes = db.getAllRouteNamesENG();
        }

        listStartingTimes = db.getStartingTimes();
        listEndingLatLngs = db.getEndingLatLngs();
        listRouteIDs = db.getAllRouteIDs();

        // Get the size of the listRoutes
        int routesSize = listRoutes.size();
        // Initialize the listStartingPoint
        listStartingPoints = new ArrayList<>();
        for (int i = 0; i < routesSize; i ++){
            listStartingTimes = db.getStartingTimes();
            // Add StartingPoint - Objects to listStartingPoints
            listStartingPoints.add(new StartingPoint(listRouteIDs.get(i), listRoutes.get(i), listStartingTimes.get(i)));
        }

        // Create the SelectRouteList Adapter
        selectRouteListAdapter = new SelectRouteListAdapter(SelectRouteActivity.this, R.layout.select_route_list_item, listStartingPoints);

        return selectRouteListAdapter;

    }








    /**
     *  Dialog Fragment for route confirmation
     */
    public static class ConfirmRouteDialogFragment extends DialogFragment {

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

            View view = inflater.inflate(R.layout.dialog_fragment_confirm_route, null, false);

            // Find the TextView and setText to it
            routeTextView = (TextView) view.findViewById(R.id.route_text_view);
            routeTextView.setText(selectedStartingPoint.getRouteName());

            // Setup the YES button
            yesButton = (Button) view.findViewById(R.id.yes_button);
            yesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    dismiss();

                    //The selected position is:
                    int selectedRouteID = selectedStartingPoint.getRouteID();

                    // Store the information we need in sharedPreferences
                    SelectRouteActivity.editor = getActivity().getSharedPreferences(PREFS_FILE, MODE_PRIVATE).edit();
                    editor.putInt(ROUTE_ID, selectedRouteID);
                    editor.putString(ROUTE_NAME, selectedStartingPoint.getRouteName());
                    editor.putString(END_LAT, String.valueOf(selectedEndingLatLng.latitude));
                    editor.putString(END_LNG, String.valueOf(selectedEndingLatLng.longitude));
                    editor.putBoolean(ROUTE_IN_PROGRESS, true);
                    editor.commit();


                    // Create an Intent
                    Intent mainActivityIntent = new Intent(getContext() , MainActivity.class);
                    // Create bundle and put the selectedStationPoint routeID
                    Bundle bundleExtras = new Bundle();
                    bundleExtras.putInt(SelectRouteActivity.ROUTE_ID, selectedRouteID);
                    // Put the bundle into the intent as extras
                    mainActivityIntent.putExtras(bundleExtras);
                    // Start the Activity
                    startActivity(mainActivityIntent);

                }
            });

            // Setup the NO button
            noButton = (Button) view.findViewById(R.id.no_button);
            noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });


            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

            return view;
        }


    }



}
