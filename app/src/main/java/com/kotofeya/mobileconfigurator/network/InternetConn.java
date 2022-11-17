package com.kotofeya.mobileconfigurator.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Logger;

public class InternetConn {
    public static final String TAG = InternetConn.class.getSimpleName();
    private static final ConnectivityManager mConnectivityManager =
            (ConnectivityManager) App.get().getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

    public static boolean hasInternetConnection() {
        final Network network = mConnectivityManager.getActiveNetwork();
        final NetworkCapabilities capabilities = mConnectivityManager .getNetworkCapabilities(network);
        boolean hasConnection = (capabilities != null &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED));
        Logger.d(Logger.INTERNET_CONN_LOG, "has internet connection: " + hasConnection);
        return hasConnection;
    }
}
