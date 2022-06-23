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

    private static final String TAG = PostInfo.class.getSimpleName();

    private  String ip;
    private OnTaskCompleted listener;
    private ProgressBarInt progressBarIntListener;

    private String urlCommand;
    private String version;

    public PostInfo(OnTaskCompleted listener, String ip, String urlCommand) {
        Logger.d(TAG, "PostInfo(): " + urlCommand + ", ip: " + ip);
        this.listener = listener;
        this.urlCommand = urlCommand;
        this.ip = ip;
    }

    public PostInfo(OnTaskCompleted listener, String ip, String urlCommand, String version) {
        Logger.d(TAG, "PostInfo: " + urlCommand + ", ip: " + ip + ", version: " + version);
        this.listener = listener;
        this.urlCommand = urlCommand;
        this.ip = ip;
        this.version = version;
    }

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
        Bundle result = new Bundle();
        URL u;
        String formattedCommand = formatCommand();
        result.putString(BundleKeys.IP_KEY, ip);
        result.putString(BundleKeys.COMMAND_KEY, formattedCommand);

        try {
            u = new URL(getUrl(ip, urlCommand));
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
            Logger.d(TAG, "run(), content: " + content);

            if (response == 200) {
                switch (formattedCommand) {
                    case PostCommand.TAKE_INFO_FULL:
                        JSONObject jsonObject = new JSONObject(content.toString()
                                .replace("<pre>", "")
                                .replace("</pre>", ""));
                        JSONObject properties = jsonObject.getJSONObject("properties");
                        double version = getVersion();
                        result.putString(BundleKeys.VERSION_KEY, version + "");
                        Logger.d(TAG, "run(), version: " + version + ", props: " + properties);
                        TakeInfoFull takeInfoFull = new GsonBuilder().setVersion(version).create().fromJson(properties.toString(), TakeInfoFull.class);
                        Logger.d(TAG, "run(), takeInfoFull: " + takeInfoFull + ", ip: " + ip);
                        result.putParcelable(BundleKeys.PARCELABLE_RESPONSE_KEY, takeInfoFull);
                        break;
                    case PostCommand.VERSION:
                        String ver = content.substring(content.lastIndexOf("version") + 8);
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
                result.putString(BundleKeys.ERROR_MESSAGE, "responseCode =  " + response);
                result.putString(BundleKeys.COMMAND_KEY, PostCommand.POST_COMMAND_ERROR);
            }
            Logger.d(TAG, "listener: " + listener + ", ip: " + ip);
            reader.close();
        }catch (MalformedURLException | ProtocolException e) {
            Logger.d(TAG, "exception: " + e.getMessage() + ", ip: " + ip);
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
        } catch (JSONException e) {
                Logger.d(TAG, "exception: " + e.getMessage() + ", ip: " + ip);
                e.printStackTrace();
            result.putString(BundleKeys.ERROR_MESSAGE, "JSONException e =  " + e.getMessage());
        } finally {
            Logger.d(TAG, "result: " + result  + ", ip: " + ip);
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
        Logger.d(TAG, "getVersion(), ip: " + ip + ", version: " + version);
        if(version.startsWith("0")){
            return Double.parseDouble(version.replaceFirst("0.", ""));
        }
        return 0.0;
    }
}


