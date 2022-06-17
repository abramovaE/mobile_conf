package com.kotofeya.mobileconfigurator.hotspot;

import com.kotofeya.mobileconfigurator.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class WiFiLocalHotspot {

    private static final String TAG = WiFiLocalHotspot.class.getSimpleName();
    private static WiFiLocalHotspot instance;

    public static synchronized WiFiLocalHotspot getInstance() {
        if(instance == null){
            instance = new WiFiLocalHotspot();
        }
        return instance;
    }

    private WiFiLocalHotspot() {}

    public void updateClientList(DeviceScanListener deviceScanListener, boolean isNeedPollAfterPing){
        Logger.d(TAG, "updateClientList()");
        List<String> clients = getConnectedIp();
        deviceScanListener.pingClientsFinished(clients, isNeedPollAfterPing);
    }

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
            while ((line = bufferedReader.readLine()) != null) {
//                Logger.d(TAG, "line: " + line);
                if (line.startsWith("192.168.")) {
                    connectedIp.add(line.split(" ")[0]);
                }
            }
            inputStream.close();
            Logger.d(TAG, "result: " + connectedIp);
        }
        catch (IOException e) {
            Logger.d(TAG, "ipNeigh() e: " + e.getMessage());
        }
        return connectedIp;
    }
}