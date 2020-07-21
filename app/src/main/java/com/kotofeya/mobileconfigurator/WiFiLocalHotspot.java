package com.kotofeya.mobileconfigurator;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WiFiLocalHotspot {



    private static WiFiLocalHotspot instance = new WiFiLocalHotspot();
    private WiFiLocalHotspot(){}

    public static WiFiLocalHotspot getInstance(){
        return instance;
    }

    public List<String> getClientList() {
        List<String> connectedTransivers = new ArrayList<>();

        int macCount = 0;
        BufferedReader br = null;
        String flushCmd = "sh ip -s -s neigh flush all";
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec(flushCmd, null, new File("/proc/net"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null) {
                    String mac = splitted[3];
                    if (mac.matches("b8:27:..:..:..:..")) {
                        macCount++;


                        connectedTransivers.add(splitted[0]);
                    }


                }
            }
        } catch (Exception e) {

        }

        return connectedTransivers;
    }



}
