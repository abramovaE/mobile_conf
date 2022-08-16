package com.kotofeya.mobileconfigurator.network;

import android.os.Bundle;

import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.fragments.config.ContentFragment;
import com.kotofeya.mobileconfigurator.network.request.SettingsUpdatePhpListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class PostInfo implements Runnable {

    private static final String TAG = PostInfo.class.getSimpleName();

    private  String ip;
    private OnTaskCompleted listener;
    private SettingsUpdatePhpListener settingsUpdatePhpListener;
    private String urlCommand;

    public PostInfo(OnTaskCompleted listener, String ip, String urlCommand) {
        Logger.d(TAG, "PostInfo(): " + urlCommand + ", ip: " + ip);
        this.listener = listener;
        this.urlCommand = urlCommand;
        this.ip = ip;
    }

//    public PostInfo(SettingsUpdatePhpListener settingsUpdatePhpListener, String ip, String urlCommand) {
//        Logger.d(TAG, "PostInfo(): " + urlCommand + ", ip: " + ip);
//        this.settingsUpdatePhpListener = settingsUpdatePhpListener;
//        this.urlCommand = urlCommand;
//        this.ip = ip;
//    }

    private String formatCommand(){
        if (urlCommand.startsWith(PostCommand.TRANSP_CONTENT)) {
            return PostCommand.TRANSP_CONTENT;
        }
        if (urlCommand.startsWith(PostCommand.WIFI)) {
            return PostCommand.WIFI;
        }
        if (urlCommand.startsWith(PostCommand.STATIC)) {
            return PostCommand.STATIC;
        }
        if (urlCommand.startsWith(PostCommand.FLOOR)) {
            return PostCommand.FLOOR;
        }
        if (urlCommand.startsWith(PostCommand.SOUND)) {
            return PostCommand.SOUND;
        }
        return urlCommand;
    }

    @Override
    public void run() {
        Logger.d(TAG, "run(), command: " + formatCommand());
        Bundle result = new Bundle();
        URL u;
        String formattedCommand = formatCommand();

        result.putString(BundleKeys.IP_KEY, ip);
        result.putString(BundleKeys.COMMAND_KEY, formattedCommand);

        try {
            u = new URL(NetworkUtils.getUrl(ip, urlCommand));
            Logger.d(TAG, "url: " + u);

            HttpURLConnection httpsURLConnection = getConnection(u);
            httpsURLConnection.connect();

            int response = httpsURLConnection.getResponseCode();
            BufferedReader br;
            StringBuilder content;
            InputStreamReader reader = new InputStreamReader(httpsURLConnection.getInputStream());
            br = new BufferedReader(reader);
            content = new StringBuilder();
            String line;
            while (null != (line = br.readLine())) {
                content.append(line);
            }
            Logger.d(TAG, "response: " + response);

            if (response == 200) {
                switch (formattedCommand) {
                    case PostCommand.STM_UPDATE_LOG:
                    case PostCommand.STM_UPDATE_LOG_CLEAR:
                    case PostCommand.ERASE_CONTENT:
                    case PostCommand.TRANSP_CONTENT:
                    case PostCommand.REBOOT + "_" + ContentFragment.REBOOT_RASP:
                    case PostCommand.REBOOT + "_" + ContentFragment.REBOOT_STM:
                    case PostCommand.REBOOT + "_" + ContentFragment.REBOOT_ALL:
                    case PostCommand.READ_WPA:
                    case PostCommand.WIFI_CLEAR:
                    case PostCommand.WIFI:
                    case PostCommand.STATIC:
                    case PostCommand.READ_NETWORK:
                    case PostCommand.NETWORK_CLEAR:
                    case PostCommand.SCUART:
                    case PostCommand.UPDATE_PHP:
                    case PostCommand.FLOOR:
                    case PostCommand.SOUND:
                    case PostCommand.VOLUME:
                        result.putString(BundleKeys.RESPONSE_KEY, content.toString());
                        break;
                }

//                httpsURLConnection.disconnect();
            } else {
                result.putString(BundleKeys.ERROR_MESSAGE, "responseCode =  " + response);
                result.putString(BundleKeys.COMMAND_KEY, PostCommand.POST_COMMAND_ERROR);
            }
//            Logger.d(TAG, "listener: " + listener + ", ip: " + ip);
            reader.close();


        }catch (MalformedURLException | ProtocolException e) {
//            Logger.d(TAG, "exception: " + e.getMessage() + ", ip: " + ip);
            result.putString(BundleKeys.COMMAND_KEY, PostCommand.POST_COMMAND_ERROR);
            result.putString(BundleKeys.RESPONSE_KEY, e.getMessage());
            result.putString(BundleKeys.ERROR_MESSAGE, "MalformedURLException | ProtocolException e =  " + e.getMessage());
        } catch (IOException e) {
            Logger.d(TAG, "io exception: " + e.getMessage() +
                    ", ip: " + ip + ", command: " + formattedCommand + ", cause" + e.getCause());
            e.printStackTrace();
            result.putString(BundleKeys.COMMAND_KEY, PostCommand.POST_COMMAND_ERROR);
            result.putString(BundleKeys.RESPONSE_KEY, e.getMessage());
            result.putString(BundleKeys.ERROR_MESSAGE, "IOException e =  " + e.getMessage());
        }finally {
            Logger.d(TAG, "result: " + result);
            listener.onTaskCompleted(result);
        }
    }

    private HttpURLConnection getConnection(URL url) throws IOException{
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("GET");
        c.setConnectTimeout(12000);
        c.setReadTimeout(15000);
        c.connect();
        return c;
    }
}
