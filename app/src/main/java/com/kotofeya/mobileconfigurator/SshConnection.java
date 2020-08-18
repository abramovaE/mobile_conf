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
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;

public class SshConnection extends AsyncTask<Object, Object, String> {

    public static final String TAKE_COMMAND = "take command";
    public static final String UPDATE_OS_LOAD_FILE_COMMAND = "update os command";
    public static final String UPDATE_STM_LOAD_FILE_COMMAND = "update stm command";

    public static final String REBOOT_COMMAND = "sudo reboot";
    public static final String REBOOT_STM_COMMAND =  "/usr/local/bin/call --cmd REST 0";

    public static final String CLEAR_RASP_COMMAND = "/sudo rm - f /var/www/html/data/*/* /var/www/html/data/*";

    public static final String SEND_TRANSPORT_CONTENT_COMMAND = "/user/local/bin/call --cmd TSCFG";
    public static final String SEND_STATION_CONTENT_COMMAND = "send station content command";

    public static final String FLOOR_COMMAND = "/user/local/bin/call --cmd FLOOR";
    public static final String ZUMMER_TYPE_COMMAND = "/user/local/bin/call --cmd SNDTYPE";
    public static final String ZUMMER_VOLUME_COMMAND = "";
    public static final String MODEM_CONFIG_MEGAF_BEELINE_COMMAND = "sudo sed -I ’s/megafon-m2m/beeline-m2m/g’ /etc/init.d/S99stp-tools";
    public static final String MODEM_CONFIG_BEELINE_MEGAF_COMMAND = "sudo sed -I ’s/beeline-m2m/megafon-m2m/g’ /etc/init.d/S99stp-tools";

    private static final String CLEAR_ARCHIVE_DIR_COMMAND = "sudo rm -rf /var/www/html/data/archive/*";
    private static final String DELETE_UPDATE_STM_LOG_COMMAND = "sudo rm /var/www/html/data/stm_update_log";
    private static final String CREATE_UPDATE_STM_LOG_COMMAND = "sudo touch /var/www/html/data/stm_update_log";

    private OnTaskCompleted listener;
    private String ip;

    public SshConnection(OnTaskCompleted listener){
            this.listener = listener;
    }

        // req[0] - ip
        //req[1] - command

    protected String doInBackground(Object...req) {
        String res = "";
        Session session = null;
        ChannelSftp channelSftp = null;
        ChannelExec channelExec = null;
        Channel channel = null;

        try {
            JSch jsch = new JSch();
            this.ip = (String) req[0];
            session = jsch.getSession("staff", ip, 22);
            session.setPassword("staff");
            ByteArrayOutputStream baos = null;
            // Avoid asking for key confirmation
            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");
            session.setConfig(prop);
            session.connect();
            Logger.d(Logger.SSH_CONNECTION_LOG, ip + " isConnected: " + session.isConnected());

            switch ((String) req[1]) {
                case TAKE_COMMAND:
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
                    res = baos.toString().substring(baos.toString().lastIndexOf("$load") + 6, baos.toString().lastIndexOf("$ exit"));
                    break;

                case UPDATE_OS_LOAD_FILE_COMMAND:
                    uploadToOverlayUpdate(session, Downloader.tempUpdateOsFile);
                    Logger.d(Logger.SSH_CONNECTION_LOG, "updateOsFile completed");
                    execCommand(session, REBOOT_COMMAND);
                    res = "updateos:" + ip;
                    break;

                case UPDATE_STM_LOAD_FILE_COMMAND:
                    execCommand(session, CLEAR_ARCHIVE_DIR_COMMAND);
                    String archiveDir = execCommand(session, "ls /var/www/html/data/archive/");
                    Logger.d(Logger.SSH_CONNECTION_LOG, "ls /var/www/html/data/archive/ result: " + archiveDir);

                    String filePath = (String) req[2];
                    File file = new File(filePath);

                    File binFile = getBinFromArchive(file);

                    Logger.d(Logger.SSH_CONNECTION_LOG, "file size: " + binFile.length() + ", file name: " + binFile.getName());
                    uploadToOverlayUpdate(session, binFile);
//                    String moveCommand = "sudo mv " + "/overlay/update/" + file.getName() + " /overlay/update/www-data/data.tar.bz2";
//                    execCommand(session, moveCommand + ";" + REBOOT_COMMAND);
                    String h = execCommand(session, "ls /overlay/update");
                    Logger.d(Logger.SSH_CONNECTION_LOG, "ls /overlay/update res: " + h);
                    String moveCommand = "sudo mv " + "/overlay/update/" + binFile.getName() + " /var/www/html/data/archive/" + binFile.getName();
                    String s = execCommand(session, moveCommand);
                    String r = execCommand(session, "ls /var/www/html/data/archive/");
                    Logger.d(Logger.SSH_CONNECTION_LOG, "ls /var/www/html/data/archive/ res: " + r);

                    execCommand(session, DELETE_UPDATE_STM_LOG_COMMAND + ";" + CREATE_UPDATE_STM_LOG_COMMAND);

                    String aftClearLogFileSize = execCommand(session, "du -h /var/www/html/data/stm_update_log");
                    Logger.d(Logger.SSH_CONNECTION_LOG, "log file size after clearing: " + aftClearLogFileSize);
                    execCommand(session, REBOOT_COMMAND);
                    res = "Downloaded" + ip;
                    break;

                case REBOOT_COMMAND:
                    execCommand(session, REBOOT_COMMAND);
                    res = "reboot:" + ip;
                    break;

                case REBOOT_STM_COMMAND:
                    execCommand(session, REBOOT_STM_COMMAND);
                    Logger.d(Logger.SSH_CONNECTION_LOG, "reboot stm res" + res);
                    break;

                case CLEAR_RASP_COMMAND:
                    execCommand(session, CLEAR_RASP_COMMAND);
                    Logger.d(Logger.SSH_CONNECTION_LOG, "clear rasp res" + res);
                    break;

                case SEND_TRANSPORT_CONTENT_COMMAND:
                    String command = SEND_TRANSPORT_CONTENT_COMMAND + " " + req[2] + " " + req[3] + " " + req[4] + " " + req[5];
                    execCommand(session, command);
                    Logger.d(Logger.SSH_CONNECTION_LOG, "send transport content res" + res);
                    break;

                case SEND_STATION_CONTENT_COMMAND:
                    String comm = (String) req[2];
                    execCommand(session, comm);
                    Logger.d(Logger.SSH_CONNECTION_LOG, "send station content" + res);
                    break;
            }
        }
        catch (Exception e){
                Logger.d(Logger.SSH_CONNECTION_LOG, "error: " + e.getMessage() + ", cause: " + e.getCause());
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
        Logger.d(Logger.SSH_CONNECTION_LOG, "result: " + result);
        if (listener != null) {
            Bundle bundle = new Bundle();
            bundle.putString("result", result);
            bundle.putString("ip", this.ip);
            listener.onTaskCompleted(bundle);
        }
    }




    private String execCommand(Session session, String command) throws IOException {
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
        ChannelSftp channelSftp = null;
        try {
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            channelSftp.put(file.getAbsolutePath(), "/overlay/update", new SftpProgressMonitor() {
                @Override
                public void init(int op, String src, String dest, long max) {
                }
                @Override
                public boolean count(long count) {
                    Logger.d(Logger.SSH_CONNECTION_LOG, "transfered: " + count);
                    return true;
                }
                @Override
                public void end() {
                    Logger.d(Logger.SSH_CONNECTION_LOG, "end transfering");
                }
            });
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
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
                binFile = new File(file.getParent() + "/" + entry.getName().substring(entry.getName().lastIndexOf("/")));
                FileOutputStream fileOutputStream = new FileOutputStream(binFile);
                int i = 0;
                while ((i = tarIn.read()) > 0){
                    fileOutputStream.write(i);
                }
                Logger.d(Logger.SSH_CONNECTION_LOG, "bin file size: " + binFile.length() + ", bin file name: " + binFile.getName());
                fileOutputStream.close();
            }
        }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return binFile;
    }


}
