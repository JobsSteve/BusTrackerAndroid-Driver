package com.example.android.bustracker_acg_driver.database;

/*
 * Created by giorgos on 3/19/2016.
 * A routeStop Data Access Object
 * (so we can manipulate the result of a select query)
 */


public class RouteStopDAO {

    // ID - routeStopID
    private int ID;
    // routeID
    private int routeID;
    // stop time
    private String stopTime;
    // name of stop in Greek
    private String nameOfStopGR;
    // name of stop in English
    private String nameOfStopENG;
    // description
    private String description;
    // latitude
    private double lat;
    // longitude
    private double lng;


    /*
        Constructors
    */
    // Constructor: Empty
    public RouteStopDAO(){}

    // Constructor: ID, routeID, stopTime, nameOfStopGR, nameOfStopENG,
    // description, lat, lng
    public RouteStopDAO(int ID, int routeID,
                        String stopTime,
                        String nameOfStopGR, String nameOfStopENG,
                        String description,
                        double lat, double lng){
        this.ID = ID;
        this.routeID = routeID;
        this.stopTime = stopTime;
        this.nameOfStopGR = nameOfStopGR;
        this.nameOfStopENG = nameOfStopENG;
        this.description = description;
        this.lat = lat;
        this.lng = lng;
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

    public String getStopTime(){
        return this.stopTime;
    }

    public void setStopTime(String stopTime){
        this.stopTime = stopTime;
    }

    public String getNameOfStopGR(){
        return this.nameOfStopGR;
    }

    public void setNameOfStopGR(String nameOfStopGR){
        this.nameOfStopGR = nameOfStopGR;
    }

    public String getNameOfStopENG(){
        return this.nameOfStopENG;
    }

    public void setNameOfStopENG(String nameOfStopENG){
        this.nameOfStopENG = nameOfStopENG;
    }

    public String getDescription(){
        return this.description;
    }

    public void setDescription(String description){
        this.description = description;
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

}

