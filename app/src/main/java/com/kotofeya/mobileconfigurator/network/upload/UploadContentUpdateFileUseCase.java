package com.kotofeya.mobileconfigurator.network.upload;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.network.SshUtils;

import java.io.File;
import java.io.IOException;

public class UploadContentUpdateFileUseCase implements Runnable {

    private static final String TAG = UploadContentUpdateFileUseCase.class.getSimpleName();
    private static final String REBOOT_COMMAND = "sudo reboot";

    private final UploadContentUpdateFileListener uploadContentUpdateFileListener;

    private final String ip;
    private final String serial;
    private int transferred;
    private final File file;

    public UploadContentUpdateFileUseCase(File file,
                                          String ip,
                                          String serial,
                                          UploadContentUpdateFileListener uploadContentUpdateFileListener){
        this.ip = ip;
        this.serial = serial;
        this.file = file;
        this.uploadContentUpdateFileListener = uploadContentUpdateFileListener;
    }

    private String getExecCommand(String fileName){
//        String newFileName = (fileName.contains("_")) ? fileName.replace("_", "/") : fileName;
        String moveCommand = "sudo mv " + "/overlay/update/" + fileName + " /overlay/update/www-data/data.tar.bz2";
        String cmd = moveCommand + ";" + REBOOT_COMMAND;
        Logger.d(TAG, "getExecCommand(), cmd: " + cmd);
        return cmd;
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
                    uploadContentUpdateFileListener.setProgress(0);
                }
                @Override
                public boolean count(long count) {
                    transferred += count;
                    double fileLength = (double) file.length();
                    uploadContentUpdateFileListener.setProgress((int) (100 * (transferred / fileLength)));
                    return true;
                }
                @Override
                public void end() {
                    Logger.d(TAG, "end transferring");
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

    @Override
    public void run() {
        Logger.d(TAG, "start upload file: " + file.getName());
        Session session = null;
        try {
            session = SshUtils.getSession(ip);
            transferred = 0;
            uploadToOverlayUpdate(session, file);
            String cmd = getExecCommand(file.getName());
            SshUtils.execCommand(session, cmd);
            uploadContentUpdateFileListener.uploadContentFileSuccessful(file, serial, ip);
        } catch (JSchException | IOException | InterruptedException e) {
            uploadContentUpdateFileListener.uploadContentFileFailed(file, serial, ip);

        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }
}