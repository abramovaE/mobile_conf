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

//    public List<String> getClientList() {
//        Logger.d(Logger.WIFI_LOG, "getting clients list");
//        List<String> connectedTransivers = new ArrayList<>();
//
////        StringBuilder cmdReturn = new StringBuilder();
////
//////        String[] command = {"cat", "/proc/net/arp"};
////        String[] command = {"netstat"};
////
////        InputStream inputStream = null;
////        try {
////            ProcessBuilder processBuilder = new ProcessBuilder(command);
////            Process process = processBuilder.start();
////            inputStream = process.getInputStream();
////            int c;
////            while ((c = inputStream.read()) != -1) {
////                cmdReturn.append((char) c);
////            }
////        } catch (IOException e) {
////            e.printStackTrace();
////            Logger.d(Logger.WIFI_LOG, "error: " + e.getMessage());
////        }
////        finally {
////            if(inputStream != null){
////                try {
////                    inputStream.close();
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
////            }
////        }
////        Logger.d(Logger.WIFI_LOG, "cmd: " + cmdReturn.toString());
//
////        if(isReadStoragePermissionGranted()){
////            runAsRoot("arp -a");
////        }
//
//
//
//            BufferedReader br = null;
//
//            try {
//                br = new BufferedReader(new FileReader("/proc/net/arp"));
//                String line;
//                while ((line = br.readLine()) != null) {
//                    Logger.d(Logger.WIFI_LOG, "client: " + line);
//
//                    String[] splitted = line.split(" +");
//                    if (splitted != null) {
//                        String mac = splitted[3];
//                        if (mac.matches("b8:27:..:..:..:..")) {
//                            connectedTransivers.add(splitted[0]);
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                Logger.d(Logger.WIFI_LOG, "exception2: " + e.getMessage());
//            } finally {
//                try {
//                    if(br != null) {
//                        br.close();
//                    }
//                } catch (IOException e) {
//
//                }
//            }
//        return connectedTransivers;
//    }

//    private static boolean runAsRoot(final String command) {
//        try {
//            Process pro = Runtime.getRuntime().exec(command);
//            DataOutputStream outStr = new DataOutputStream(pro.getOutputStream());
//            DataInputStream inputStream = new DataInputStream(pro.getInputStream());
//            StringBuilder sb = new StringBuilder();
//            outStr.writeBytes(command);
////            int i =0;
////            while ((i = inputStream.read()) != -1){
////                sb.append(i);
////            }
//
////            sb.append(new BufferedReader(new InputStreamReader(inputStream)));
//            Logger.d(Logger.WIFI_LOG, "sb: " + new BufferedReader(new InputStreamReader(inputStream)).lines()
//                    .parallel().collect(Collectors.joining("\n")));
//            outStr.writeBytes("\nexit\n");
//            outStr.flush();
//            int retval = pro.waitFor();
//            return (retval == 0);
//        } catch (Exception e) {
//            Logger.d(Logger.WIFI_LOG, "e: " + e.getLocalizedMessage());
//            return false;
//        }
//    }

//    public boolean clearArpTable(){
//        return runAsRoot("ip -s -s neigh flush all");
//    }

//    class ScanIpTask extends AsyncTask<Integer, String, String> {
//        static final String subnet = "192.168.43.";
//        static final int timeout = 5000;
//        @Override
//        protected String doInBackground(Integer... params) {
//                String host = subnet + params[0];
//                Logger.d(Logger.WIFI_LOG, "doing: " + host);
//                try {
//                    InetAddress inetAddress = InetAddress.getByName(host);
//                    if (inetAddress.isReachable(timeout)){
//                        publishProgress(inetAddress.toString());
//                    }
//                } catch (UnknownHostException e) {
//                    e.printStackTrace();
//                    Logger.d(Logger.WIFI_LOG, "exception: " + e.getLocalizedMessage());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Logger.d(Logger.WIFI_LOG, "exception: " + e.getLocalizedMessage());
//                }
//            return "";
//        }
//
//        @Override
//        protected void onProgressUpdate(String... values) {
//            clients.add(values[0]);
//        }
//
//        @Override
//        protected void onPostExecute(String aVoid) {
//            counter++;
//            Logger.d(Logger.WIFI_LOG, "on post execute, counter: " + counter + ", clients: " + clients);
//        }
//    }

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
            } catch (UnknownHostException e) {
                e.printStackTrace();
                Logger.d(Logger.WIFI_LOG, "exception: " + e.getLocalizedMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Logger.d(Logger.WIFI_LOG, "exception: " + e.getLocalizedMessage());
            }
        }
    }
}
