package com.kotofeya.mobileconfigurator.network;

import android.os.Bundle;

import com.google.gson.GsonBuilder;
import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.ProgressBarInt;
import com.kotofeya.mobileconfigurator.fragments.config.ContentFragment;
import com.kotofeya.mobileconfigurator.network.post_response.TakeInfoFull;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class PostInfo implements Runnable {

    private  String ip;
    private OnTaskCompleted listener;
    private ProgressBarInt progressBarIntListener;

    private String command;
    private String version;

    public PostInfo(OnTaskCompleted listener, String ip, String command) {
        Logger.d(Logger.POST_INFO_LOG, "new post: " + command + ", ip: " + ip);
        this.listener = listener;
        this.command = command;
        this.ip = ip;
    }

    public PostInfo(OnTaskCompleted listener, String ip, String command, String version) {
        Logger.d(Logger.POST_INFO_LOG, "new post: " + command + ", ip: " + ip + ", version: " + version);
        this.listener = listener;
        this.command = command;
        this.ip = ip;
        this.version = version;
    }

    @Override
    public void run() {
        Logger.d(Logger.POST_INFO_LOG, "post command: " +  command);
        Bundle result = new Bundle();
        URL u;
        try {
            u = new URL(getUrl(ip, command));
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

            Logger.d(Logger.POST_INFO_LOG, "post command: " +  command +", response: " + response);
            if(command.startsWith(PostCommand.TRANSP_CONTENT)){
                command = PostCommand.TRANSP_CONTENT;
            }
            if (command.startsWith(PostCommand.WIFI)){
                command = PostCommand.WIFI;
            }
            if (command.startsWith(PostCommand.STATIC)){
                command = PostCommand.STATIC;
            }
            if (command.startsWith(PostCommand.FLOOR)){
                command = PostCommand.FLOOR;
            }
            if (command.startsWith(PostCommand.SOUND)){
                command = PostCommand.SOUND;
            }

            result.putString(BundleKeys.COMMAND_KEY, command);
            result.putString(BundleKeys.IP_KEY, ip);

            Logger.d(Logger.POST_INFO_LOG, "post command: " +  command +", response: " + response);

            if(response == 200){
                switch (command){
                    case PostCommand.TAKE_INFO_FULL:
                        JSONObject jsonObject = new JSONObject(content.toString()
                                .replace("<pre>", "")
                                .replace("</pre>", ""));
                        String command = jsonObject.getString(BundleKeys.COMMAND_KEY);
                        JSONObject properties = jsonObject.getJSONObject("properties");
                        double version = getVersion();
                        TakeInfoFull takeInfoFull = new GsonBuilder().setVersion(version).create().fromJson(properties.toString(),  TakeInfoFull.class);
                        result.putParcelable(BundleKeys.PARCELABLE_RESPONSE_KEY, takeInfoFull);
                        result.putString(BundleKeys.VERSION_KEY, this.version);
                        break;
                    case PostCommand.VERSION:
                        String ver = content.toString().substring(content.lastIndexOf("version") + 8);
                        result.putString(BundleKeys.RESPONSE_KEY, ver);
                        break;
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
            } else {
                result.putString(BundleKeys.COMMAND_KEY, PostCommand.POST_COMMAND_ERROR);
            }
            Logger.d(Logger.POST_INFO_LOG, "listener: " + listener);
            reader.close();
        } catch (MalformedURLException | ProtocolException e) {
            Logger.d(Logger.POST_INFO_LOG, "exception: " + e.getMessage());
            result.putString(BundleKeys.COMMAND_KEY, PostCommand.POST_COMMAND_ERROR);
            result.putString(BundleKeys.RESPONSE_KEY, e.getMessage());
        } catch (IOException e) {
            Logger.d(Logger.POST_INFO_LOG, "io exception: " + e.getMessage());
            e.printStackTrace();
            result.putString(BundleKeys.COMMAND_KEY, PostCommand.POST_COMMAND_ERROR);
            result.putString(BundleKeys.RESPONSE_KEY, e.getMessage());
            result.putString(BundleKeys.IP_KEY, ip);
        } catch (JSONException e) {
            Logger.d(Logger.POST_INFO_LOG, "exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            listener.onTaskCompleted(result);
        }
    }

    private String getUrl(String ip, String command)  throws IOException{
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("timeN", "14.07.21_16.32"));
        params.add(new BasicNameValuePair("user", "dirvion"));
        params.add(new BasicNameValuePair("secret", "fasterAnDfaster"));
        params.add(new BasicNameValuePair("command", command));
        return "http://" + ip + "/interface/index.php?" + getQuery(params);
    }

    private HttpURLConnection getConnection(URL url) throws IOException{
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("POST");
        c.setConnectTimeout(12000);
        c.setReadTimeout(15000);
        c.connect();
        return c;
    }

    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (NameValuePair pair : params) {
            if (first)
                first = false;
            else
                result.append("&");
            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }
        return result.toString();
    }

    private double getVersion(){
        if(version.startsWith("0")){
            return Double.parseDouble(version.replaceFirst("0.", ""));
        }
//        switch (version){
//            case "0.1.6":
//                return 1.6;
//            case "0.1.7":
//                return 1.7;
//
//        }
        return 0.0;
    }
}
