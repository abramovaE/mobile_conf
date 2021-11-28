package com.kotofeya.mobileconfigurator.hotspot;

import com.kotofeya.mobileconfigurator.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WiFiLocalHotspot {
    public DeviceScanListener deviceScanListener;
    private static final String TAG = "WiFiLocalHotspot";
    private static WiFiLocalHotspot instance;
    private boolean isScanning = false;
    private List<String> clients;

    public static synchronized WiFiLocalHotspot getInstance() {
        if(instance == null){
            instance = new WiFiLocalHotspot();
        }
        return instance;
    }

    private WiFiLocalHotspot() {}

    public void updateClientList(String deviceIp, DeviceScanListener deviceScanListener){
        Logger.d(TAG, "updateClientList(String deviceIp), deviceIp: " + deviceIp);
        this.deviceScanListener = deviceScanListener;
        if(!isScanning){
            pingClients(deviceIp);
        }
    }

    // deviceIp != null
    private void pingClients(String deviceIp) {
        isScanning = true;
        Logger.d(TAG, "pingClients(String deviceIp), deviceIp: " + deviceIp);
        clients = new ArrayList<>();
        String host = deviceIp.substring(0, deviceIp.lastIndexOf("."));
        Logger.d(TAG, "device ip substring: " + host);
        ExecutorService executorService = Executors.newFixedThreadPool(256);
        CompletableFuture<Void>[] futures = new CompletableFuture[256];
        for (int i = 0; i < 256; i++) {
            futures[i] = CompletableFuture.runAsync(new PingIp(host + "." + i), executorService);
        }
        CompletableFuture.allOf(futures)
                .thenRun(() -> {
                    Logger.d(TAG, "all clients pinged, return: " + clients);
                    executorService.shutdown();
                    isScanning = false;
                    clients.remove(deviceIp);
                    deviceScanListener.scanFinished(clients);
                });
    }

    private class PingIp implements Runnable {
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
                    Logger.d(TAG, "ping: " + host);
                    clients.add(host);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Logger.d(TAG, "exception: " + e.getLocalizedMessage());
            }
        }
    }
}