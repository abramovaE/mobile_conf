package com.kotofeya.mobileconfigurator.network;


import android.os.Bundle;

import com.google.gson.GsonBuilder;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
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
    private String command;
    private String version;
    public static String COMMAND = "command";
    public static String VERSION = "version";

    public static String IP = "ip";
    public static String RESPONSE = "response";
    public static String PARCELABLE_RESPONSE = "parc_response";

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
//            else if(command.startsWith(PostCommand.REBOOT)){
//                command = PostCommand.REBOOT;
//            }

            result.putString(COMMAND, command);
            result.putString(IP, ip);



            Logger.d(Logger.POST_INFO_LOG, "post command: " +  command +", response: " + response);

            if(response == 200){
                switch (command){
                    case PostCommand.TAKE_INFO_FULL:
                        JSONObject jsonObject = new JSONObject(content.toString()
                                .replace("<pre>", "")
                                .replace("</pre>", ""));
                        String command = jsonObject.getString(COMMAND);
                        JSONObject properties = jsonObject.getJSONObject("properties");

                        Logger.d(Logger.POST_INFO_LOG, COMMAND + " : " + command);
                        Logger.d(Logger.POST_INFO_LOG, "properties: " + properties);
                        double version = getVersion();
                        Logger.d(Logger.POST_INFO_LOG, "version: " + version);
                        TakeInfoFull takeInfoFull = new GsonBuilder().setVersion(version).create().fromJson(properties.toString(),  TakeInfoFull.class);
                        Logger.d(Logger.POST_INFO_LOG, "takeInfoFull created: " + (takeInfoFull != null));
                        Logger.d(Logger.POST_INFO_LOG, "takeInfoFull serial: " + takeInfoFull.getSerial());
                        result.putParcelable(PARCELABLE_RESPONSE, takeInfoFull);
                        result.putString(VERSION, this.version);
                        break;
                    case PostCommand.VERSION:
                        String ver = content.toString().substring(content.lastIndexOf("version") + 8);
                        Logger.d(Logger.POST_INFO_LOG, "version: " + ver);
                        result.putString(RESPONSE, ver);
                        break;
                    case PostCommand.STM_UPDATE_LOG:
                        break;
                    case PostCommand.STM_UPDATE_LOG_CLEAR:
                        break;
                    case PostCommand.ERASE_CONTENT:
                        result.putString(RESPONSE, content.toString());
                        break;
//                    case PostCommand.HARD_RESET:
//                        break;
//                    case PostCommand.SC_UART:
//                        break;
                    case PostCommand.TRANSP_CONTENT:
                        Logger.d(Logger.POST_INFO_LOG, "tr content response: " + content.toString());
                        result.putString(RESPONSE, content.toString());
                        break;

                    case PostCommand.REBOOT + "_" + ContentFragment.REBOOT_RASP:
                    case PostCommand.REBOOT + "_" + ContentFragment.REBOOT_STM:
                    case PostCommand.REBOOT + "_" + ContentFragment.REBOOT_ALL:
                        result.putString(RESPONSE, content.toString());
                        break;


                }
            } else {
                result.putString(PostInfo.COMMAND, PostCommand.POST_COMMAND_ERROR);
            }
            Logger.d(Logger.POST_INFO_LOG, "listener: " + listener);
            reader.close();
            listener.onTaskCompleted(result);
        } catch (MalformedURLException e) {
            Logger.d(Logger.POST_INFO_LOG, "exception: " + e.getMessage());
            result.putString(PostInfo.COMMAND, PostCommand.POST_COMMAND_ERROR);
            result.putString(RESPONSE, e.getMessage());
        } catch (ProtocolException e) {
            Logger.d(Logger.POST_INFO_LOG, "exception: " + e.getMessage());
//            e.printStackTrace();
            result.putString(PostInfo.COMMAND, PostCommand.POST_COMMAND_ERROR);
            result.putString(RESPONSE, e.getMessage());
        } catch (IOException e) {
            Logger.d(Logger.POST_INFO_LOG, "exception: " + e.getMessage());
            e.printStackTrace();
            result.putString(PostInfo.COMMAND, PostCommand.POST_COMMAND_ERROR);
            result.putString(RESPONSE, e.getMessage());
        } catch (JSONException e) {
            Logger.d(Logger.POST_INFO_LOG, "exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getUrl(String ip, String command)  throws IOException{
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("timeN", "14.04.21_16.32"));
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
        switch (version){
            case "0.1.6":
                return 1.6;
            case "0.1.7":
                return 1.7;
        }
        return 0.0;
    }
}
