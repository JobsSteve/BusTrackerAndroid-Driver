package com.example.android.bustracker_acg_driver.network_connection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * This class can be used to check the network state
 * of the device at any moment.
 */
public class NetworkUtility {

    // The WIFI data connection
    public static int TYPE_WIFI = 1;
    // The Mobile data connection
    public static int TYPE_MOBILE = 2;
    // No Connection
    public static int TYPE_NOT_CONNECTED = 0;


    /**
        Returns one of the above connectivity status
     */
    public static int getConnectivityStatus(Context context) {
        /**
            ConnectivityManager : Class that answers queries
            about the state of network connectivity.

            Get an instance of this class by calling:
            context.getSystemService(Context.CONNECTIVITY_SERVICE)
         */
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get the active network, if there is one
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    public static String getConnectivityStatusString(Context context) {
        int conn = getConnectivityStatus(context);
        String status = null;
        if (conn == TYPE_WIFI) {
            status = "Wifi enabled";
        } else if (conn == TYPE_MOBILE) {
            status = "Mobile data enabled";
        } else if (conn == TYPE_NOT_CONNECTED) {
            status = "Not connected to Internet";
        }
        return status;
    }

}
