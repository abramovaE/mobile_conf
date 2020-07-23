package com.kotofeya.mobileconfigurator;

import android.os.AsyncTask;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.Properties;

public class SshConnection extends AsyncTask<Object, Object, String> {


    public static final String UPTIME_COMMAND = "uptime";
    public static final String TAKE_COMMAND = "'sh -s' < /home/dirvion/scripts/take/take.sh";

    private SshCompleted listener;
    private Transiver currentTransiver;


    public SshConnection(SshCompleted listener){
            this.listener = listener;
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

            Logger.d(Logger.SSH_CONNECTION_LOG, ((Transiver) req[0]).getIp() + " isConnected: " + session.isConnected());

            Channel channel=session.openChannel("shell");

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
            reader.close();
            commander.close();
            do {
                Thread.sleep(1000);
            } while(!channel.isEOF());


            res = baos.toString().substring(baos.toString().lastIndexOf("$load") + 6, baos.toString().lastIndexOf("$ exit"));
            String[] r = res.split("\n");
            session.disconnect();
        }
        catch (Exception e)
        {
            Logger.d(Logger.SSH_CONNECTION_LOG, "error: " + e.getMessage());
        }
        return res;
    }

    protected void onPostExecute(String result){
            Logger.d(Logger.SSH_CONNECTION_LOG, "result: " + result);
            currentTransiver.setBasicScanInfo(result);
            listener.onTaskCompleted(result);
        }

}
