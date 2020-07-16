package com.kotofeya.mobileconfigurator;

import android.os.AsyncTask;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

public class SshConnection extends AsyncTask<String, Object, Object> {

    private SshCompleted listener;


    public SshConnection(SshCompleted listener){
            this.listener=listener;
        }


    protected Object doInBackground(String... ip) {

        try
        {
            JSch jsch = new JSch();
            Session session = jsch.getSession("staff", ip[0], 22);
            session.setPassword("staff");

            // Avoid asking for key confirmation
                Properties prop = new Properties();
                prop.put("StrictHostKeyChecking", "no");
                session.setConfig(prop);

            session.connect();

            Logger.d(Logger.MAIN_LOG, ip[0] + " isConnected: " + session.isConnected());


            // SSH Channel
            ChannelExec channelssh = (ChannelExec) session.openChannel("exec");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            channelssh.setOutputStream(baos);

            channelssh.setCommand("uptime");
            channelssh.connect();
            try{Thread.sleep(1000);}catch(Exception ee){}
            Logger.d(Logger.MAIN_LOG, "result: " + baos.toString());
            channelssh.disconnect();







        }
        catch (Exception e)
        {
            Logger.d(Logger.MAIN_LOG, "error: " + e.getMessage());

        }

        return null;
    }

    protected void onPostExecute(Object o){
            listener.onTaskCompleted();
        }

}
