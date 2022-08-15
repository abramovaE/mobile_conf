package com.kotofeya.mobileconfigurator.network.upload;

import android.os.AsyncTask;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.data.TempFilesRepositoryImpl;
import com.kotofeya.mobileconfigurator.domain.tempfiles.GetCoreUpdateFilesUseCase;
import com.kotofeya.mobileconfigurator.network.SshUtils;

import java.io.File;
import java.io.IOException;

public class UploadCoreUpdateFileUseCase extends AsyncTask<Object, Object, String> implements TaskCode {

    private static final String TAG = UploadCoreUpdateFileUseCase.class.getSimpleName();
    private static final String REBOOT_COMMAND = "sudo reboot";

    private UploadCoreUpdateFileListener uploadCoreUpdateFileListener;

    private final String ip;
    private String serial;
    private int iteration;
    private int transferred;
    private File file;

    private TempFilesRepositoryImpl tempFilesRepository = TempFilesRepositoryImpl.getInstance();
    private GetCoreUpdateFilesUseCase getCoreUpdateFilesUseCase =
            new GetCoreUpdateFilesUseCase(tempFilesRepository);

    public UploadCoreUpdateFileUseCase(File file,
                                       String ip,
                         int filePosition,
                         String serial,
                         UploadCoreUpdateFileListener uploadCoreUpdateFileListener){
        this.ip = ip;
        this.serial = serial;
        this.iteration = filePosition;
        this.file = file;
        this.uploadCoreUpdateFileListener = uploadCoreUpdateFileListener;
    }

    protected String doInBackground(Object...req) {
        Logger.d(TAG, "start upload file: " + file.getName());
        Session session = null;
        try {
            session = SshUtils.getSession(ip);
            String cmd;
            transferred = 0;

            File[] coreUpdateFiles = getCoreUpdateFilesUseCase.getCoreUpdateFiles();
            if (iteration < coreUpdateFiles.length) {
                String fileName = file.getName();
//                res = getCoreResultString(iteration,  fileName);
                if (iteration == 2) {
                    Logger.d(TAG, "fileName: " + fileName);
                    uploadToOverlayUpdate(session, file);
                    iteration += 1;
                    file = coreUpdateFiles[iteration];
                    fileName = file.getName();
                }
                Logger.d(TAG, "fileName: " + fileName);
                uploadToOverlayUpdate(session, file);
                cmd = getCoreExecCommand(iteration, fileName);
                String res = SshUtils.execCommand(session, cmd);
//                iteration += 1;
            }
            return "success";
//            uploadCoreUpdateFileListener.uploadFileSuccessful(file, iteration,serial, ip);

        } catch (JSchException | IOException e) {
            Logger.d(TAG, "exception: " + e.getMessage() + " " + e.getCause());

//            uploadCoreUpdateFileListener.uploadFileFailed(file, iteration);
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
            uploadCoreUpdateFileListener.uploadFileSuccessful(file, iteration,serial, ip);
        } else {
            uploadCoreUpdateFileListener.uploadFileFailed(file, iteration, serial, ip);
        }
    }

    private String getCoreExecCommand(int iteration, String fileName){
        String renameCommand;
        String cmd = "";
        switch (iteration){
            case 0:
                renameCommand = "sudo mv " + "/overlay/update/" + fileName + " /overlay/update/" + "root.img.bz2";
                cmd = renameCommand + ";" + REBOOT_COMMAND;
                break;
            case 1:
                cmd = REBOOT_COMMAND;
                break;
            case 3:
                renameCommand = "sudo mv " + "/overlay/update/" + fileName + " /overlay/update/" + "root-new.img.bz2";
                cmd = renameCommand + ";" + REBOOT_COMMAND;
                break;
        }
        Logger.d(TAG, "getCoreExecCommand(), iteration: " + iteration + ", cmd: " + cmd);
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
                    uploadCoreUpdateFileListener.setProgress(0);
                }
                @Override
                public boolean count(long count) {
                    transferred += count;
                    double fileLength = (double) file.length();
                    uploadCoreUpdateFileListener.setProgress((int) (100 * (transferred / fileLength)));
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