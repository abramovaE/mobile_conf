package com.kotofeya.mobileconfigurator;

import android.os.AsyncTask;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;

public class SshExecConnection extends AsyncTask<Object, Object, String> {


    public static final String TAKE_COMMAND = "take command";
    public static final String UPDATE_OS_LOAD_FILE_COMMAND = "update os command";
    public static final String REBOOT_COMMAND = "reboot";




//    public static final String TAKE_COMMAND = "'sh -s' < /home/dirvion/scripts/take/take.sh";

    private OnTaskCompleted listener;
    private Transiver currentTransiver;


    public SshExecConnection() {
    }



        // req[0] - transiver
        //req[1] - command

    protected String doInBackground(Object...req) {

        Session session;
        ByteArrayOutputStream baos = null;
        String res = "";


        switch ((String)req[1]){
            case TAKE_COMMAND:
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
                    Logger.d(Logger.SSH_CONNECTION_LOG, currentTransiver.getIp() + " isConnected: " + session.isConnected());
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
                String ip = (String) req[0];
                Logger.d(Logger.SSH_CONNECTION_LOG, ip);

                try
                {
                JSch jsch = new JSch();
                session = jsch.getSession("staff", ip, 22);
                session.setPassword("staff");

                Properties prop = new Properties();
                prop.put("StrictHostKeyChecking", "no");

                session.setConfig(prop);
                session.connect();
                Logger.d(Logger.SSH_CONNECTION_LOG, ip + " isConnected: " + session.isConnected());

//                    // SSH Channel
//                    ChannelExec channelssh = (ChannelExec) session.openChannel("exec");
//                    baos = new ByteArrayOutputStream();
//                    channelssh.setOutputStream(baos);
//
//                    // Execute command
////                    channelssh.setCommand(REBOOT_COMMAND);
//                    channelssh.connect();
//
//                    do {
//                        Thread.sleep(2000);
//                    } while(!channelssh.isEOF());
//
////                    channelssh.disconnect();

//                    res = baos.toString();
                    session.disconnect();
                    return res;

        }
                catch (Exception e)
        {
            Logger.d(Logger.SSH_CONNECTION_LOG, "error: " + e.getMessage());
        }
        return res;


        }

        return "";

    }

    protected void onPostExecute(String result) {
        Logger.d(Logger.SSH_CONNECTION_LOG, "result: " + result);
//            currentTransiver.setBasicScanInfo(result);

        if (result.split("\n").length > 10) {
            parseBasicScanInfo(result);
        }


        if (listener != null) {
            listener.onTaskCompleted(result);
        }
    }



    private void parseBasicScanInfo(String result){
        String[] info = result.split("\n");
//        Logger.d(Logger.SSH_CONNECTION_LOG, "info: " + info.length);

//        for(String s: info){
//            Logger.d(Logger.SSH_CONNECTION_LOG, "s: " + s);
//        }

            currentTransiver.setSsid(info[1]);
//            currentTransiver.setIp(info[2].trim());
            currentTransiver.setMacWifi(info[3]);
            currentTransiver.setMacBt(info[4]);
            currentTransiver.setBoardVersion(info[5]);
            currentTransiver.setOsVersion(info[6]);
            currentTransiver.setStmFirmware(info[7]);
            currentTransiver.setStmBootloader(info[8]);
            currentTransiver.setCore(info[9]);
            currentTransiver.setModem(info[10]);
            currentTransiver.setIncrementOfContent(info[11]);
            currentTransiver.setUptime(info[12]);
            currentTransiver.setCpuTemp(info[13]);
            currentTransiver.setLoad(info[14]);


    }

}
