package com.example.android.bustracker_acg_driver.network_connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by giorgos on 4/7/2016.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    // LOG_TAG
    private static final String TAG = "NetworkChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        String status = NetworkUtility.getConnectivityStatusString(context);
        Toast.makeText(context, status, Toast.LENGTH_SHORT).show();

        Log.e(TAG, status);

    }
}
