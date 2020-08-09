package com.kotofeya.mobileconfigurator;

import android.os.AsyncTask;
import android.os.Bundle;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpProgressMonitor;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;

public class SshConnection extends AsyncTask<Object, Object, String> {


    public static final String TAKE_COMMAND = "take command";
    public static final String UPDATE_OS_LOAD_FILE_COMMAND = "update os command";
    public static final String UPDATE_STM_LOAD_FILE_COMMAND = "update stm command";

    public static final String REBOOT_COMMAND = "sudo reboot";
    public static final String REBOOT_STM_COMMAND =  "/usr/local/bin/call --cmd REST 0";


    private OnTaskCompleted listener;
    private Transiver currentTransiver;


    private String ip;


    public SshConnection(OnTaskCompleted listener){
            this.listener = listener;

        }

        // req[0] - transiver
        //req[1] - command

    protected String doInBackground(Object...req) {

        Session session;
        ByteArrayOutputStream baos = null;
        String res = "";
        JSch jsch = new JSch();
        this.ip = (String) req[0];

//        String ip;


        switch ((String)req[1]){
            case TAKE_COMMAND:
//                this.ip = (String) req[0];
                ip = (String) req[0];
                try
                {

                    session = jsch.getSession("staff", ip, 22);
                    session.setPassword("staff");

                    // Avoid asking for key confirmation
                    Properties prop = new Properties();
                    prop.put("StrictHostKeyChecking", "no");

                    session.setConfig(prop);
                    session.connect();
                    Logger.d(Logger.SSH_CONNECTION_LOG, ip + " isConnected: " + session.isConnected());
                    Channel channel = session.openChannel("shell");
                    baos =new ByteArrayOutputStream();

                    OutputStream inputstream_for_the_channel = channel.getOutputStream();
                    PrintStream commander = new PrintStream(inputstream_for_the_channel, true);
                    channel.setOutputStream(baos, true);
                    channel.connect();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(App.get().getAssets().open("take.sh")));
                    String e;
                    while ((e = reader.readLine()) != null){
                        commander.println(e);
                    }
                    do {
                        Thread.sleep(2000);
                    } while(!channel.isEOF());

                    reader.close();
                    commander.close();
                    res = baos.toString().substring(baos.toString().lastIndexOf("$load") + 6, baos.toString().lastIndexOf("$ exit"));
                    session.disconnect();
                }
                catch (Exception e)
                {
                    Logger.d(Logger.SSH_CONNECTION_LOG, "error: " + e.getMessage());
                }
                return res;


            case UPDATE_OS_LOAD_FILE_COMMAND:
//                ip = (String) req[0];
                Logger.d(Logger.SSH_CONNECTION_LOG, ip);
                try
                {
                session = jsch.getSession("staff", ip, 22);
                session.setPassword("staff");

                Properties prop = new Properties();
                prop.put("StrictHostKeyChecking", "no");

                session.setConfig(prop);
                session.connect();
                Logger.d(Logger.SSH_CONNECTION_LOG, ip + " isConnected: " + session.isConnected());

                    ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
                    sftpChannel.connect();
                    Logger.d(Logger.SSH_CONNECTION_LOG, "updateOsFile: " + Downloader.tempUpdateOsFile);
                    Logger.d(Logger.SSH_CONNECTION_LOG, "src file length: " + Downloader.tempUpdateOsFile.length());
                    sftpChannel.put(Downloader.tempUpdateOsFile.getAbsolutePath(), "/overlay/update", new SftpProgressMonitor() {
                        @Override
                        public void init(int op, String src, String dest, long max) {

                        }

                        @Override
                        public boolean count(long count) {
                            Logger.d(Logger.SSH_CONNECTION_LOG, "transfered: " + count);
                            return true;
                        }

                        @Override
                        public void end() {
                            Logger.d(Logger.SSH_CONNECTION_LOG, "end transfering");
//                            sftpChannel.exit();
                        }
                    });
                    Logger.d(Logger.SSH_CONNECTION_LOG, "updateOsFile completed");

                    // SSH Channel
                    ChannelExec channelssh = (ChannelExec) session.openChannel("exec");
                    channelssh.setCommand(REBOOT_COMMAND);
                    channelssh.connect();
                    res = "updateos:" + ip;
                    session.disconnect();
        }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Logger.d(Logger.SSH_CONNECTION_LOG, "error: " + e.getMessage());
                }
                return res;

            case UPDATE_STM_LOAD_FILE_COMMAND:
//                String ipTrans = (String) req[0];
                String filePath = (String) req[2];
                File file = new File(filePath);
                try
                {
                    session = jsch.getSession("staff", ip, 22);
                    session.setPassword("staff");
                    Properties prop = new Properties();
                    prop.put("StrictHostKeyChecking", "no");
                    session.setConfig(prop);
                    session.connect();
                    Logger.d(Logger.SSH_CONNECTION_LOG, ip + " isConnected: " + session.isConnected());
                    ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
                    sftpChannel.connect();

                    Logger.d(Logger.SSH_CONNECTION_LOG, "updateStmFile: " + file.getAbsolutePath());
                    Logger.d(Logger.SSH_CONNECTION_LOG, "src file length: " + file.length());
                    sftpChannel.put(file.getAbsolutePath(), "/overlay/update", new SftpProgressMonitor() {
                        @Override
                        public void init(int op, String src, String dest, long max) {

                        }

                        @Override
                        public boolean count(long count) {
                            Logger.d(Logger.SSH_CONNECTION_LOG, "transfered: " + count);
                            return true;
                        }

                        @Override
                        public void end() {
                            Logger.d(Logger.SSH_CONNECTION_LOG, "end transfering");

                        }
                    });

                    ChannelExec channelssh = null;
                    try {
                        channelssh = (ChannelExec) session.openChannel("exec");
                        String moveCommand = "sudo mv " + "/overlay/update/" + file.getName() + " /overlay/update/www-data/data.tar.bz2";
                        channelssh.setCommand(moveCommand + ";" + REBOOT_COMMAND);
                        channelssh.connect();

                    } catch (JSchException e) {
                        Logger.d(Logger.SSH_CONNECTION_LOG, "jsch exception");
                        e.printStackTrace();
                    }
                    res = "Downloaded" + ip;
                    session.disconnect();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Logger.d(Logger.SSH_CONNECTION_LOG, "error: " + e.getMessage());
                }
                return res;

            case REBOOT_COMMAND:
                ip = (String) req[0];
                Logger.d(Logger.SSH_CONNECTION_LOG, ip);
                try
                {
                    session = jsch.getSession("staff", ip, 22);
                    session.setPassword("staff");

                    Properties prop = new Properties();
                    prop.put("StrictHostKeyChecking", "no");

                    session.setConfig(prop);
                    session.connect();
                    Logger.d(Logger.SSH_CONNECTION_LOG, ip + " isConnected: " + session.isConnected());

                    // SSH Channel
                    ChannelExec channelssh = (ChannelExec) session.openChannel("exec");
                    channelssh.setCommand(REBOOT_COMMAND);
                    channelssh.connect();
                    res = "reboot:" + ip;
                    session.disconnect();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Logger.d(Logger.SSH_CONNECTION_LOG, "error: " + e.getMessage());
                }
                return res;

            case REBOOT_STM_COMMAND:
                ip = (String) req[0];
                Logger.d(Logger.SSH_CONNECTION_LOG, ip);
                try
                {
                    session = jsch.getSession("staff", ip, 22);
                    session.setPassword("staff");

                    Properties prop = new Properties();
                    prop.put("StrictHostKeyChecking", "no");

                    session.setConfig(prop);
                    session.connect();
                    Logger.d(Logger.SSH_CONNECTION_LOG, ip + " isConnected: " + session.isConnected());

                    // SSH Channel
                    ChannelExec channelssh = (ChannelExec) session.openChannel("exec");
                    channelssh.setCommand(REBOOT_STM_COMMAND);
                    StringBuilder sb = new StringBuilder();
                    channelssh.connect();
                    InputStream commandOutput = channelssh.getInputStream();

                    Thread.sleep(1000);
                    int readByte = 0;

                    while((readByte = commandOutput.read()) != -1)
                    {
                        sb.append((char)readByte);
                    }

                    res = sb.toString();
                    Logger.d(Logger.SSH_CONNECTION_LOG, "reboot stm res" + res);

                    session.disconnect();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Logger.d(Logger.SSH_CONNECTION_LOG, "error: " + e.getMessage());
                }
                return res;





            default:
                return "";

        }


    }

    protected void onPostExecute(String result) {
        Logger.d(Logger.SSH_CONNECTION_LOG, "result: " + result);

        if (listener != null) {
            Bundle bundle = new Bundle();
            bundle.putString("result", result);
            bundle.putString("ip", this.ip);
            listener.onTaskCompleted(bundle);
        }
    }

}
