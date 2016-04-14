package com.example.android.bustracker_acg_driver.select_route_activity;

/*
 * This Object consists of the RouteNames and the Starting time of each route
 * and it is used from the SelectRouteListAdapter
 */


public class StartingPoint {

    // Route ID
    public int routeID;
    // Route Name
    public String routeName;
    // Route Starting Time
    public String startingTime;

    public StartingPoint(int routeID, String routeName, String startingTime){
        this.routeID = routeID;
        this.routeName = routeName;
        this.startingTime = startingTime;
    }

    private void setRouteID(int routeID){
        this.routeID = routeID;
    }

    private void setStartingTime(String startingTime){
        this.startingTime = startingTime;
    }

    private void setRoute(String routeName){
        this.routeName = routeName;
    }

    public int getRouteID(){
        return this.routeID;
    }

    public String getStartingTime(){
        return this.startingTime;
    }

    public String getRouteName(){
        return this.routeName;
    }

}

