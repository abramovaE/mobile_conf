package com.kotofeya.mobileconfigurator.network;

import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.internal.$Gson$Preconditions;
import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.network.post_response.TakeInfoFull;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
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
    public static String COMMAND = "command";
    public static String IP = "ip";
    public static String RESPONSE = "response";

    public PostInfo(OnTaskCompleted listener, String ip, String command) {
        this.listener = listener;
        this.command = command;
        this.ip = ip;
    }

    @Override
    public void run() {
        Logger.d(Logger.MAIN_LOG, "user: " + App.get().getLogin());
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

            result.putInt(COMMAND, PostCommand.getResponseCode(command));
            result.putString(IP, ip);

            if(response == 200){
                switch (command){
                    case PostCommand.TAKE_INFO_FULL:
                        JSONObject jsonObject = new JSONObject(content.toString()
                                .replace("<pre>", "")
                                .replace("</pre>", ""));
                        String command = jsonObject.getString("command");
                        JSONObject properties = jsonObject.getJSONObject("properties");
                        Logger.d(Logger.UTILS_LOG, "command: " + command);
                        Logger.d(Logger.UTILS_LOG, "properties: " + properties);
                        TakeInfoFull takeInfoFull = new GsonBuilder().create().fromJson(properties.toString(),  TakeInfoFull.class);
                        Logger.d(Logger.UTILS_LOG, "takeInfoFull: " + takeInfoFull);
                        break;



                    case PostCommand.VERSION:
                        break;
                    case PostCommand.STM_UPDATE_LOG:
                        break;
                    case PostCommand.STM_UPDATE_LOG_CLEAR:
                        break;
                    case PostCommand.ERASE_CONTENT:
                        break;
//                    case PostCommand.HARD_RESET:
//                        break;
//                    case PostCommand.SC_UART:
//                        break;
//                        case PostCommand.






                }

//                PostResponse postResponse = new GsonBuilder().create().fromJson(content.toString()
//                        .replace("<pre>", "")
//                        .replace("</pre>", ""), PostResponse.class);



//                int code = Integer.parseInt(jsonObject.getString("code"));
//                result.putInt("code", code);
//                String r = content.toString();
//                result.putString("result", r);
//                Logger.d(Logger.UTILS_LOG, "result: " + r);
            }
            else {
                result.putString(RESPONSE, PostCommand.TAKE_INFO_FULL_ERROR);
            }









            listener.onTaskCompleted(result);
            reader.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            result.putString("result", e.getMessage());
        } catch (ProtocolException e) {
            e.printStackTrace();
            result.putString("result", e.getMessage());
        } catch (IOException e) {
            if(!(e instanceof ConnectException)){
                e.printStackTrace();
            }
            result.putString("result", e.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        catch (JSONException e) {
//            e.printStackTrace();
//        }
    }


    private String getUrl(String ip, String command)  throws IOException{
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("timeN", "14.04.21_16.32"));
        params.add(new BasicNameValuePair("user", "dirvion"));
        params.add(new BasicNameValuePair("secret", "fasterAnDfaster"));
        params.add(new BasicNameValuePair("command", command));
        return "http://" + ip + "/interface/index.php?" + getQuery(params);
    }

    private HttpURLConnection getConnection(URL url) throws IOException {
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("POST");
        c.setConnectTimeout(12000);
        c.setReadTimeout(15000);
        c.connect();
        return c;
    }

    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
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
}
