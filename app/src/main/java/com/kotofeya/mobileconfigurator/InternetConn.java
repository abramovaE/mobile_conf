package com.kotofeya.mobileconfigurator;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class InternetConn {
    WifiManager.LocalOnlyHotspotReservation mReservation;
    private static final ConnectivityManager mConnectivityManager = (ConnectivityManager) App.get().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

    public boolean hasInternetConnection() {
        final Network network = mConnectivityManager.getActiveNetwork();
        final NetworkCapabilities capabilities = mConnectivityManager .getNetworkCapabilities(network);
        boolean hasConnection = (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED));
        Logger.d(Logger.INTERNET_CONN_LOG, "has internet connection: " + hasConnection);
        return hasConnection;
    }

    public String getDeviceIp() {
        Logger.d(Logger.INTERNET_CONN_LOG, "get host device ip");
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    String sAddr = addr.getHostAddress();
//                    Logger.d(Logger.INTERNET_CONN_LOG, "host addr: " + sAddr);
                    if (sAddr.startsWith("192")) {
                            Logger.d(Logger.INTERNET_CONN_LOG, "host address: " + sAddr);
                                return sAddr;
                    }
                }
            }
        } catch (Exception ex) {
            Logger.d(Logger.INTERNET_CONN_LOG, "exception: " + ex + ", cause: " + ex.getCause());
        }
        return null;
    }
}
