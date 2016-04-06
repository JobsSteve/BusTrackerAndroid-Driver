package com.example.android.bustracker_acg_driver;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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

import com.example.android.bustracker_acg_driver.database.BusTrackerDBHelper;

import java.util.ArrayList;

/**
 * Created by giorgos on 4/4/2016.
 */


public class SelectRouteActivity extends AppCompatActivity {

    // LOG TAG
    private final String TAG = "SelectRouteActivity";
    // SharedPreferences file name
    public static final String PREFS_FILE = "DriverPreferencesFile";
    // sharedPreference key for language preference
    public static final String LANGUAGE = "LanguagePreference";
    // supported languages
    public static final String GR = "GR";
    public static final String ENG = "ENG";
    // Selected route ID
    public static final String ROUTE_ID = "selectedRouteID";
    // SharedPreferences
    SharedPreferences sharedPreferences;
    static SharedPreferences.Editor editor;
    // The StartingPoint object of the selected route
    protected static StartingPoint selectedStartingPoint;


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

        // Initialize and execute the AsyncTask to set the adapter
        SetAdapterAsyncTask setAdapterAsyncTask = new SetAdapterAsyncTask();
        setAdapterAsyncTask.execute();
    }


    private class SetAdapterAsyncTask extends
            AsyncTask<Void, Void, SelectRouteListAdapter> {

        // LOG TAG
        private static final String TAG = "SetAdapterAsyncTask";

        @Override
        protected SelectRouteListAdapter doInBackground(Void... params) {


            // Check SharedPreferences for the language
            sharedPreferences = getSharedPreferences(PREFS_FILE, Activity.MODE_PRIVATE);
            // Get the language
            String language = sharedPreferences.getString(LANGUAGE, GR);
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


            /*
             * Preparing the list data
             */
            if (language.equals(GR)) {
                listRoutes = db.getAllRouteNamesGR();
            } else {
                listRoutes = db.getAllRouteNamesENG();
            }

            listStartingTimes = db.getStartingTimes();
            listRouteIDs = db.getAllRouteIDs();

            // Get the size of the listRoutes
            int routesSize = listRoutes.size();
            // Initialize the listStartingPoint
            listStartingPoints = new ArrayList<>();
            for (int i = 0; i < routesSize; i ++){
                Log.e(TAG, "i: " + i);
                listStartingTimes = db.getStartingTimes();
                // Add StartingPoint - Objects to listStartingPoints
                listStartingPoints.add(new StartingPoint(listRouteIDs.get(i), listRoutes.get(i), listStartingTimes.get(i)));
            }

            // Create the SelectRouteList Adapter
            selectRouteListAdapter = new SelectRouteListAdapter(SelectRouteActivity.this, R.layout.select_route_list_item, listStartingPoints);

            // Return the SelectRouteListAdapter
            return selectRouteListAdapter;
        }

        @Override
        protected void onPostExecute(SelectRouteListAdapter selectRouteListAdapter) {
            super.onPostExecute(selectRouteListAdapter);

            // Adapter must be set after the header , footer
            listView.setAdapter(selectRouteListAdapter);

            // Add a listener
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.e(TAG, "onItemClick: " + position + " = routeID");

                    selectedStartingPoint = (StartingPoint)parent.getItemAtPosition(position);
                    Log.e(TAG, selectedStartingPoint.getRouteName());

                    ConfirmRouteDialogFragment confirmRouteDialogFragment = new ConfirmRouteDialogFragment();
                    confirmRouteDialogFragment.show(getSupportFragmentManager(), "ConfirmRoute");

                }
            });

        }
    }

    // Dialog Fragment for route confirmation
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

            // Find the TestView and setText to it
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

                    // Store the selectedRouteID as the routeID in sharedPreferences
                    SelectRouteActivity.editor = getActivity().getSharedPreferences(PREFS_FILE, MODE_PRIVATE).edit();
                    editor.putInt(ROUTE_ID, selectedRouteID);
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
