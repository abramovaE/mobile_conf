package com.kotofeya.mobileconfigurator;

import android.os.AsyncTask;
import android.os.Bundle;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.Timer;


public class SshConnectionRunnable implements Runnable, TaskCode {

    private OnTaskCompleted listener;
    private String ip;
    private int resultCode;
    String req2;
    String req3;
    String req4;
    String req5;

    public SshConnectionRunnable(OnTaskCompleted listener, Object...req){
        this.listener = listener;
        this.ip = (String) req[0];
        this.resultCode = (Integer) req[1];

        if(req.length > 2){
            this.req2 = (String) req[2];
        }
        if(req.length > 3) {
            this.req3 = (String) req[3];
        }
        if(req.length > 4){
            this.req4 = (String) req[4];
        }
        if(req.length > 5) {
            this.req5 = (String) req[5];
        }
    }
    int transferred;
    // req[0] - ip
    //req[1] - command


    @Override
    public void run() {
            String res = "";
            Session session = null;
            ChannelSftp channelSftp = null;
            ChannelExec channelExec = null;
            Channel channel = null;

            try {
                JSch jsch = new JSch();
                session = jsch.getSession("staff", ip, 22);
                session.setPassword("staff");
                ByteArrayOutputStream baos = null;
                // Avoid asking for key confirmation
                Properties prop = new Properties();
                prop.put("StrictHostKeyChecking", "no");
                session.setConfig(prop);
                session.connect();
                Logger.d(Logger.SSH_CONNECTION_LOG, ip + " isConnected: " + session.isConnected());

                switch (resultCode) {
                    case TAKE_CODE:
                        long start = System.currentTimeMillis();
                        Logger.d(Logger.SSH_CONNECTION_LOG, "start: " + start);
                        channel = session.openChannel("shell");
                        baos = new ByteArrayOutputStream();
                        OutputStream inputstream_for_the_channel = channel.getOutputStream();
                        PrintStream commander = new PrintStream(inputstream_for_the_channel, true);
                        channel.setOutputStream(baos, true);
                        channel.connect();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(App.get().getAssets().open("take.sh")));
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
                        long end = System.currentTimeMillis() - start;
                        Logger.d(Logger.SSH_CONNECTION_LOG, "end: " + end);
                        break;
                }
            }
            catch (Exception e){
                res = e.getMessage();
                this.resultCode = SSH_ERROR_CODE;
            }

            finally {
                if(channel != null){
                    channel.disconnect();
                }
                if(channelExec != null){
                    channelExec.disconnect();
                }
                if(channelSftp != null){
                    channelSftp.disconnect();
                }
                if(session != null) {
                    session.disconnect();
                }
                if(resultCode != 0){
                    Logger.d(Logger.SSH_CONNECTION_LOG, "resultCode: " + resultCode + ", result: " + res + ",ip: " + ip);
                    Logger.d(Logger.SSH_CONNECTION_LOG, "listener != null: " + listener);
                }
                if (listener != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString("ip", this.ip);
                    bundle.putInt("resultCode", this.resultCode);
                    bundle.putString("result", res);
                    listener.onTaskCompleted(bundle);
                }
            }
    }
}