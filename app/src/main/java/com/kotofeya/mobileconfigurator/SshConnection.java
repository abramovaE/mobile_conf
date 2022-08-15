package com.kotofeya.mobileconfigurator;

import android.os.AsyncTask;
import android.os.Bundle;

import com.jcraft.jsch.Session;
import com.kotofeya.mobileconfigurator.network.SshUtils;


public class SshConnection extends AsyncTask<Object, Object, String> implements TaskCode{

    private static final String TAG = SshConnection.class.getSimpleName();
    private static final String REBOOT_COMMAND = "sudo reboot";
    private static final String REBOOT_STM_COMMAND =
            "/usr/local/bin/call --cmd REST 0";
    private static final String CLEAR_RASP_COMMAND =
            "/sudo rm - f /var/www/html/data/*/* /var/www/html/data/*";
    private static final String SEND_TRANSPORT_CONTENT_COMMAND =
            "/usr/local/bin/call --cmd TSCFG";

    public static final String FLOOR_COMMAND =
            "/usr/local/bin/call --cmd FLOOR";
    public static final String ZUMMER_TYPE_COMMAND =
            "/usr/local/bin/call --cmd SNDTYPE";

    public static final String MODEM_CONFIG_MEGAF_BEELINE_COMMAND =
            "sudo sed -I ’s/megafon-m2m/beeline-m2m/g’ /etc/init.d/S99stp-tools";
    public static final String MODEM_CONFIG_BEELINE_MEGAF_COMMAND =
            "sudo sed -I ’s/beeline-m2m/megafon-m2m/g’ /etc/init.d/S99stp-tools";


    private final OnTaskCompleted listener;

    private final String ip;
    private int resultCode;

    public SshConnection(String ip, OnTaskCompleted listener){
        this.ip = ip;
        this.listener = listener;
    }


        //req[0] - command
    protected String doInBackground(Object...req) {
        String res = "";
        Session session = null;
        try {
            session = SshUtils.getSession(ip);
            this.resultCode = (Integer) req[0];
            Logger.d(TAG, ip + " isConnected: " + session.isConnected());
            String cmd;
            switch (resultCode) {
                case REBOOT_CODE:
                case REBOOT_STM_CODE:
                case CLEAR_RASP_CODE:
                    cmd = getExecCommand(resultCode);
                    res = SshUtils.execCommand(session, cmd);
                    break;
                case SEND_TRANSPORT_CONTENT_CODE:
                    cmd = SEND_TRANSPORT_CONTENT_COMMAND + " " + req[1] + " " + req[2] + " " + req[3] + " " + req[4];
                    res = SshUtils.execCommand(session, cmd);
                    break;
                case SEND_STATION_CONTENT_CODE:
                    cmd = (String) req[1];
                    res = SshUtils.execCommand(session, cmd);
                    break;
            }
        } catch (Exception e){
            Logger.d(TAG, "exception: " + e.getMessage() + " " + e.getCause());
                res = e.getMessage();
                this.resultCode = SSH_ERROR_CODE;
        } finally {
            if(session != null) {
                session.disconnect();
            }
        }
        return res;
    }

    private String getExecCommand(int taskCode){
        String cmd = "";
        switch (taskCode){
            case REBOOT_CODE:
                cmd =  REBOOT_COMMAND;
                break;
            case REBOOT_STM_CODE:
                cmd = REBOOT_STM_COMMAND;
                break;
            case CLEAR_RASP_CODE:
                cmd = CLEAR_RASP_COMMAND;
                break;
        }
        Logger.d(TAG, "getExecCommand(), taskCode: " + taskCode + ", cmd: " + cmd);
        return cmd;
    }

    protected void onPostExecute(String result) {
        if (listener != null) {
            Bundle bundle = new Bundle();
            bundle.putString(BundleKeys.IP_KEY, ip);
            bundle.putInt(BundleKeys.RESULT_CODE_KEY, resultCode);
            bundle.putString(BundleKeys.RESULT_KEY, result);
            listener.onTaskCompleted(bundle);
        }
    }
}