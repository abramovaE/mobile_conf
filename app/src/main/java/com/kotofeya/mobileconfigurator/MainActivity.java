package com.kotofeya.mobileconfigurator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PatternMatcher;
import android.widget.Toast;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class MainActivity extends AppCompatActivity implements SshCompleted {


    private WifiManager.LocalOnlyHotspotReservation mReservation;

    private List<String> connectedTransivers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        connectedTransivers = new ArrayList<>();
        getClientList();
        Logger.d(Logger.MAIN_LOG, connectedTransivers.toString());

        for(String s: connectedTransivers){
           Logger.d(Logger.MAIN_LOG, "connecting to: " + s);

           SshConnection connection = new SshConnection(this);
           connection.execute(s);

//           connection.doInBackground(s);

        }


    }

    public int getClientList() {
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
                    if (mac.matches("..:..:..:..:..:..")) {
                        macCount++;

               connectedTransivers.add(splitted[0]);
                    }


                }
            }
        } catch (Exception e) {

        }
        return macCount;
    }


    @Override
    public void onTaskCompleted() {

    }
}
