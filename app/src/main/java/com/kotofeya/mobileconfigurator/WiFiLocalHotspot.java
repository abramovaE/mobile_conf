package com.kotofeya.mobileconfigurator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WiFiLocalHotspot {
    private static WiFiLocalHotspot instance = new WiFiLocalHotspot();
    private WiFiLocalHotspot(){}
    public static WiFiLocalHotspot getInstance(){
        return instance;
    }

    List<String> clients;

    public List<String> getClientList() {
        clients = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(300);
        CompletableFuture<Void>[] futures = new CompletableFuture[256];
        for (int i = 0; i < 256; i++) {
            futures[i] = CompletableFuture.runAsync(new PingIp(i), executorService);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        CompletableFuture.allOf(futures)
                .thenRun(() -> {
                    Logger.d(Logger.WIFI_LOG, "clients res: " + clients);
                    executorService.shutdown();
                    // Здесь выполнить действие
                });
        Logger.d(Logger.WIFI_LOG, "clients return: " + clients);
        return clients;
    }

    public class PingIp implements Runnable{
        private int ipSuff;
        static final String subnet = "192.168.43.";
        static final int timeout = 5000;

        public PingIp(int ipSuff) {
            this.ipSuff = ipSuff;
        }

        @Override
        public void run() {
            try {
                String host = subnet + ipSuff;
                InetAddress inetAddress = InetAddress.getByName(host);
                if (inetAddress.isReachable(timeout)){
                    clients.add(host);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Logger.d(Logger.WIFI_LOG, "exception: " + e.getLocalizedMessage());
            }
        }
    }
}
