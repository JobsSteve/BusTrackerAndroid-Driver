package com.example.android.bustracker_acg_driver.database;

/*
 * Created by giorgos on 3/19/2016.
 * A snappedPoint Data Access Object
 * (so we can manipulate the result of a select query)
 */

public class SnappedPointDAO {

    // ID - routeStopID
    private int ID;
    // routeID
    private int routeID;
    // latitude
    private double lat;
    // longitude
    private double lng;
    // originalIndex
    private String originalIndex;
    // placeID
    private String placeID;

    /*
        Constructors
    */
    // Constructor: Empty
    public SnappedPointDAO(){}

    // Constructor: ID, routeID, stopTime, nameOfStopGR, nameOfStopENG,
    // description, lat, lng
    public SnappedPointDAO(int ID, int routeID,
                           double lat, double lng,
                           String originalIndex, String placeID){
        this.ID = ID;
        this.routeID = routeID;
        this.lat = lat;
        this.lng = lng;
        this.originalIndex = originalIndex;
        this.placeID = placeID;
    }

    /*
        Setters & Getters
    */
    public int getID(){
        return this.ID;
    }

    public void setID(int ID){
        this.ID = ID;
    }

    public int getRouteID(){
        return this.routeID;
    }

    public void setRouteID(int routeID){
        this.routeID = routeID;
    }

    public double getLat(){
        return this.lat;
    }

    public void setLat(double lat){
        this.lat = lat;
    }

    public double getLng(){
        return this.lng;
    }

    public void setLng(double lng){
        this.lng = lng;
    }

    public String getOriginalIndex(){
        return this.originalIndex;
    }

    public void setOriginalIndex(String originalIndex){
        this.originalIndex = originalIndex;
    }

    public String getPlaceID(){
        return this.placeID;
    }

    public void setPlaceID(String placeID){
        this.placeID = placeID;
    }

}

