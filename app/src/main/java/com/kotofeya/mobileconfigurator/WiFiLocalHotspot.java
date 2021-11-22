package com.kotofeya.mobileconfigurator;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class WiFiLocalHotspot {
    public static final String TAG = "WiFiLocalHotspot";

    public static WiFiLocalHotspot getInstance() {
        return instance;
    }

    private static WiFiLocalHotspot instance = new WiFiLocalHotspot();

    private WiFiLocalHotspot() {
    }

    private List<String> clients;

    public List<String> getClientList(String deviceIp) {
        clients = new ArrayList<>();
        Logger.d(TAG, "isAppHotSpotStarted(), deviceIp: " + deviceIp);

//        ConnectivityManager manager = (ConnectivityManager) App.get().getSystemService(App.get().getApplicationContext().CONNECTIVITY_SERVICE);
//        Network activeNetwork = manager.getActiveNetwork();
//        manager.getNetworkCapabilities(activeNetwork);
////        manager.getActiveNetwork().
//        try {
//            for(InetAddress inetAddress: activeNetwork.getAllByName(deviceIp)){
//                Logger.d(TAG, "inetAddress: " + inetAddress.getAddress());
//
//
//            }
//        } catch (UnknownHostException e) {
//            Logger.d(TAG, "UnknownHostException e");
//            e.printStackTrace();
//        }


//        final boolean[] isStarted = {true};
//        WifiManager manager = (WifiManager) App.get().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        manager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {
//            @Override
//            public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
//                super.onStarted(reservation);
//                Logger.d(Logger.MAIN_LOG, "Wifi Hotspot is on now");
//                isStarted[0] = false;
//            }
//
//            @Override
//            public void onStopped() {
//                super.onStopped();
//                Logger.d(Logger.MAIN_LOG, "onStopped: ");
//            }
//
//            @Override
//            public void onFailed(int reason) {
//                super.onFailed(reason);
//                Logger.d(Logger.MAIN_LOG, "onFailed: " + reason);

        String host = deviceIp.substring(0, deviceIp.lastIndexOf("."));
        Logger.d(Logger.WIFI_LOG, "device ip substring: " + host);

        ExecutorService executorService = Executors.newFixedThreadPool(300);
        CompletableFuture<Void>[] futures = new CompletableFuture[256];
        for (int i = 0; i < 256; i++) {
            futures[i] = CompletableFuture.runAsync(new PingIp(host + "." + i), executorService);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        CompletableFuture.allOf(futures)
                .thenRun(() -> {
                    Logger.d(Logger.WIFI_LOG, "all clients pinged, return: " + clients);
                    executorService.shutdown();
                });

//            }
//        }, new Handler());
        if (deviceIp != null) {
            clients.remove(deviceIp);
        }
        return clients;
    }

    public void getClientList2() {
        Logger.d(TAG, "getClientList");
        int macCount = 0;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null) {
                    // Basic sanity check
                    String mac = splitted[3];
                    System.out.println("Mac : Outside If " + mac);
                    if (mac.matches("..:..:..:..:..:..")) {
                        macCount++;
                /* ClientList.add("Client(" + macCount + ")");
                 IpAddr.add(splitted[0]);
                 HWAddr.add(splitted[3]);
                 Device.add(splitted[5]);*/

//                        System.out.println("Mac : "+ mac + " IP Address : "+splitted[0] );
//                        System.out.println("Mac_Count  " + macCount + " MAC_ADDRESS  "+ mac);
                        Logger.d(TAG, "ip  " + splitted[0]);

                    }
            /* for (int i = 0; i < splitted.length; i++)
                 System.out.println("Address "+ splitted[i]);*/

                }
            }
        } catch (Exception e) {
            Logger.d(TAG, "exception  " + e.getMessage());

        }
    }


    public class PingIp implements Runnable {
        private static final int timeout = 5000;
        private String host;

        public PingIp(String host) {
            this.host = host;
        }

        @Override
        public void run() {
            try {
                InetAddress inetAddress = InetAddress.getByName(host);
                if (inetAddress.isReachable(timeout)) {
                    Logger.d(Logger.WIFI_LOG, "ping: " + host);
                    clients.add(host);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Logger.d(Logger.WIFI_LOG, "exception: " + e.getLocalizedMessage());
            }
        }
    }

    public void getClientList() {
        Logger.d(TAG, "getClientList");





        Pattern macPattern = Pattern.compile("..:..:..:..:..:..");

        BufferedReader br = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Process ipProc = Runtime.getRuntime().exec("ip neighbor");
                ipProc.waitFor();
                if (ipProc.exitValue() != 0) {
                    throw new Exception("Unable to access ARP entries");
                }

                br = new BufferedReader(new InputStreamReader(ipProc.getInputStream(), "UTF-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] neighborLine = line.split("\\s+");
                    if (neighborLine.length <= 4) {
                        continue;
                    }
                    String ip = neighborLine[0];
                    final String hwAddr = neighborLine[4];

                    InetAddress addr = InetAddress.getByName(ip);
                    if (addr.isLinkLocalAddress() || addr.isLoopbackAddress()) {
                        continue;
                    }
                    String macAddress = neighborLine[4];
                    String state = neighborLine[neighborLine.length - 1];

                    Logger.d(TAG, "state: " + state);
//                if (!NEIGHBOR_FAILED.equals(state) && !NEIGHBOR_INCOMPLETE.equals(state)) {
                    boolean isReachable = false;
                    try {
                        isReachable = InetAddress.getByName(ip).isReachable(5000);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Logger.d(TAG, "IOException: " + e.getLocalizedMessage());

                    }
                    if (isReachable) {
                        Logger.d(TAG, "isReac: " + ip);
//                    }
//                        result.add(new WifiClient(ip, hwAddr));
                    }
                }
//            }
            } else {
                br = new BufferedReader(new FileReader("/proc/net/arp"));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(" +");
                    if (parts.length < 6) {
                        continue;
                    }
                    final String ipAddr = parts[0];
                    final String hwAddr = parts[3];
                    if (!ipAddr.equalsIgnoreCase("IP")) {
                        boolean isReachable = false;
                        try {
                            isReachable = InetAddress.getByName(ipAddr).isReachable(5000);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (isReachable) {
                            Logger.d(TAG, "isReac: " + ipAddr);

//                    }
//                        result.add(new WifiClient(ipAddr, hwAddr));
                        }
                    }

                }
            }
        } catch (Exception e) {
            Logger.d(TAG, "exc, e: " + e);

        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                Logger.d(TAG, "ioexc, e: " + e);
            }
        }

    }
}
