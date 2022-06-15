package com.kotofeya.mobileconfigurator.hotspot;

import com.kotofeya.mobileconfigurator.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class WiFiLocalHotspot {
    public DeviceScanListener deviceScanListener;
    private static final String TAG = WiFiLocalHotspot.class.getSimpleName();
    private static WiFiLocalHotspot instance;
    private boolean isScanning = false;
    private List<String> clients;
    private boolean isNeedPollAfterPing;

    public static synchronized WiFiLocalHotspot getInstance() {
        if(instance == null){
            instance = new WiFiLocalHotspot();
        }
        return instance;
    }

    private WiFiLocalHotspot() {}

    public void updateClientList(String deviceIp, DeviceScanListener deviceScanListener, boolean isNeedPollAfterPing){
        Logger.d(TAG, "updateClientList(), deviceIp: " + deviceIp);
        this.deviceScanListener = deviceScanListener;
        this.isNeedPollAfterPing = isNeedPollAfterPing;
        if(!isScanning){
            List<String> clients = getConnectedIp();
            deviceScanListener.pingClientsFinished(clients, isNeedPollAfterPing);
//            pingClients(deviceIp);
        }
    }

//    // deviceIp != null
//    private void pingClients(String deviceIp) {
//        isScanning = true;
//        Logger.d(TAG, "pingClients(), deviceIp: " + deviceIp);
//        clients = new ArrayList<>();
//        String host = deviceIp.substring(0, deviceIp.lastIndexOf("."));
//        Logger.d(TAG, "device ip substring: " + host);
//
//        ExecutorService executorService = Executors.newFixedThreadPool(256);
//
//        CompletableFuture<Void>[] futures = new CompletableFuture[256];
//        for (int i = 0; i < 256; i++) {
//            futures[i] = CompletableFuture.runAsync(new PingIp(host + "." + i), executorService);
//        }
//        CompletableFuture.allOf(futures)
//                .thenRun(() -> {
//                    executorService.shutdown();
//                    isScanning = false;
//                    clients.remove(deviceIp);
//                    Logger.d(TAG, "all clients pinged, return: " + clients);
//                    deviceScanListener.pingClientsFinished(clients, isNeedPollAfterPing);
//                });
//    }

//    private class PingIp implements Runnable {
//        private static final int timeout = 5000;
//        private final String host;
//        public PingIp(String host) {
//            this.host = host;
//        }
//        @Override
//        public void run() {
//            try {
//                InetAddress inetAddress = InetAddress.getByName(host);
//                if (inetAddress.isReachable(timeout)) {
//                    Logger.d(TAG, "ping: " + host);
//                    clients.add(host);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//                Logger.d(TAG, "Ping ip exception: " + e.getLocalizedMessage());
//            }
//        }
//    }

    public static List<String> getConnectedIp(){
        Logger.d(TAG, "getConnectedIp()");
        List<String> connectedIp = new ArrayList<>();
        InputStream inputStream;
        try {
            String ping = "ip neigh";
            Runtime run = Runtime.getRuntime();
            Process pro = run.exec(ping);
            inputStream = pro.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null)
                if(line.contains("DELAY")) {
                    connectedIp.add(line.split(" ")[0]);
                }
            inputStream.close();
            Logger.d(TAG, "result: " + connectedIp);
        }
        catch (IOException e) {
            Logger.d(TAG, "ipNeight() e: " + e.getMessage());
        }
        return connectedIp;
    }
}