package com.example.android.bustracker_acg_driver.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.bustracker_acg_driver.database.DatabaseContract.RouteStopsEntry;
import com.example.android.bustracker_acg_driver.database.DatabaseContract.RoutesEntry;
import com.example.android.bustracker_acg_driver.database.DatabaseContract.SnappedPointsEntry;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;


/*
    Manages a local database for bus tracker data
 */

public class BusTrackerDBHelper extends SQLiteOpenHelper {

    // TAG
    private static final String TAG = "BusTrackerDBHelper";

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "busTracker.db";


    public BusTrackerDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    /*
        Called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.e(TAG, "onCreate!");

        final String SQL_CREATE_ROUTES_TABLE = "CREATE TABLE " +
                RoutesEntry.TABLE_NAME + " (" +
                RoutesEntry.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                RoutesEntry.COLUMN_NAME_ENG + " TEXT, " +
                RoutesEntry.COLUMN_NAME_GR + " TEXT, " +
                RoutesEntry.COLUMN_SCHOOL + " TEXT" + " );";

        final String SQL_CREATE_ROUTE_STOPS_TABLE = "CREATE TABLE " +
                RouteStopsEntry.TABLE_NAME + " (" +
                RouteStopsEntry.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                RouteStopsEntry.COLUMN_ROUTE_ID + " INTEGER, " +
                RouteStopsEntry.COLUMN_STOP_TIME + " TEXT, " +
                RouteStopsEntry.COLUMN_NAME_OF_STOP_GR + " TEXT, " +
                RouteStopsEntry.COLUMN_NAME_OF_STOP_ENG + " TEXT, " +
                RouteStopsEntry.COLUMN_DESCRIPTION + " TEXT, " +
                RouteStopsEntry.COLUMN_LAT + " REAL, " +
                RouteStopsEntry.COLUMN_LNG + " REAL" + " );";

        final String SQL_CREATE_SNAPPED_POINTS_TABLE = "CREATE TABLE " +
                SnappedPointsEntry.TABLE_NAME + " (" +
                SnappedPointsEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SnappedPointsEntry.COLUMN_ROUTE_ID + " INTEGER, " +
                SnappedPointsEntry.COLUMN_LAT + " REAL, " +
                SnappedPointsEntry.COLUMN_LNG + " REAL, " +
                SnappedPointsEntry.COLUMN_ORIGINAL_INDEX + " TEXT, " +
                SnappedPointsEntry.COLUMN_PLACE_ID + " TEXT" + " );";


        try {
            db.execSQL(SQL_CREATE_ROUTES_TABLE);
            db.execSQL(SQL_CREATE_ROUTE_STOPS_TABLE);
            db.execSQL(SQL_CREATE_SNAPPED_POINTS_TABLE);
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }
    }


    /*
        Called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.e(TAG, "onUpgrade!");

        db.execSQL("DROP TABLE IF EXISTS " + RoutesEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RouteStopsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SnappedPointsEntry.TABLE_NAME);
        onCreate(db);

    }


    /*
        All CRUD(Create, Read, Update, Delete) Operations
        ===== Routes =====
     */

    // Adding new route
    // The addRoute() method accepts Route attributes (we can use RouteDAO object) as parameter.
    // We need to build ContentValues parameters using Route attributes (we can use RouteDAO object).
    // Once we inserted data in database we need to close the database connection.
    public void addRoute(int ID, String nameENG, String nameGR, String school) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RoutesEntry.COLUMN_ID, ID); // ID
        values.put(RoutesEntry.COLUMN_NAME_ENG, nameENG); // name in English
        values.put(RoutesEntry.COLUMN_NAME_GR, nameGR); // name in Greek
        values.put(RoutesEntry.COLUMN_SCHOOL, school); // school

        // Inserting Row
        db.insert(RoutesEntry.TABLE_NAME, null, values);
        // Closing database connection
        db.close();
    }

    // Getting single route
    // The following method getRoute() will read single contact row.
    // It accepts id as parameter and will return the matched row from the database.
    public RouteDAO getRoute(int ID) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(RoutesEntry.TABLE_NAME, new String[]{RoutesEntry.COLUMN_ID,
                        RoutesEntry.COLUMN_NAME_ENG, RoutesEntry.COLUMN_NAME_GR, RoutesEntry.COLUMN_SCHOOL},
                RoutesEntry.COLUMN_ID + "=?",
                new String[]{String.valueOf(ID)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        RouteDAO routeDAO = new RouteDAO(
                Integer.parseInt(cursor.getString(0)), // ID
                cursor.getString(1), // nameENG
                cursor.getString(2), // nameGR
                cursor.getString(3)); // school

        // Closing database connection
        db.close();
        // Closing cursor
        cursor.close();

        // return route
        return routeDAO;
    }

    // Getting All routes
    // getAllRoutesDAO() will return all contacts from database in array list format of RouteDAO class type.
    // You need to write a for loop to go through each contact.
    public ArrayList<RouteDAO> getAllRoutesDAO() {
        ArrayList<RouteDAO> routeDAOList = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + RoutesEntry.TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                RouteDAO routeDAO = new RouteDAO(
                        Integer.parseInt(cursor.getString(0)), // ID
                        cursor.getString(1), // nameENG
                        cursor.getString(2), // nameGR
                        cursor.getString(3)); // school

                // Adding route to list
                routeDAOList.add(routeDAO);
            } while (cursor.moveToNext());
        }

        // Closing database connection
        db.close();
        // Closing cursor
        cursor.close();

        // return route list
        return routeDAOList;
    }

    // Getting All route IDs
    public ArrayList<Integer> getAllRouteIDs() {
        ArrayList<Integer> routeIDs = new ArrayList<>();

        // Select namesGR Query
        String selectQuery = "SELECT " + RoutesEntry.COLUMN_ID + " FROM " + RoutesEntry.TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding route to list
                routeIDs.add(Integer.parseInt(cursor.getString(0)));
            } while (cursor.moveToNext());
        }

        // Closing database connection
        db.close();
        // Closing cursor
        cursor.close();

        return routeIDs;
    }

    // Getting All route names in Greek
    public ArrayList<String> getAllRouteNamesGR() {
        ArrayList<String> routeNamesGR = new ArrayList<>();

        // Select namesGR Query
        String selectQuery = "SELECT " + RoutesEntry.COLUMN_NAME_GR + " FROM " + RoutesEntry.TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding route to list
                routeNamesGR.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        // Closing database connection
        db.close();
        // Closing cursor
        cursor.close();

        return routeNamesGR;
    }


    // Getting route name in Greek by ID
    public String getRouteNameGR_byID(int routeID) {
        String routeNameGR;

        // Select namesGR Query
        String selectQuery = "SELECT " + RoutesEntry.COLUMN_NAME_GR +
                " FROM " + RoutesEntry.TABLE_NAME +
                " WHERE " + RoutesEntry.COLUMN_ID +
                " = " + routeID;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        routeNameGR = cursor.getString(0);

        // Closing database connection
        db.close();
        // Closing cursor
        cursor.close();

        return routeNameGR;
    }

    // Getting routeID by name in Greek
    public int getRouteID_byNameGR(String routeNameGR) {
        int routeID;

        // Select namesGR Query
        String selectQuery = "SELECT " + RoutesEntry.COLUMN_ID +
                " FROM " + RoutesEntry.TABLE_NAME +
                " WHERE " + RoutesEntry.COLUMN_NAME_GR +
                " = '" + routeNameGR + "'";

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        routeID = cursor.getInt(0);

        // Closing database connection
        db.close();
        // Closing cursor
        cursor.close();

        return routeID;
    }


    // Getting All route names in English
    public ArrayList<String> getAllRouteNamesENG() {
        ArrayList<String> routeNamesENG = new ArrayList<>();

        // Select namesENG Query
        String selectQuery = "SELECT " + RoutesEntry.COLUMN_NAME_ENG + " FROM " + RoutesEntry.TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding route to list
                routeNamesENG.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        // Closing database connection
        db.close();
        // Closing cursor
        cursor.close();

        return routeNamesENG;
    }


    // Getting route name in English by ID
    public String getRouteNameENG_byID(int routeID) {
        String routeNameENG;

        // Select namesGR Query
        String selectQuery = "SELECT " + RoutesEntry.COLUMN_NAME_ENG +
                " FROM " + RoutesEntry.TABLE_NAME +
                " WHERE " + RoutesEntry.COLUMN_ID +
                " = " + routeID;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        routeNameENG = cursor.getString(0);

        // Closing database connection
        db.close();
        // Closing cursor
        cursor.close();

        return routeNameENG;
    }

    // Getting routeID by name in English
    public int getRouteID_byNameENG(String routeNameENG) {
        int routeID;

        // Select namesGR Query
        String selectQuery = "SELECT " + RoutesEntry.COLUMN_ID +
                " FROM " + RoutesEntry.TABLE_NAME +
                " WHERE " + RoutesEntry.COLUMN_NAME_ENG +
                " = '" + routeNameENG + "'";

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        routeID = cursor.getInt(0);

        // Closing database connection
        db.close();
        // Closing cursor
        cursor.close();

        return routeID;
    }


    // Getting starting time of each route in an ArrayList
    public ArrayList<String> getStartingTimes() {
        ArrayList<String> startingTimes = new ArrayList<>();

        // Get all route IDs
        ArrayList<Integer> listRouteIDs = getAllRouteIDs();

        // ArrayList with Date objects for the RouteStops - times of each route
        ArrayList<Date> listRouteStopsTimes;

        for (int id : listRouteIDs) {
            listRouteStopsTimes = DatabaseContract.convertStringsToDates(getAllRouteStopTimes(id));
            // Get the minimum Time - so the First Station
            Date minTime = Collections.min(listRouteStopsTimes);
            // Build the Time string
            String time = new StringBuilder()
                    .append(DatabaseContract.pad(minTime.getHours())).append(":")
                    .append(DatabaseContract.pad(minTime.getMinutes())).toString();
            // Add time to startingTimes
            startingTimes.add(time);

            }

        // Return the ArrayList with the Starting Times
        return startingTimes;
    }


    // Getting starting time of each route in an ArrayList
    public ArrayList<LatLng> getEndingLatLngs() {
        ArrayList<LatLng> endingLatLngs = new ArrayList<>();

        // Get all route IDs
        ArrayList<Integer> listRouteIDs = getAllRouteIDs();

        // ArrayList with Date objects for the RouteStops - times of each route
        ArrayList<Date> listRouteStopsTimes;

        for (int id : listRouteIDs) {
            listRouteStopsTimes = DatabaseContract.convertStringsToDates(getAllRouteStopTimes(id));
            // Get the maximum Time - so the Last Station
            Date maxTime = Collections.max(listRouteStopsTimes);
            // Build the Time string
            String time = new StringBuilder()
                    .append(DatabaseContract.pad(maxTime.getHours())).append(":")
                    .append(DatabaseContract.pad(maxTime.getMinutes())).toString();
            // Get the LatLng of the RouteStop with the maximum Time
            endingLatLngs.add(getRouteStopLatLng(id, time));
        }

        // Return the ArrayList with the Starting Times
        return endingLatLngs;
    }



    // Getting routes Count
    // getRoutesCount() will return total number of routes in SQLite database.
    public int getRoutesCount() {
        String countQuery = "SELECT * FROM " + RoutesEntry.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();

        // Closing database connection
        db.close();
        // Closing cursor
        cursor.close();

        // return count
        return count;
    }

    // Updating single route
    // updateRoute() will update single route in database.
    // This method accepts RouteDAO class object as parameter.
    public int updateRoute(RouteDAO route) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RoutesEntry.COLUMN_NAME_ENG, route.getNameENG());
        values.put(RoutesEntry.COLUMN_NAME_GR, route.getNameGR());
        values.put(RoutesEntry.COLUMN_SCHOOL, route.getSchool());

        // updating row
        return db.update(RoutesEntry.TABLE_NAME, values, RoutesEntry.COLUMN_ID + " = ?",
                new String[]{String.valueOf(route.getID())});
    }

    // Deleting single route
    public void deleteRoute(RouteDAO route) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(RoutesEntry.TABLE_NAME, RoutesEntry.COLUMN_ID + " = ?",
                new String[]{String.valueOf(route.getID())});
        db.close();
    }


    /*
        All CRUD(Create, Read, Update, Delete) Operations
        ===== RouteStops =====
     */

    // Adding new routeStop
    // The addRouteStop() method accepts RouteStop attributes (we can use RouteStopDAO object) as parameter.
    // We need to build ContentValues parameters using RouteStop attributes (we can use RouteStopDAO object).
    // Once we inserted data in database we need to close the database connection.
    public void addRouteStop(int ID, int routeID,
                             String stopTime,
                             String nameOfStopGR, String nameOfStopENG,
                             String description,
                             double lat, double lng) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RouteStopsEntry.COLUMN_ID, ID); // ID
        values.put(RouteStopsEntry.COLUMN_ROUTE_ID, routeID); // routeID
        values.put(RouteStopsEntry.COLUMN_STOP_TIME, stopTime); // stopTime
        values.put(RouteStopsEntry.COLUMN_NAME_OF_STOP_GR, nameOfStopGR); // name in Greek
        values.put(RouteStopsEntry.COLUMN_NAME_OF_STOP_ENG, nameOfStopENG); // name in English
        values.put(RouteStopsEntry.COLUMN_DESCRIPTION, description); // description
        values.put(RouteStopsEntry.COLUMN_LAT, lat); // latitude
        values.put(RouteStopsEntry.COLUMN_LNG, lng); // longitude

        // Inserting Row
        db.insert(RouteStopsEntry.TABLE_NAME, null, values);
        // Closing database connection
        db.close();
    }

    // Getting single routeStop
    // The following method getRouteStop() will read single contact row.
    // It accepts id as parameter and will return the matched row from the database.
    public RouteStopDAO getRouteStop(int ID) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(RouteStopsEntry.TABLE_NAME, new String[]{
                        RouteStopsEntry.COLUMN_ID,
                        RouteStopsEntry.COLUMN_ROUTE_ID,
                        RouteStopsEntry.COLUMN_STOP_TIME,
                        RouteStopsEntry.COLUMN_NAME_OF_STOP_GR,
                        RouteStopsEntry.COLUMN_NAME_OF_STOP_ENG,
                        RouteStopsEntry.COLUMN_DESCRIPTION,
                        RouteStopsEntry.COLUMN_LAT,
                        RouteStopsEntry.COLUMN_LNG
                },
                RouteStopsEntry.COLUMN_ID + "=?",
                new String[]{String.valueOf(ID)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        RouteStopDAO routeStop = new RouteStopDAO(
                Integer.parseInt(cursor.getString(0)), // ID
                Integer.parseInt(cursor.getString(1)), // routeID
                cursor.getString(2), // stopTime
                cursor.getString(3), // name in Greek
                cursor.getString(4), // name in English
                cursor.getString(5), // description
                Double.parseDouble(cursor.getString(6)), // latitude
                Double.parseDouble(cursor.getString(7))); // longitude

        // Closing database connection
        db.close();
        // Closing cursor
        cursor.close();

        // return routeStop
        return routeStop;
    }

    // Getting All routeStops
    // getAllRouteStopsDAO() will return all contacts from database
    // in array list format of RouteStopDAO class type.
    // You need to write a for loop to go through each contact.
    public ArrayList<RouteStopDAO> getAllRouteStopsDAO() {
        ArrayList<RouteStopDAO> routeStopDAOList = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT * FROM " + RouteStopsEntry.TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                RouteStopDAO routeStopDAO = new RouteStopDAO(
                        Integer.parseInt(cursor.getString(0)), // ID
                        Integer.parseInt(cursor.getString(1)), // routeID
                        cursor.getString(2), // stopTime
                        cursor.getString(3), // name in Greek
                        cursor.getString(4), // name in English
                        cursor.getString(5), // description
                        Double.parseDouble(cursor.getString(6)), // latitude
                        Double.parseDouble(cursor.getString(7))); // longitude

                // Adding routeStop to list
                routeStopDAOList.add(routeStopDAO);
            } while (cursor.moveToNext());
        }

        // Closing database connection
        db.close();
        // Closing cursor
        cursor.close();

        // return routeStops list
        return routeStopDAOList;
    }


    // Getting All route stop names in Greek

    /**
     * @param routeID
     * @return ArrayList<String> routeStopNamesGR
     */
    public ArrayList<String> getAllRouteStopNamesGR(int routeID) {
        ArrayList<String> routeStopNamesGR = new ArrayList<>();

        // Select namesGR Query
        String selectQuery = "SELECT " + RouteStopsEntry.COLUMN_NAME_OF_STOP_GR +
                " FROM " + RouteStopsEntry.TABLE_NAME +
                " WHERE " + RouteStopsEntry.COLUMN_ROUTE_ID + " = " + routeID;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding route to list
                routeStopNamesGR.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        // Closing database connection
        db.close();
        // Closing cursor
        cursor.close();

        return routeStopNamesGR;
    }


    // Getting All route stop names in English

    /**
     * @param routeID
     * @return ArrayList<String> routeStopNamesENG
     */
    public ArrayList<String> getAllRouteStopNamesENG(int routeID) {
        ArrayList<String> routeStopNamesENG = new ArrayList<>();

        // Select namesENG Query
        String selectQuery = "SELECT " + RouteStopsEntry.COLUMN_NAME_OF_STOP_ENG +
                " FROM " + RouteStopsEntry.TABLE_NAME +
                " WHERE " + RouteStopsEntry.COLUMN_ROUTE_ID + " = " + routeID;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding route to list
                routeStopNamesENG.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        // Closing database connection
        db.close();
        // Closing cursor
        cursor.close();

        return routeStopNamesENG;
    }


    // Getting All route stop times

    /**
     * @param routeID
     * @return ArrayList<String> routeStopTimes
     */
    public ArrayList<String> getAllRouteStopTimes(int routeID) {
        ArrayList<String> routeStopTimes = new ArrayList<>();

        // Select times Query
        String selectQuery = "SELECT " + RouteStopsEntry.COLUMN_STOP_TIME +
                " FROM " + RouteStopsEntry.TABLE_NAME +
                " WHERE " + RouteStopsEntry.COLUMN_ROUTE_ID + " = " + routeID;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding route to list
                routeStopTimes.add(cursor.getString(0).substring(0, 5));
            } while (cursor.moveToNext());
        }

        // Closing database connection
        db.close();
        // Closing cursor
        cursor.close();

        return routeStopTimes;
    }


    // Getting All route stop lat-lng

    /**
     * @param routeID
     * @return ArrayList<LatLng> routeStopTimes
     */
    public ArrayList<LatLng> getAllRouteStopLatLngs(int routeID) {
        ArrayList<LatLng> routeStopLatLngs = new ArrayList<>();

        // Select namesGR Query
        String selectQuery = "SELECT " + RouteStopsEntry.COLUMN_LAT +
                ", " + RouteStopsEntry.COLUMN_LNG +
                " FROM " + RouteStopsEntry.TABLE_NAME +
                " WHERE " + RouteStopsEntry.COLUMN_ROUTE_ID + " = " + routeID;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding route to list
                routeStopLatLngs.add(new LatLng(cursor.getDouble(0), cursor.getDouble(1)));
            } while (cursor.moveToNext());
        }

        // Closing database connection
        db.close();
        // Closing cursor
        cursor.close();

        return routeStopLatLngs;
    }


    // Get the latLng of the RouteStop at stopTime, of the route with id == routeID
    public LatLng getRouteStopLatLng(int routeID, String stopTime){
        LatLng routeStopLatLng = null;

        // Select the LatLng of the last RouteStop Query
        String selectQuery = "SELECT " + RouteStopsEntry.COLUMN_LAT +
                ", " + RouteStopsEntry.COLUMN_LNG +
                " FROM " + RouteStopsEntry.TABLE_NAME +
                " WHERE " + RouteStopsEntry.COLUMN_ROUTE_ID + " = " + routeID +
                " AND " + RouteStopsEntry.COLUMN_STOP_TIME + " = " + "'" + stopTime + ":00'" + ";";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                routeStopLatLng = new LatLng(cursor.getDouble(0), cursor.getDouble(1));
            } while (cursor.moveToNext());
        }


        // Closing database connection
        db.close();
        // Closing cursor
        cursor.close();

        return routeStopLatLng;
    }

    // Getting routeStops Count
    // getRouteStopsCount() will return total number of routeStops in SQLite database.
    public int getRouteStopsCount() {
        String countQuery = "SELECT  * FROM " + RouteStopsEntry.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    // Updating single routeStop
    // updateRouteStop() will update single routeStop in database.
    // This method accepts RouteStopDAO class object as parameter.
    public int updateRouteStop(RouteStopDAO routeStop) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RouteStopsEntry.COLUMN_ROUTE_ID, routeStop.getRouteID());
        values.put(RouteStopsEntry.COLUMN_STOP_TIME, routeStop.getStopTime());
        values.put(RouteStopsEntry.COLUMN_NAME_OF_STOP_GR, routeStop.getNameOfStopGR());
        values.put(RouteStopsEntry.COLUMN_NAME_OF_STOP_ENG, routeStop.getNameOfStopENG());
        values.put(RouteStopsEntry.COLUMN_DESCRIPTION, routeStop.getDescription());
        values.put(RouteStopsEntry.COLUMN_LAT, routeStop.getLat());
        values.put(RouteStopsEntry.COLUMN_LNG, routeStop.getLng());

        // updating row
        return db.update(RouteStopsEntry.TABLE_NAME, values, RouteStopsEntry.COLUMN_ID + " = ?",
                new String[]{String.valueOf(routeStop.getID())});
    }

    // Deleting single routeStop
    public void deleteRouteStop(RouteStopDAO routeStop) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(RouteStopsEntry.TABLE_NAME, RouteStopsEntry.COLUMN_ID + " = ?",
                new String[]{String.valueOf(routeStop.getID())});
        db.close();
    }


    /*
        All CRUD(Create, Read, Update, Delete) Operations
        ===== SnappedPoints =====
     */

    // Adding new routeStop
    // The addSnappedPoint() method accepts SnappedPoint attributes (we can use SnappedPointDAO object) as parameter.
    // We need to build ContentValues parameters using SnappedPoint attributes (we can use RouteStopDAO object).
    // Once we inserted data in database we need to close the database connection.
    public void addSnappedPoint(int routeID,
                                double lat, double lng,
                                String originalIdex,
                                String placeID) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SnappedPointsEntry.COLUMN_ROUTE_ID, routeID); // routeID
        values.put(SnappedPointsEntry.COLUMN_LAT, lat); // latitude
        values.put(SnappedPointsEntry.COLUMN_LNG, lng); // longitude
        values.put(SnappedPointsEntry.COLUMN_ORIGINAL_INDEX, originalIdex); // original Index
        values.put(SnappedPointsEntry.COLUMN_PLACE_ID, placeID); // place ID

        // Inserting Row
        db.insert(SnappedPointsEntry.TABLE_NAME, null, values);
        // Closing database connection
        db.close();
    }


    // Getting All snapped point lat-lng

    /**
     * @param routeID
     * @return ArrayList<LatLng> snappedPointLatLngs
     */
    public ArrayList<LatLng> getAllSnappedPointLatLngs(int routeID) {
        ArrayList<LatLng> snappedPointLatLngs = new ArrayList<>();

        // Select namesGR Query
        String selectQuery = "SELECT " + SnappedPointsEntry.COLUMN_LAT +
                ", " + SnappedPointsEntry.COLUMN_LNG +
                " FROM " + SnappedPointsEntry.TABLE_NAME +
                " WHERE " + SnappedPointsEntry.COLUMN_ROUTE_ID + " = " + routeID;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding route to list
                snappedPointLatLngs.add(new LatLng(cursor.getDouble(0), cursor.getDouble(1)));
            } while (cursor.moveToNext());
        }

        // Closing database connection
        db.close();
        // Closing cursor
        cursor.close();

        return snappedPointLatLngs;
    }


    // Getting All snappedPoints
    // getAllSnappedPointsDAO() will return all contacts from database
    // in array list format of SnappedPointDAO class type.
    // You need to write a for loop to go through each contact.
    public ArrayList<SnappedPointDAO> getAllSnappedPointsDAO() {
        ArrayList<SnappedPointDAO> snappedPointDAOList = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT * FROM " + SnappedPointsEntry.TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                SnappedPointDAO snappedPointDAO = new SnappedPointDAO(
                        Integer.parseInt(cursor.getString(0)), // ID
                        Integer.parseInt(cursor.getString(1)), // routeID
                        Double.parseDouble(cursor.getString(2)), // latitude
                        Double.parseDouble(cursor.getString(3)), // longitude
                        cursor.getString(4), // stopTime
                        cursor.getString(5)); // name in Greek
                // Adding routeStop to list
                snappedPointDAOList.add(snappedPointDAO);
            } while (cursor.moveToNext());
        }

        // Closing database connection
        db.close();
        // Closing cursor
        cursor.close();

        // return snappedPointDAO list
        return snappedPointDAOList;
    }


}

