package com.example.android.bustracker_acg_driver.database;

import android.provider.BaseColumns;
import android.text.format.Time;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class DatabaseContract {

    // To make it easy to query for the exact date,
    // we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }


    public static ArrayList<Date> convertStringsToDates(ArrayList<String> listStringTimes){
        ArrayList<Date> listDateTimes = new ArrayList<>();

        // SimpleDateFormat
        SimpleDateFormat timeParser = new SimpleDateFormat("HH:mm");

        for (String time : listStringTimes){
            try {
                // Convert String time into SimpleDateFormat - so they are comparable
                Date dateTime = timeParser.parse(time);
                listDateTimes.add(dateTime);
            } catch (ParseException e) {
                // Invalid date was entered
                Log.e("SimpleDateFormat", e.getMessage());
            }
        }

        return listDateTimes;
    }


    /**
     * @param c
     * @return the appropriate String representation of the hour or minute.
     * The pad() method that we called from the updateDisplay()
     * It will prefix a zero to the number if it's a single digit
     */
    public static String pad(int number) {
        if (number >= 10)
            return String.valueOf(number);
        else
            return "0" + String.valueOf(number);
    }

    /*
        Inner class that defines the table contents of the Routes table
     */
    public static final class RoutesEntry implements BaseColumns {

        public static final String TABLE_NAME = "Routes";

        // Column with the ID of the Routes
        public static final String COLUMN_ID = "ID";

        // The name of the route in Greek
        public static final String COLUMN_NAME_GR = "nameGR";

        // The name of the route in English
        public static final String COLUMN_NAME_ENG = "nameENG";

        // The name of the school
        public static final String COLUMN_SCHOOL = "school";

    }

    /*
        Inner class that defines the table contents of the RouteStops table
     */
    public static final class RouteStopsEntry implements BaseColumns {

        public static final String TABLE_NAME = "RouteStops";

        // Column with the ID of the RouteStops
        public static final String COLUMN_ID = "ID";

        // The routeID of the route this stop belongs to
        public static final String COLUMN_ROUTE_ID = "routeID";

        // The time a stop occurs
        public static final String COLUMN_STOP_TIME = "stopTime";

        // The name of the stop in Greek
        public static final String COLUMN_NAME_OF_STOP_GR = "nameOfStopGR";

        // The name of the stop in English
        public static final String COLUMN_NAME_OF_STOP_ENG = "nameOfStopENG";

        // A short description for the stop
        public static final String COLUMN_DESCRIPTION = "description";

        // The stop 's latitude
        public static final String COLUMN_LAT = "lat";

        // The stop 's longitude
        public static final String COLUMN_LNG = "lng";

    }

    /*
        Inner class that defines the table contents of the SnappedPoints table
     */
    public static final class SnappedPointsEntry implements BaseColumns {

        public static final String TABLE_NAME = "SnappedPoints";

        // Column with the auto_increment ID of the SnappedPoint
        public static final String COLUMN_ID = "ID";

        // The routeID of the route this stop belongs to
        public static final String COLUMN_ROUTE_ID = "routeID";

        // The snapped point 's latitude
        public static final String COLUMN_LAT = "lat";

        // The snapped point 's longitude
        public static final String COLUMN_LNG = "lng";

        // The original index
        public static final String COLUMN_ORIGINAL_INDEX = "originalIndex";

        // The placeID
        public static final String COLUMN_PLACE_ID = "placeID";

    }

}

