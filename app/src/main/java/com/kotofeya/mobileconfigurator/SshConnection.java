package com.kotofeya.mobileconfigurator;

import android.os.AsyncTask;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

public class SshConnection extends AsyncTask<String, Object, String> {


    public static final String UPTIME_COMMAND = "uptime";

    private SshCompleted listener;


    public SshConnection(SshCompleted listener){
            this.listener=listener;
        }

        // req[0] - ip
        //req[1] - command
    protected String doInBackground(String...req) {
        ByteArrayOutputStream baos = null;

        try
        {
            JSch jsch = new JSch();
            Session session = jsch.getSession("staff", req[0], 22);
            session.setPassword("staff");

            // Avoid asking for key confirmation
                Properties prop = new Properties();
                prop.put("StrictHostKeyChecking", "no");
                session.setConfig(prop);

            session.connect();

            Logger.d(Logger.MAIN_LOG, req[0] + " isConnected: " + session.isConnected());


            // SSH Channel
            ChannelExec channelssh = (ChannelExec) session.openChannel("exec");
            baos = new ByteArrayOutputStream();
            channelssh.setOutputStream(baos);

            channelssh.setCommand((String)req[1]);
            channelssh.connect();
            try{Thread.sleep(1000);}catch(Exception ee){}

            Logger.d(Logger.MAIN_LOG, "result: " + baos.toString());
            channelssh.disconnect();
        }
        catch (Exception e)
        {
            Logger.d(Logger.MAIN_LOG, "error: " + e.getMessage());

        }

        return baos.toString();
    }

    protected void onPostExecute(String result){

            listener.onTaskCompleted(result);
        }

}
