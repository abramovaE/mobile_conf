package com.kotofeya.mobileconfigurator.network;



import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.kotofeya.mobileconfigurator.Logger;

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
}
