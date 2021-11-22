package com.kotofeya.mobileconfigurator;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

public class InternetConn {
    private static final ConnectivityManager mConnectivityManager = (ConnectivityManager) App.get().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

    public boolean hasInternetConnection() {
        final Network network = mConnectivityManager.getActiveNetwork();
        final NetworkCapabilities capabilities = mConnectivityManager .getNetworkCapabilities(network);
        boolean hasConnection = (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED));
        Logger.d(Logger.INTERNET_CONN_LOG, "has internet connection: " + hasConnection);
        return hasConnection;
    }

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
    }

    public String getDeviceIp() {
        ConnectivityManager manager = (ConnectivityManager) App.get().getSystemService(Context.CONNECTIVITY_SERVICE);
        WifiManager wifiManager = (WifiManager) App.get().getSystemService(Context.WIFI_SERVICE);
        Network network = manager.getActiveNetwork();



//        Logger.d(Logger.INTERNET_CONN_LOG, "links: " + wifiManager.getConnectionInfo().);

        Logger.d(Logger.INTERNET_CONN_LOG, "network: " + wifiManager.getConnectionInfo().getIpAddress());
//        try {
//            InetAddress inetAddress = InetAddress.getLocalHost();
//            Logger.d(Logger.INTERNET_CONN_LOG, "ipaddr: " );
//
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }

        Logger.d(Logger.INTERNET_CONN_LOG, "get host device ip");
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {

                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {

                    String sAddr = addr.getHostAddress();
                    Logger.d(Logger.INTERNET_CONN_LOG, "host addr: " + sAddr);
                    Logger.d(Logger.INTERNET_CONN_LOG, "host addr isLoopBAck: " + addr.isLoopbackAddress());
                    Logger.d(Logger.INTERNET_CONN_LOG, "host addr isLink: " + addr.isLinkLocalAddress());
//                    Logger.d(Logger.INTERNET_CONN_LOG, "host addr isAny: " + InetAddress.getLocalHost());
//                    Logger.d(Logger.INTERNET_CONN_LOG, "host addr canonical: " + addr.getCanonicalHostName());




                    if (sAddr.startsWith("192")) {
//                        Logger.d(Logger.INTERNET_CONN_LOG, "host address: " + sAddr);
//                        Logger.d(Logger.INTERNET_CONN_LOG, "host address: " + network.getAllByName(sAddr).length);
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
