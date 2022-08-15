package com.kotofeya.mobileconfigurator;

import android.os.Bundle;


import com.jcraft.jsch.Channel;
import com.jcraft.jsch.Session;
import com.kotofeya.mobileconfigurator.network.SshCommand;
import com.kotofeya.mobileconfigurator.network.SshUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

public class SshTakeInfoConnectionRunnable implements Runnable, TaskCode {

    private static final String TAG = SshTakeInfoConnectionRunnable.class.getSimpleName();
    private final OnTaskCompleted listener;
    private final String ip;


    public SshTakeInfoConnectionRunnable(OnTaskCompleted listener, String ip){
        Logger.d(TAG, "new SshTakeInfoConnectionRunnable(), ip: " + ip);
        this.listener = listener;
        this.ip = ip;
    }

    @Override
    public void run() {
            String res = "";
            Session session = null;
            Channel channel = null;
            String command = "";
            try {
                session = SshUtils.getSession(ip);
                Logger.d(TAG, ip + " isConnected: " + session.isConnected());

                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                command = SshCommand.SSH_TAKE_COMMAND;
                channel = session.openChannel("shell");

                OutputStream inputstream_for_the_channel = channel.getOutputStream();
                PrintStream commander = new PrintStream(inputstream_for_the_channel, true);
                channel.setOutputStream(baos, true);
                channel.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(App.get()
                        .getAssets().open("take.sh")));
                        String e;
                        while ((e = reader.readLine()) != null) {
                            commander.println(e);
                        }
                        do {
                            Thread.sleep(2000);
                        } while (!channel.isEOF());
                        reader.close();
                        commander.close();
                        res = baos.toString().substring(baos.toString().lastIndexOf("$typeT") + 7, baos.toString().lastIndexOf("$ exit"));
            } catch (Exception e){
                Logger.d(TAG, "ssh error: " + e.getMessage());
                res = e.getMessage();
                command = SshCommand.SSH_COMMAND_ERROR;
            } finally {
                if(channel != null){
                    channel.disconnect();
                }
                if(session != null) {
                    session.disconnect();
                }
                if (listener != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString(BundleKeys.COMMAND_KEY, command);
                    bundle.putString(BundleKeys.IP_KEY, this.ip);
                    bundle.putString(BundleKeys.RESPONSE_KEY, res);
                    bundle.putString(BundleKeys.VERSION_KEY, "ssh_conn");
                    listener.onTaskCompleted(bundle);
                }
            }
    }
}