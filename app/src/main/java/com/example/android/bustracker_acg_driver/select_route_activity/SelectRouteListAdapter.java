package com.example.android.bustracker_acg_driver.select_route_activity;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.android.bustracker_acg_driver.R;

import java.util.ArrayList;

public class SelectRouteListAdapter extends ArrayAdapter<StartingPoint> {

    Context context;
    int layoutResourceID;
    ArrayList<StartingPoint> startingPointData;

    public SelectRouteListAdapter(Context context, int layoutResourceID, ArrayList<StartingPoint> startingPointData) {
        super(context, layoutResourceID, startingPointData);
        this.context = context;
        this.layoutResourceID = layoutResourceID;
        this.startingPointData = startingPointData;
    }

    // This method will be called for every item in the ListView
    // to create views with their properties set as we want.
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null){
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceID, parent, false);
        }

        TextView timeTextView = (TextView) convertView.findViewById(R.id.list_item_start_time);
        TextView routeTextView = (TextView) convertView.findViewById(R.id.list_item_route);

        StartingPoint startingPoint = startingPointData.get(position);

        timeTextView.setText(startingPoint.startingTime);
        routeTextView.setText(startingPoint.routeName);

        return convertView;
    }

}
