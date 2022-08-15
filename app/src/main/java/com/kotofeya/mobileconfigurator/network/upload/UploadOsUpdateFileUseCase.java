package com.kotofeya.mobileconfigurator.network.upload;

import android.os.AsyncTask;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.network.SshUtils;

import java.io.File;
import java.io.IOException;

public class UploadOsUpdateFileUseCase extends AsyncTask<Object, Object, String> implements TaskCode {

    private static final String TAG = UploadOsUpdateFileUseCase.class.getSimpleName();
    private static final String REBOOT_COMMAND = "sudo reboot";

    private UploadOsUpdateFileListener uploadOsUpdateFileListener;

    private final String ip;
    private String serial;
    private int transferred;
    private File file;

    public UploadOsUpdateFileUseCase(File file,
                                     String ip,
                                     String serial,
                                     UploadOsUpdateFileListener uploadOsUpdateFileListener){
        this.ip = ip;
        this.serial = serial;
        this.file = file;
        this.uploadOsUpdateFileListener = uploadOsUpdateFileListener;
    }

    protected String doInBackground(Object...req) {
        Logger.d(TAG, "start upload file: " + file.getName());
        Session session = null;
        try {
            session = SshUtils.getSession(ip);
            String cmd;
            transferred = 0;
            cmd = "sudo reboot";
            uploadToOverlayUpdate(session, file);
            String r = SshUtils.execCommand(session, cmd);
            return "success";
        } catch (JSchException | IOException e) {
            Logger.d(TAG, "exception: " + e.getMessage() + " " + e.getCause());
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
        return "failed";
    }

    @Override
    protected void onPostExecute(String string) {
        Logger.d(TAG, "onPostExecute(): " + ip + " " + string);
        super.onPostExecute(string);
        if(string.equals("success")){
            uploadOsUpdateFileListener.uploadFileSuccessful(file, serial, ip);
        } else {
            uploadOsUpdateFileListener.uploadFileFailed(file, serial, ip);
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
                    uploadOsUpdateFileListener.setProgress(0);
                }
                @Override
                public boolean count(long count) {
                    transferred += count;
                    double fileLength = (double) file.length();
                    uploadOsUpdateFileListener.setProgress((int) (100 * (transferred / fileLength)));
                    return true;
                }
                @Override
                public void end() {
                    Logger.d(TAG, "end transferring");
//                    uploadCoreUpdateFileListener.uploadFileSuccessful(file, iteration, serial, ip);
                }
            });
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
        } finally {
            if(channelSftp != null){
                channelSftp.disconnect();
            }
        }
    }

}