package com.kotofeya.mobileconfigurator;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.InetAddresses;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.LocalOnlyHotspotReservation;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

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

    public String getDeviceIp() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            Logger.d(Logger.WIFI_LOG, "network interfaces: " + interfaces);

            for (NetworkInterface intf : interfaces) {
//                Logger.d(Logger.WIFI_LOG, "network interface adresses: " + intf.getInterfaceAddresses());
//                Logger.d(Logger.WIFI_LOG, "inet adresses: " + Collections.list(intf.getInetAddresses()));
//                Logger.d(Logger.WIFI_LOG, "isub int: " + Collections.list(intf.getSubInterfaces()));

                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    String sAddr = addr.getHostAddress();
                    if (sAddr.startsWith("192")) {
                            Logger.d(Logger.WIFI_LOG, "addr: " + sAddr);
                                return sAddr;
                    }
                }
            }
        } catch (Exception ex) {

        }
        return null;
    }
}
