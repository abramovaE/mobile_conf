package com.kotofeya.mobileconfigurator.network;



import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.kotofeya.mobileconfigurator.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SshUtils {
    private static final String TAG = SshUtils.class.getSimpleName();
    private static final String USERNAME = "staff";
    private static final String PASSWORD = "staff";

    private static final Integer PORT = 22;

    public static Session getSession(String ip) throws JSchException {
        JSch jsch = new JSch();
        Logger.d(TAG, "getSession(), ip: " + ip);
        com.jcraft.jsch.Session session = jsch.getSession(USERNAME, ip, PORT);
        session.setPassword(PASSWORD);
        // Avoid asking for key confirmation
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);
        session.connect();
        return session;
    }

    public static String execCommand(Session session, String command) throws IOException {
        Logger.d(TAG, "exec command: " + command);
        String res;
        ChannelExec channelExec = null;
        InputStream commandOutput = null;
        try {
            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);
            StringBuilder sb = new StringBuilder();
            channelExec.connect();
            commandOutput = channelExec.getInputStream();
            Thread.sleep(2000);
            int readByte;
            while ((readByte = commandOutput.read()) != -1) {
                sb.append((char) readByte);
            }
            res = sb.toString();
        } catch (JSchException | IOException | InterruptedException e) {
            e.printStackTrace();
            res = "error";
        } finally {
            if(commandOutput != null){
                commandOutput.close();
            }
            if(channelExec != null){
                channelExec.disconnect();
            }
        }
        Logger.d(TAG, "exec command: " + command + " result: " + res);
        return res;
    }
}