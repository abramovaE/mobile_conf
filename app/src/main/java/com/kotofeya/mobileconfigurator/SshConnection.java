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
    private String ip;

    public SshConnection(OnTaskCompleted listener){
            this.listener = listener;
    }

        // req[0] - ip
        //req[1] - command

    protected String doInBackground(Object...req) {
        String res = "";
        Session session = null;
        ChannelSftp channelSftp = null;
        ChannelExec channelExec = null;
        Channel channel = null;

        try {
            JSch jsch = new JSch();
            this.ip = (String) req[0];
            session = jsch.getSession("staff", ip, 22);
            session.setPassword("staff");
            ByteArrayOutputStream baos = null;
            // Avoid asking for key confirmation
            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");
            session.setConfig(prop);
            session.connect();
            Logger.d(Logger.SSH_CONNECTION_LOG, ip + " isConnected: " + session.isConnected());

            switch ((String) req[1]) {
                case TAKE_COMMAND:
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
                    res = baos.toString().substring(baos.toString().lastIndexOf("$load") + 6, baos.toString().lastIndexOf("$ exit"));
                    break;

                case UPDATE_OS_LOAD_FILE_COMMAND:
                    channelSftp = (ChannelSftp) session.openChannel("sftp");
                    channelSftp.connect();
                    channelSftp.put(Downloader.tempUpdateOsFile.getAbsolutePath(), "/overlay/update", new SftpProgressMonitor() {
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
                    channelExec = (ChannelExec) session.openChannel("exec");
                    channelExec.setCommand(REBOOT_COMMAND);
                    channelExec.connect();
                    res = "updateos:" + ip;
                    break;

                case UPDATE_STM_LOAD_FILE_COMMAND:
                    String filePath = (String) req[2];
                    File file = new File(filePath);
                    channelSftp = (ChannelSftp) session.openChannel("sftp");
                    channelSftp.connect();
                    channelSftp.put(file.getAbsolutePath(), "/overlay/update", new SftpProgressMonitor() {
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

                    channelExec = (ChannelExec) session.openChannel("exec");
                    String moveCommand = "sudo mv " + "/overlay/update/" + file.getName() + " /overlay/update/www-data/data.tar.bz2";
                    channelExec.setCommand(moveCommand + ";" + REBOOT_COMMAND);
                    channelExec.connect();
                    res = "Downloaded" + ip;
                    break;

                case REBOOT_COMMAND:
                    channelExec = (ChannelExec) session.openChannel("exec");
                    channelExec.setCommand(REBOOT_COMMAND);
                    channelExec.connect();
                    res = "reboot:" + ip;
                    break;

                case REBOOT_STM_COMMAND:

                    // SSH Channel
                    channelExec = (ChannelExec) session.openChannel("exec");
                    channelExec.setCommand(REBOOT_STM_COMMAND);
                    StringBuilder sb = new StringBuilder();
                    channelExec.connect();
                    InputStream commandOutput = channelExec.getInputStream();
                    Thread.sleep(1000);
                    int readByte = 0;
                    while ((readByte = commandOutput.read()) != -1) {
                        sb.append((char) readByte);
                    }
                    res = sb.toString();
                    Logger.d(Logger.SSH_CONNECTION_LOG, "reboot stm res" + res);
                    break;
            }
        }
        catch (Exception e){
                Logger.d(Logger.SSH_CONNECTION_LOG, "error: " + e.getMessage());
        }

        finally {
            if(channel != null){
                channel.disconnect();
            }
            if(channelExec != null){
                channel.disconnect();
            }
            if(channelSftp != null){
                channelSftp.disconnect();
            }
            if(session != null) {
                session.disconnect();
            }
        }
        return res;
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
