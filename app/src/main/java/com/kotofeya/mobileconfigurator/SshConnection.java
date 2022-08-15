package com.kotofeya.mobileconfigurator;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.kotofeya.mobileconfigurator.network.SshUtils;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

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

    private static final String CLEAR_ARCHIVE_DIR_COMMAND =
            "sudo rm -rf /var/www/html/data/archive/*";
    private static final String DELETE_UPDATE_STM_LOG_COMMAND =
            "sudo rm /var/www/html/data/stm_update_log";
    private static final String CREATE_UPDATE_STM_LOG_COMMAND =
            "sudo touch /var/www/html/data/stm_update_log";

    private final OnTaskCompleted listener;
    private ProgressBarInt progressBarIntListener;

    private final String ip;
    private int resultCode;
    private int transferred;

    public SshConnection(String ip, OnTaskCompleted listener){
        this.ip = ip;
        this.listener = listener;
    }

    public SshConnection(String ip, OnTaskCompleted listener, ProgressBarInt progressBarIntListener){
        this.ip = ip;
        this.listener = listener;
        this.progressBarIntListener = progressBarIntListener;
    }

        //req[0] - command
    protected String doInBackground(Object...req) {
        String res = "";
        Session session = null;
        File file;
        try {
            session = SshUtils.getSession(ip);
            this.resultCode = (Integer) req[0];
            Logger.d(TAG, ip + " isConnected: " + session.isConnected());
            String cmd;
            switch (resultCode) {
                case UPDATE_STM_UPLOAD_CODE:
                    res = SshUtils.execCommand(session, CLEAR_ARCHIVE_DIR_COMMAND);
                    file = new File((String) req[1]);
                    File binFile = getBinFromArchive(file);
                    uploadToOverlayUpdate(session, binFile);
                    cmd = getExecCommand(resultCode, binFile.getName());
                    res = SshUtils.execCommand(session, cmd);
                    break;
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

    private String getExecCommand(int taskCode, String fileName){
        String moveCommand = "sudo mv " + "/overlay/update/" +
                fileName + " /var/www/html/data/archive/" + fileName;
        String cmd = moveCommand + ";" + DELETE_UPDATE_STM_LOG_COMMAND +
                ";" + CREATE_UPDATE_STM_LOG_COMMAND + ";" + REBOOT_COMMAND;
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


    private void uploadToOverlayUpdate(Session session, File file){
        Logger.d(TAG, "uploading file: " + file.getName() +" length: " + file.length());
        ChannelSftp channelSftp = null;
        try {
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            channelSftp.put(file.getAbsolutePath(), "/overlay/update", new SftpProgressMonitor() {
                @Override
                public void init(int op, String src, String dest, long max) {
                    progressBarIntListener.clearProgressBar();
                    progressBarIntListener.setProgressBarVisibility(View.VISIBLE);
                }
                @Override
                public boolean count(long count) {
                    transferred += count;
                    double fileLength = (double) file.length();
                    listener.onProgressUpdate((int) (100 * (transferred / fileLength)));
                    return true;
                }
                @Override
                public void end() {
                    Logger.d(TAG, "end transferring");
                    progressBarIntListener.setProgressBarVisibility(View.GONE);
                }
            });
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
            this.resultCode = SSH_ERROR_CODE;
        } finally {
            if(channelSftp != null){
                channelSftp.disconnect();
            }
        }
    }
    private File getBinFromArchive(File file){
        File binFile = null;
        try (FileInputStream in = new FileInputStream(file);
             BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
             TarArchiveInputStream tarIn = new TarArchiveInputStream(bzIn)){
        ArchiveEntry entry;

        while (null != (entry = tarIn.getNextEntry())){
            if (entry.getSize() < 1){
                continue;
            }
            Logger.d(TAG, "tar entry: " + entry.getName() + ", isContainsBin: " + entry.getName().contains(".bin"));

            if(entry.getName().contains(".bin")){
                if(entry.getName().contains("static")){
                    // TODO: 13.11.2020 mobile? 
                    binFile = new File(file.getParent() + "/mobile" + entry.getName().substring(entry.getName().lastIndexOf("_")));
                }
                else if(entry.getName().contains("mobile")){
                    String s = entry.getName().substring(entry.getName().indexOf("ver.") + 4);
                    if(s.contains("_")){
                        s = s.replace("_", ".");
                    }
                    binFile = new File(file.getParent() + "/mobile_" + s);
                }
//                Logger.d(TAG, "bin file size: " + binFile.length() + ", bin file name: " + binFile.getName());
                try(FileOutputStream fileOutputStream = new FileOutputStream(binFile)) {
                    int i;
                    while ((i = tarIn.read()) > 0) {
                        fileOutputStream.write(i);
                    }
//                    Logger.d(TAG, "bin file size: " + binFile.length() + ", bin file name: " + binFile.getName());
                }
            }
        }
        } catch (Exception e) {
            e.printStackTrace();
            this.resultCode = SSH_ERROR_CODE;
        }
        return binFile;
    }
}