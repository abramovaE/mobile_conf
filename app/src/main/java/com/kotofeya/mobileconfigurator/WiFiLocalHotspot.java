package com.kotofeya.mobileconfigurator;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WiFiLocalHotspot {
    public static WiFiLocalHotspot getInstance() {
        return instance;
    }

    private static WiFiLocalHotspot instance = new WiFiLocalHotspot();

    private WiFiLocalHotspot() {
    }

    private List<String> clients;

    public List<String> getClientList(String deviceIp) {
        clients = new ArrayList<>();
        Logger.d(Logger.WIFI_LOG, "isAppHotSpotStarted()");
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
        if(deviceIp !=null) {
            clients.remove(deviceIp);
        }
        return clients;
    }

    public class PingIp implements Runnable{
        private static final int timeout = 5000;
        private String host;
        public PingIp(String host) {
            this.host = host;
        }
        @Override
        public void run() {
            try {
                InetAddress inetAddress = InetAddress.getByName(host);
                if (inetAddress.isReachable(timeout)){
                    Logger.d(Logger.WIFI_LOG, "ping: " + host);
                    clients.add(host);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Logger.d(Logger.WIFI_LOG, "exception: " + e.getLocalizedMessage());
            }
        }
    }
}
