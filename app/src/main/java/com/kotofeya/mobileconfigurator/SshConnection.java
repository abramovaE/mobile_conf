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

            Logger.d(Logger.MAIN_LOG, ((Transiver) req[0]).getIp() + " isConnected: " + session.isConnected());

            Channel channel=session.openChannel("shell");

            baos =new ByteArrayOutputStream();

            OutputStream inputstream_for_the_channel = channel.getOutputStream();
            PrintStream commander = new PrintStream(inputstream_for_the_channel, true);
            channel.setOutputStream(baos, true);

            channel.connect();



            BufferedReader reader = new BufferedReader(new InputStreamReader(App.get().getAssets().open("take")));
            String e;
            while ((e = reader.readLine()) != null){
                commander.println(e);
            }
            reader.close();
            commander.close();

            do {
                Thread.sleep(1000);
            } while(!channel.isEOF());

            session.disconnect();

            Logger.d(Logger.MAIN_LOG, "res " + baos.toString());

//            InputStream in = new PipedInputStream();
//            PipedOutputStream pin = new PipedOutputStream((PipedInputStream) in);
//            channel.setInputStream(in);
//            channel.connect();
//
//            int c;
//            InputStream inputStream = App.get().getAssets().open("take");
//            while ((c = inputStream.read()) != -1){
//                pin.write(c);
//            }
////
////            inputStream.close();
////
//
//
//            channel.setOutputStream(System.out, true);

//
//
//            OutputStream out = new PipedOutputStream();
//            PipedInputStream pout = new PipedInputStream((PipedOutputStream) out);
//            BufferedReader consoleOutput = new BufferedReader(new InputStreamReader(pout));
//            consoleOutput.readLine();
//
//            boolean end = true;
//
//            while(!end)
//            {
//                consoleOutput.mark(32);
//                if (consoleOutput.read()==0x03) end = true;//End of Text
//                else
//                {
//                    consoleOutput.reset();
//                    consoleOutput.readLine();
//                    end = false;
//                }
//            }

//
//            OutputStream inputstream_for_the_channel = channel.getOutputStream();
//            PrintStream commander = new PrintStream(inputstream_for_the_channel, true);
//
//            channel.setOutputStream(System.out, true);
//
//            channel.connect();
//
//
//            BufferedReader reader = new BufferedReader(new InputStreamReader(App.get().getAssets().open("take")));
//            String e;
//            while ((e = reader.readLine()) != null){
//                commander.println(e);
////                Logger.d(Logger.MAIN_LOG, "rres: " + e);
//            }
//            reader.close();
////

//
//            commander.println("ls -la");
//            commander.println("cd folder");
//            commander.println("ls -la");
//            commander.println("exit");
//            commander.close();

//            do {
//                Thread.sleep(1000);
//            } while(!channel.isEOF());

            session.disconnect();





//
//            InputStream in = new PipedInputStream();
//            channel.setInputStream(App.get().getAssets().open("take"));
//
//
//
//            baos = new ByteArrayOutputStream();
//            channel.setOutputStream(baos);
//
//            channel.connect();
//
//            Thread.sleep(4000);
//
//            res = baos.toString();
//
//            baos.close();
//            in.close();
//
//            channel.disconnect();
//            session.disconnect();
//
//
//
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
