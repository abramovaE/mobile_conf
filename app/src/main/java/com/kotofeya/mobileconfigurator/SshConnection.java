package com.kotofeya.mobileconfigurator;

import android.os.AsyncTask;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

public class SshConnection extends AsyncTask<Object, Object, String> {


    public static final String UPTIME_COMMAND = "uptime";
    public static final String TAKE_COMMEND = "-C ";

    private SshCompleted listener;
    private Transiver currentTransiver;


    public SshConnection(SshCompleted listener){
            this.listener=listener;
        }

        // req[0] - transiver
        //req[1] - command

    protected String doInBackground(Object...req) {

        Session session;
        ChannelExec channelssh;
        ByteArrayOutputStream baos = null;
        String res = "";

        this.currentTransiver = (Transiver) req[0];
        try
        {
            JSch jsch = new JSch();
            session = jsch.getSession("staff", currentTransiver.getIp(), 22);
            session.setPassword("staff");

            // Avoid asking for key confirmation
                Properties prop = new Properties();
                prop.put("StrictHostKeyChecking", "no");
                session.setConfig(prop);

            session.connect();

            Logger.d(Logger.MAIN_LOG, ((Transiver) req[0]).getIp() + " isConnected: " + session.isConnected());


            // SSH Channel
            channelssh = (ChannelExec) session.openChannel("exec");


            baos = new ByteArrayOutputStream();
            channelssh.setOutputStream(baos);

            channelssh.setCommand((String)req[1]);
            channelssh.connect();
            try{Thread.sleep(1000);}catch(Exception ee){}
            res = baos.toString();
            baos.close();
            channelssh.disconnect();
            session.disconnect();
        }
        catch (Exception e)
        {
            Logger.d(Logger.MAIN_LOG, "error: " + e.getMessage());

        }



        return res;
    }

    protected void onPostExecute(String result){
            Logger.d(Logger.MAIN_LOG, "result: " + result);
            currentTransiver.setBasicScanInfo(result);
            listener.onTaskCompleted(result);
        }

}
