package com.kotofeya.mobileconfigurator;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.LocalOnlyHotspotReservation;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

public class InternetConn {
    WifiManager.LocalOnlyHotspotReservation mReservation;
    private static final ConnectivityManager mConnectivityManager = (ConnectivityManager) App.get().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);


    public void bindToNetork() {
        if (mConnectivityManager.getActiveNetwork() != null) {
            mConnectivityManager.bindProcessToNetwork(mConnectivityManager.getActiveNetwork());
        }
    }

    public boolean isInternetEnabled(){
        Network network = mConnectivityManager.getActiveNetwork();
        Logger.d(Logger.INTERNET_CONN_LOG, "interner enabled: " + (network != null));
        return network != null;
    }

    public boolean hasInternetConnection() {
        final Network network = mConnectivityManager.getActiveNetwork();
        final NetworkCapabilities capabilities = mConnectivityManager .getNetworkCapabilities(network);
        boolean hasConnection = (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED));
        Logger.d(Logger.INTERNET_CONN_LOG, "has internet connection: " + hasConnection);
        return hasConnection;
    }




    public void validateHotSpot() {

        String p = Settings.Global.getString(App.get().getContentResolver(), Settings.Global.NETWORK_PREFERENCE);
        Logger.d(Logger.INTERNET_CONN_LOG, "p: " + p);

//        String d = Settings.Global.getString(App.get().getContentResolver(), Settings.Secure.N);
//        Logger.d(Logger.INTERNET_CONN_LOG, "d: " + d);

        String u = Settings.System.getString(App.get().getContentResolver(), Settings.System.NAME);
        Logger.d(Logger.INTERNET_CONN_LOG, "u: " + u);



//        Settings.
//        String m = Settings.Global.getString(App.get().getContentResolver(), Settings.System.EXTRA_SUB_ID);
//        Logger.d(Logger.INTERNET_CONN_LOG, "u: " + m);



//        mConnectivityManager.

//        turnOnHotspot();
//        WifiManager wifiManager = (WifiManager) App.get().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        Log.d("uuu", "ssid: ";


//        LocalOnlyHotspotReservation localOnlyHotspotReservation = new LocalOnlyHotspotReservation().getWifiConfiguration();

//        ConnectivityManager

//        wifiManager.getConnectionInfo();
//        final Network network = mConnectivityManager.getActiveNetwork();
//        final NetworkCapabilities capabilities = mConnectivityManager .getNetworkCapabilities(network);
//        boolean hasConnection = (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED));
//        Logger.d(Logger.INTERNET_CONN_LOG, "validate hotspot: " +  LocalOnlyHotspotReservation().getWifiConfiguration());
//        return hasConnection;
    }

    private void turnOnHotspot() {
        WifiManager manager = (WifiManager) App.get().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        manager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

            @Override
            public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                super.onStarted(reservation);
                Log.d("uuu", "Wifi Hotspot is on now: " + manager.getConnectionInfo().getSSID());
                mReservation = reservation;

//                String deviceName = Settings.Global.getString(.getContentResolver(), Settings.Global.DEVICE_NAME);

//                reservation.getWifiConfiguration().
//                Log.d("uuu", "ssid: " + );

            }

            @Override
            public void onStopped() {
                super.onStopped();
                Log.d("uuu", "onStopped: ");
            }

            @Override
            public void onFailed(int reason) {
                super.onFailed(reason);
                Log.d("uuu", "onFailed: ");
            }
        }, new Handler());
    }



    private void turnOffHotspot() {
        if (mReservation != null) {
            mReservation.close();
        }
    }



}
