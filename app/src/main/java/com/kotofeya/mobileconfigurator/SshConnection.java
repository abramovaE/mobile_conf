package com.kotofeya.mobileconfigurator;

import android.os.AsyncTask;
import android.os.Bundle;


import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SshConnection extends AsyncTask<Object, Object, String> implements TaskCode{

    private static final String REBOOT_COMMAND = "sudo reboot";
    private static final String REBOOT_STM_COMMAND =  "/usr/local/bin/call --cmd REST 0";
    private static final String CLEAR_RASP_COMMAND = "/sudo rm - f /var/www/html/data/*/* /var/www/html/data/*";
    private static final String SEND_TRANSPORT_CONTENT_COMMAND = "/usr/local/bin/call --cmd TSCFG";
    private static final String SEND_STATION_CONTENT_COMMAND = "send station content command";

    public static final String FLOOR_COMMAND = "/usr/local/bin/call --cmd FLOOR";
    public static final String ZUMMER_TYPE_COMMAND = "/usr/local/bin/call --cmd SNDTYPE";
    public static final String ZUMMER_VOLUME_COMMAND = "";
    public static final String MODEM_CONFIG_MEGAF_BEELINE_COMMAND = "sudo sed -I ’s/megafon-m2m/beeline-m2m/g’ /etc/init.d/S99stp-tools";
    public static final String MODEM_CONFIG_BEELINE_MEGAF_COMMAND = "sudo sed -I ’s/beeline-m2m/megafon-m2m/g’ /etc/init.d/S99stp-tools";

    private static final String CLEAR_ARCHIVE_DIR_COMMAND = "sudo rm -rf /var/www/html/data/archive/*";
    private static final String DELETE_UPDATE_STM_LOG_COMMAND = "sudo rm /var/www/html/data/stm_update_log";
    private static final String CREATE_UPDATE_STM_LOG_COMMAND = "sudo touch /var/www/html/data/stm_update_log";

    private OnTaskCompleted listener;
    private ProgressBarInt progressBarIntListener;

    private String ip;
    private int resultCode;

    private static Map<String, Integer> coreUpdateIteration = new HashMap<>();


    public SshConnection(OnTaskCompleted listener){
        Logger.d(Logger.SSH_CONNECTION_LOG, "new ssh connection, coreupdmap: " + coreUpdateIteration);
        this.listener = listener;
    }

    public SshConnection(OnTaskCompleted listener, ProgressBarInt progressBarIntListener){
        Logger.d(Logger.SSH_CONNECTION_LOG, "new ssh connection, coreupdmap: " + coreUpdateIteration);
            this.listener = listener;
            this.progressBarIntListener = progressBarIntListener;
    }

    int transferred;
        // req[0] - ip
        //req[1] - command

    protected String doInBackground(Object...req) {

        String res = "";
        Session session = null;
        ChannelSftp channelSftp = null;
        ChannelExec channelExec = null;
        Channel channel = null;
        String filePath;
        File file;
        File binFile;
        String moveCommand;

        try {
            JSch jsch = new JSch();
            this.ip = (String) req[0];
            this.resultCode = (Integer) req[1];

            Logger.d(Logger.SSH_CONNECTION_LOG, "try ssh connection, ip: " + ip);

            session = jsch.getSession("staff", ip, 22);
            session.setPassword("staff");

            ByteArrayOutputStream baos = null;
            // Avoid asking for key confirmation
            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");



            session.setConfig(prop);

            Logger.d(Logger.SSH_CONNECTION_LOG, ip + " config is set, connecting");
            session.connect();
            Logger.d(Logger.SSH_CONNECTION_LOG, ip + " isConnected: " + session.isConnected());

            switch (resultCode) {

                case UPDATE_OS_UPLOAD_CODE:
                    Logger.d(Logger.SSH_CONNECTION_LOG, "result code: " + resultCode + ", exec command: " + REBOOT_COMMAND);
                    transferred = 0;
                    uploadToOverlayUpdate(session, new File(App.get().getUpdateOsFilePath()));
                    execCommand(session, REBOOT_COMMAND);
                    break;

                case UPDATE_STM_UPLOAD_CODE:
                    execCommand(session, CLEAR_ARCHIVE_DIR_COMMAND);
                    filePath = (String) req[2];
                    file = new File(filePath);
                    binFile = getBinFromArchive(file);
                    uploadToOverlayUpdate(session, binFile);
                    moveCommand = "sudo mv " + "/overlay/update/" + binFile.getName() + " /var/www/html/data/archive/" + binFile.getName();
                    Logger.d(Logger.SSH_CONNECTION_LOG, "result code: " + resultCode + ", exec command: " + moveCommand + ";" + DELETE_UPDATE_STM_LOG_COMMAND + ";" + CREATE_UPDATE_STM_LOG_COMMAND + ";" + REBOOT_COMMAND);
                    execCommand(session, moveCommand + ";" + DELETE_UPDATE_STM_LOG_COMMAND + ";" + CREATE_UPDATE_STM_LOG_COMMAND + ";" + REBOOT_COMMAND);
                    break;


                case UPDATE_CORE_UPLOAD_CODE:
                    Logger.d(Logger.SSH_CONNECTION_LOG, "update core uploading started, coreUpdateIteration: " + coreUpdateIteration);
                    transferred = 0;
                    int coreUpdateIterationInt = coreUpdateIteration.get(ip);

                    if(coreUpdateIterationInt < Downloader.tempUpdateCoreFiles.length) {
                        File f = Downloader.tempUpdateCoreFiles[coreUpdateIterationInt];
                        String fileName = f.getName();
                        String renameCommand = "";

                        switch (coreUpdateIterationInt){
                            case 0:
                                if (fileName.contains("root_prepare")) {
                                    uploadToOverlayUpdate(session, f);
                                    renameCommand = "sudo mv " + "/overlay/update/" + fileName + " /overlay/update/" + "root.img.bz2";
                                    execCommand(session, renameCommand + ";" + REBOOT_COMMAND);
                                    coreUpdateIterationInt += 1;
                                    res = fileName + " загружен на устройство. Трансивер перезагружается. " +
                                            "Обновите список трансиверов примерно через 3 минуты.";
                                }
                                break;
                            case 1:
                                if (fileName.contains("boot-old.img.bz2")) {
                                    uploadToOverlayUpdate(session, f);
                                    execCommand(session, REBOOT_COMMAND);
                                    coreUpdateIterationInt += 1;
                                    res = fileName + " загружен на устройство. Трансивер перезагружается. " +
                                            "Обновите список трансиверов примерно через 2 минуты.";
                                }
                                break;
                            case 2:
                                if (fileName.contains("boot-new.img.bz2")) {
                                    uploadToOverlayUpdate(session, f);
                                    renameCommand = "sudo mv " + "/overlay/update/" + fileName + " /overlay/update/" + "boot-new.img.bz2";
                                    execCommand(session, renameCommand);
                                    coreUpdateIterationInt += 1;

                                    uploadToOverlayUpdate(session, Downloader.tempUpdateCoreFiles[3]);
                                    renameCommand = "sudo mv " + "/overlay/update/" + fileName + " /overlay/update/" + "root-new.img.bz2";
                                    execCommand(session, renameCommand + ";" + REBOOT_COMMAND);
                                    coreUpdateIterationInt += 1;

                                    res = Downloader.tempUpdateCoreFiles[2].getName() + " " + Downloader.tempUpdateCoreFiles[3].getName() +
                                            " загружены на устройство. Трансивер перезагружается. " +
                                            "Обновите список трансиверов примерно через 5 минут";
                                }
                        }
                        coreUpdateIteration.put(ip, coreUpdateIterationInt);
                    }
                    if(coreUpdateIterationInt == Downloader.tempUpdateCoreFiles.length){
                        resetCoreFilesCounter(ip);
                    }
                    break;

                case UPDATE_TRANSPORT_CONTENT_UPLOAD_CODE:
                case UPDATE_STATION_CONTENT_UPLOAD_CODE:
//                    uploadMoveReboot(session, (String) req[2]);
                    transferred = 0;
                    filePath = (String) req[2];
                    file = new File(filePath);
                    Logger.d(Logger.SSH_CONNECTION_LOG, "update station upload file: " + file);
                    uploadToOverlayUpdate(session, file);
                    moveCommand = "sudo mv " + "/overlay/update/" + file.getName() + " /overlay/update/www-data/" + file.getName();
                    Logger.d(Logger.SSH_CONNECTION_LOG, "result code: " + resultCode + ", exec command: " + moveCommand + ";" + REBOOT_COMMAND);
                    execCommand(session, moveCommand + ";" + REBOOT_COMMAND);
                    break;

                case REBOOT_CODE:
                    Logger.d(Logger.SSH_CONNECTION_LOG, "result code: " + resultCode + ", exec command: " + REBOOT_COMMAND);
                    execCommand(session, REBOOT_COMMAND);
                    break;

                case REBOOT_STM_CODE:
                    Logger.d(Logger.SSH_CONNECTION_LOG, "result code: " + resultCode + ", exec command: " + REBOOT_STM_COMMAND);
                    res = execCommand(session, REBOOT_STM_COMMAND);
                    break;

                case CLEAR_RASP_CODE:
                    Logger.d(Logger.SSH_CONNECTION_LOG, "result code: " + resultCode + ", exec command: " + CLEAR_RASP_COMMAND);
                    res = execCommand(session, CLEAR_RASP_COMMAND);
                    break;

                case SEND_TRANSPORT_CONTENT_CODE:
                    String command = SEND_TRANSPORT_CONTENT_COMMAND + " " + req[2] + " " + req[3] + " " + req[4] + " " + req[5];
                    Logger.d(Logger.SSH_CONNECTION_LOG, "result code: " + resultCode + ", exec command: " + command);
                    res = execCommand(session, command);
                    break;

                case SEND_STATION_CONTENT_CODE:
                    String comm = (String) req[2];
                    Logger.d(Logger.SSH_CONNECTION_LOG, "result code: " + resultCode + ", exec command: " + comm);
                    res = execCommand(session, comm);
                    break;
            }
        }
        catch (Exception e){
            Logger.d(Logger.SSH_CONNECTION_LOG, "exception: " + e.getMessage());
                res = e.getMessage();
                this.resultCode = SSH_ERROR_CODE;
        }

        finally {
            if(channel != null){
                channel.disconnect();
            }
            if(channelExec != null){
                channelExec.disconnect();
            }
            if(channelSftp != null){
                channelSftp.disconnect();
            }
            if(session != null) {
                session.disconnect();
            }
        }
        return res;
    }

    protected void onPostExecute(String result) {
        if(resultCode != 0){
            Logger.d(Logger.SSH_CONNECTION_LOG, "resultCode: " + resultCode + ", result: " + result + ",ip: " + ip);
            Logger.d(Logger.SSH_CONNECTION_LOG, "listener != null: " + listener);
        }

        if (listener != null) {
            Bundle bundle = new Bundle();
            bundle.putString("ip", this.ip);
            bundle.putInt("resultCode", this.resultCode);
            bundle.putString("result", result);
            listener.onTaskCompleted(bundle);
        }
    }
    private String execCommand(Session session, String command) throws IOException {
        Logger.d(Logger.SSH_CONNECTION_LOG, "exec command: " + command);
        String res = "";
        ChannelExec channelExec = null;
        InputStream commandOutput = null;
        try {
            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);
            StringBuilder sb = new StringBuilder();
            channelExec.connect();
            commandOutput = channelExec.getInputStream();
            Thread.sleep(2000);
            int readByte = 0;
            while ((readByte = commandOutput.read()) != -1) {
                sb.append((char) readByte);
            }
            res = sb.toString();
        } catch (JSchException | IOException | InterruptedException e) {
            e.printStackTrace();
            this.resultCode = SSH_ERROR_CODE;
        }
        finally {
            if(commandOutput != null){
                commandOutput.close();
            }
            if(channelExec != null){
                channelExec.disconnect();
            }
        }
        return res;
    }
    private void uploadToOverlayUpdate(Session session, File file){
        Logger.d(Logger.SSH_CONNECTION_LOG, "uploading file: " + file.getName() +" length: " + file.length());

        ChannelSftp channelSftp = null;
        try {
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            channelSftp.put(file.getAbsolutePath(), "/overlay/update", new SftpProgressMonitor() {
                @Override
                public void init(int op, String src, String dest, long max) {
                    progressBarIntListener.clearProgressBar();
                    progressBarIntListener.setProgressBarVisible();
                }
                @Override
                public boolean count(long count) {
                    transferred += count;
                    Logger.d(Logger.SSH_CONNECTION_LOG, "transfered: " + transferred);
                    Double fileLength = Double.valueOf(file.length());
                    listener.onProgressUpdate((int) (100 * (transferred / fileLength)));
                    return true;
                }
                @Override
                public void end() {
                    Logger.d(Logger.SSH_CONNECTION_LOG, "end transfering");
                    progressBarIntListener.setProgressBarGone();
                }
            });
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
            this.resultCode = SSH_ERROR_CODE;
        }
        finally {
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
        ArchiveEntry entry = null;

        while (null != (entry = tarIn.getNextEntry())){
            if (entry.getSize() < 1){
                continue;
            }
            Logger.d(Logger.SSH_CONNECTION_LOG, "tar entry: " + entry.getName() + ", isContainsBin: " + entry.getName().contains(".bin"));

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
                Logger.d(Logger.SSH_CONNECTION_LOG, "bin file size: " + binFile.length() + ", bin file name: " + binFile.getName());
                try(FileOutputStream fileOutputStream = new FileOutputStream(binFile)) {
                    int i = 0;
                    while ((i = tarIn.read()) > 0) {
                        fileOutputStream.write(i);
                    }
                    Logger.d(Logger.SSH_CONNECTION_LOG, "bin file size: " + binFile.length() + ", bin file name: " + binFile.getName());
                }
            }
        }
        } catch (Exception e) {
            e.printStackTrace();
            this.resultCode = SSH_ERROR_CODE;
        }
        return binFile;
    }

    public static void resetCoreFilesCounter(String ip){
        Logger.d(Logger.SSH_CONNECTION_LOG, "core files counter: " + coreUpdateIteration);
        coreUpdateIteration.put(ip, 0);
    }

    public static int getCoreUpdateIteration(String ip){
        Logger.d(Logger.SSH_CONNECTION_LOG, "coreUpdateIteration: " + coreUpdateIteration);
        Logger.d(Logger.SSH_CONNECTION_LOG, "coreUpdateIteration: " + coreUpdateIteration.get(ip));
        if(coreUpdateIteration.containsKey(ip)) {
            return coreUpdateIteration.get(ip);
        }
        return 0;
    }
}