package com.kotofeya.mobileconfigurator;

import android.os.AsyncTask;
import android.os.Bundle;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.Timer;


public class SshConnectionRunnable implements Runnable, TaskCode {

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
    private String ip;
    private int resultCode;
    String req2;
    String req3;
    String req4;
    String req5;


    public SshConnectionRunnable(OnTaskCompleted listener, Object...req){
        this.listener = listener;
        this.ip = (String) req[0];
        this.resultCode = (Integer) req[1];

        if(req.length > 2){
            this.req2 = (String) req[2];
        }
        if(req.length > 3) {
            this.req3 = (String) req[3];
        }
        if(req.length > 4){
            this.req4 = (String) req[4];
        }
        if(req.length > 5) {
            this.req5 = (String) req[5];
        }
    }
    int transferred;
    // req[0] - ip
    //req[1] - command


    @Override
    public void run() {
            String res = "";
            Session session = null;
            ChannelSftp channelSftp = null;
            ChannelExec channelExec = null;
            Channel channel = null;

            try {
                JSch jsch = new JSch();
                session = jsch.getSession("staff", ip, 22);
                session.setPassword("staff");
                ByteArrayOutputStream baos = null;
                // Avoid asking for key confirmation
                Properties prop = new Properties();
                prop.put("StrictHostKeyChecking", "no");
                session.setConfig(prop);
                session.connect();
                Logger.d(Logger.SSH_CONNECTION_LOG, ip + " isConnected: " + session.isConnected());

                switch (resultCode) {
                    case TAKE_CODE:
                        long start = System.currentTimeMillis();
                        Logger.d(Logger.SSH_CONNECTION_LOG, "start: " + start);

                        channel = session.openChannel("shell");
                        baos = new ByteArrayOutputStream();
                        OutputStream inputstream_for_the_channel = channel.getOutputStream();
                        PrintStream commander = new PrintStream(inputstream_for_the_channel, true);
                        channel.setOutputStream(baos, true);
                        channel.connect();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(App.get().getAssets().open("take.sh")));
                        String e;
                        while ((e = reader.readLine()) != null) {
                            commander.println(e);
                        }
                        do {
                            Thread.sleep(2000);
                        } while (!channel.isEOF());
                        reader.close();
                        commander.close();
                        res = baos.toString().substring(baos.toString().lastIndexOf("$typeT") + 7, baos.toString().lastIndexOf("$ exit"));
//                        Logger.d(Logger.SSH_CONNECTION_LOG, "baos: " + baos.toString());
                        long end = System.currentTimeMillis() - start;
                        Logger.d(Logger.SSH_CONNECTION_LOG, "end: " + end);

                        break;




//                    case UPDATE_OS_UPLOAD_CODE:
//                        transferred = 0;
//                        uploadToOverlayUpdate(session, new File(App.get().getUpdateOsFilePath()));
//                        execCommand(session, REBOOT_COMMAND);
//                        break;

//                    case UPDATE_STM_UPLOAD_CODE:
//                        execCommand(session, CLEAR_ARCHIVE_DIR_COMMAND);
//                        String filePath = req2;
//                        File file = new File(filePath);
//                        File binFile = getBinFromArchive(file);
//                        uploadToOverlayUpdate(session, binFile);
//                        String moveCommand = "sudo mv " + "/overlay/update/" + binFile.getName() + " /var/www/html/data/archive/" + binFile.getName();
//                        execCommand(session, moveCommand + ";" + DELETE_UPDATE_STM_LOG_COMMAND + ";" + CREATE_UPDATE_STM_LOG_COMMAND + ";" + REBOOT_COMMAND);
//                        break;

//                    case REBOOT_CODE:
//                        execCommand(session, REBOOT_COMMAND);
//                        break;

//                    case REBOOT_STM_CODE:
//                        res = execCommand(session, REBOOT_STM_COMMAND);
//                        break;

//                    case CLEAR_RASP_CODE:
//                        res = execCommand(session, CLEAR_RASP_COMMAND);
//                        break;

//                    case SEND_TRANSPORT_CONTENT_CODE:
//                        String command = SEND_TRANSPORT_CONTENT_COMMAND + " " + req2 + " " + req3 + " " + req4 + " " + req5;
//                        res = execCommand(session, command);
//                        break;
//
//                    case SEND_STATION_CONTENT_CODE:
//                        String comm = req2;
//                        res = execCommand(session, comm);
//                        break;
                }
            }
            catch (Exception e){
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


                if(resultCode != 0){
                    Logger.d(Logger.SSH_CONNECTION_LOG, "resultCode: " + resultCode + ", result: " + res + ",ip: " + ip);
                    Logger.d(Logger.SSH_CONNECTION_LOG, "listener != null: " + listener);
                }

                if (listener != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString("ip", this.ip);
                    bundle.putInt("resultCode", this.resultCode);
                    bundle.putString("result", res);
                    listener.onTaskCompleted(bundle);
                }
            }
    }


//    private String getTakeInfo(){
//        String res = "";
//        channel = session.openChannel("shell");
//        baos = new ByteArrayOutputStream();
//        OutputStream inputstream_for_the_channel = channel.getOutputStream();
//        PrintStream commander = new PrintStream(inputstream_for_the_channel, true);
//        channel.setOutputStream(baos, true);
//        channel.connect();
//        BufferedReader reader = new BufferedReader(new InputStreamReader(App.get().getAssets().open("take.sh")));
//        String e;
//        while ((e = reader.readLine()) != null) {
//            commander.println(e);
//        }
//        do {
//            Thread.sleep(2000);
//        } while (!channel.isEOF());
//        reader.close();
//        commander.close();
//        res = baos.toString().substring(baos.toString().lastIndexOf("$typeT") + 7, baos.toString().lastIndexOf("$ exit"));
//        return res;
//    }
//
//

//    private String execCommand(Session session, String command) throws IOException {
//        String res = "";
//        ChannelExec channelExec = null;
//        InputStream commandOutput = null;
//        try {
//            channelExec = (ChannelExec) session.openChannel("exec");
//            channelExec.setCommand(command);
//            StringBuilder sb = new StringBuilder();
//            channelExec.connect();
//            commandOutput = channelExec.getInputStream();
//            Thread.sleep(2000);
//            int readByte = 0;
//            while ((readByte = commandOutput.read()) != -1) {
//                sb.append((char) readByte);
//            }
//            res = sb.toString();
//        } catch (JSchException | IOException | InterruptedException e) {
//            e.printStackTrace();
//            this.resultCode = SSH_ERROR_CODE;
//        }
//        finally {
//            if(commandOutput != null){
//                commandOutput.close();
//            }
//            if(channelExec != null){
//                channelExec.disconnect();
//            }
//        }
//        return res;
//    }
//
//    private void uploadToOverlayUpdate(Session session, File file){
//        Logger.d(Logger.SSH_CONNECTION_LOG, "uploading file: " + file.getName() +" length: " + file.length());
//        ChannelSftp channelSftp = null;
//        try {
//            channelSftp = (ChannelSftp) session.openChannel("sftp");
//            channelSftp.connect();
//            channelSftp.put(file.getAbsolutePath(), "/overlay/update", new SftpProgressMonitor() {
//                @Override
//                public void init(int op, String src, String dest, long max) {
//                }
//                @Override
//                public boolean count(long count) {
//                    transferred += count;
//                    Logger.d(Logger.SSH_CONNECTION_LOG, "transfered: " + transferred);
//                    listener.onProgressUpdate((int) (100 * (transferred / 40755927.0)));
//                    return true;
//                }
//                @Override
//                public void end() {
//                    Logger.d(Logger.SSH_CONNECTION_LOG, "end transfering");
//                }
//            });
//        } catch (JSchException | SftpException e) {
//            e.printStackTrace();
//            this.resultCode = SSH_ERROR_CODE;
//        }
//        finally {
//            if(channelSftp != null){
//                channelSftp.disconnect();
//            }
//        }
//    }
//
//    private File getBinFromArchive(File file){
//        File binFile = null;
//        try (FileInputStream in = new FileInputStream(file);
//             BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
//             TarArchiveInputStream tarIn = new TarArchiveInputStream(bzIn)){
//            ArchiveEntry entry = null;
//
//            while (null != (entry = tarIn.getNextEntry())){
//                if (entry.getSize() < 1){
//                    continue;
//                }
//                Logger.d(Logger.SSH_CONNECTION_LOG, "tar entry: " + entry.getName() + ", isContainsBin: " + entry.getName().contains(".bin"));
//
//                if(entry.getName().contains(".bin")){
//                    if(entry.getName().contains("static")){
//                        binFile = new File(file.getParent() + "/mobile" + entry.getName().substring(entry.getName().lastIndexOf("_")));
//                    }
//                    else if(entry.getName().contains("mobile")){
//                        String s = entry.getName().substring(entry.getName().indexOf("ver.") + 4);
//                        if(s.contains("_")){
//                            s = s.replace("_", ".");
//                        }
//                        binFile = new File(file.getParent() + "/mobile_" + s);
//                    }
//                    Logger.d(Logger.SSH_CONNECTION_LOG, "bin file size: " + binFile.length() + ", bin file name: " + binFile.getName());
//
//                    FileOutputStream fileOutputStream = new FileOutputStream(binFile);
//                    int i = 0;
//                    while ((i = tarIn.read()) > 0){
//                        fileOutputStream.write(i);
//                    }
//                    Logger.d(Logger.SSH_CONNECTION_LOG, "bin file size: " + binFile.length() + ", bin file name: " + binFile.getName());
//                    fileOutputStream.close();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            this.resultCode = SSH_ERROR_CODE;
//        }
//        return binFile;
//    }
}
