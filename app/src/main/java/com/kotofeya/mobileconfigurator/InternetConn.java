package com.kotofeya.mobileconfigurator;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

public class InternetConn {
    public static final String TAG = InternetConn.class.getSimpleName();
    private static final ConnectivityManager mConnectivityManager = (ConnectivityManager) App.get().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

    public static boolean hasInternetConnection() {
        final Network network = mConnectivityManager.getActiveNetwork();
        final NetworkCapabilities capabilities = mConnectivityManager .getNetworkCapabilities(network);
        boolean hasConnection = (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED));
        Logger.d(Logger.INTERNET_CONN_LOG, "has internet connection: " + hasConnection);

        return hasConnection;
    }


//    public void isEnabled(){
//        WifiManager wifiManager = (WifiManager) App.get().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        Method method= null;
//        try {
//            method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        }
//        method.invoke(wifiManager, wifi_configuration, true);
//    }

//    public static String getIPAddress(boolean useIPv4) {
//        try {
//            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
//            for (NetworkInterface intf : interfaces) {
//                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
//                for (InetAddress addr : addrs) {
//                    if (!addr.isLoopbackAddress()) {
//                        String sAddr = addr.getHostAddress();
//                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
//                        boolean isIPv4 = sAddr.indexOf(':')<0;
//
//                        if (useIPv4) {
//                            if (isIPv4)
//                                return sAddr;
//                        } else {
//                            if (!isIPv4) {
//                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
//                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (Exception ignored) { } // for now eat exceptions
//        return "";
//    }



//    public static String getLocalIpAddress() {
//        try {
//            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
//                NetworkInterface intf = en.nextElement();
//                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
//                    InetAddress inetAddress = enumIpAddr.nextElement();
//                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
//                        return inetAddress.getHostAddress();
//                    }
//                }
//            }
//        } catch (SocketException ex) {
//            ex.printStackTrace();
//        }
//        return null;
//    }

    public static String getDeviceIp() {
        Logger.d(Logger.INTERNET_CONN_LOG, "getDeviceIp()");
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                InetAddress address = Collections.list(intf.getInetAddresses())
                        .stream().filter(it->it.getHostAddress().startsWith("192")).findAny().orElse(null);
                if(address != null){
                    return address.getHostAddress();
                }
            }
        } catch (SocketException ex) {
            Logger.d(Logger.INTERNET_CONN_LOG, "exception: " + ex + ", cause: " + ex.getCause());
        }
        return null;
    }
}
