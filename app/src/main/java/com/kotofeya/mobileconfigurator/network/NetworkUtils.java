package com.kotofeya.mobileconfigurator.network;

import com.kotofeya.mobileconfigurator.Logger;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Response;
import okhttp3.ResponseBody;

public class NetworkUtils {

    public static final String TAG = NetworkUtils.class.getSimpleName();

    public static String getUrl(String ip, String command)  throws IOException {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("timeN", "14.07.21_16.32"));
        params.add(new BasicNameValuePair("user", "dirvion"));
        params.add(new BasicNameValuePair("secret", "fasterAnDfaster"));
        params.add(new BasicNameValuePair("command", command));
        return "http://" + ip + "/interface/index.php?" + getQuery(params);
    }

    public static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
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


    public static String responseToString(Response response){
        StringBuilder content = new StringBuilder();
        try {
            ResponseBody responseBody = response.body();
            BufferedReader br;
            InputStreamReader reader = new InputStreamReader(responseBody.byteStream());
            br = new BufferedReader(reader);
            String line;
            while (null != (line = br.readLine())) {
                content.append(line);
            }
            reader.close();
            br.close();
        } catch (IOException e){
            content.append("response io error");
        }
        return content.toString();
    }

    public static List<String> getTransportContent(Response response){
        List<String> tempUpdateTransportContentFiles = new ArrayList<>();
        StringBuilder content = new StringBuilder();
        try {
            ResponseBody responseBody = response.body();
            BufferedReader br;
            InputStreamReader reader = new InputStreamReader(responseBody.byteStream());
            br = new BufferedReader(reader);
            String line;
            while (null != (line = br.readLine())) {
//                Logger.d(TAG, "line: " + line);
                if (line.contains("href") && line.contains("\">")) {
                    String subS = line.substring(line.indexOf("./") + 2, line.indexOf("\">"));
                    tempUpdateTransportContentFiles.add(subS);
                }
            }
            reader.close();
            br.close();
        } catch (IOException e){
            content.append("response io error");
        }
        return tempUpdateTransportContentFiles;
    }





    public static Map<String, String> getStationContent(Response response){
        Map<String, String> tempUpdateStatContentFiles = new HashMap<>();
        StringBuilder content = new StringBuilder();
        try {
            ResponseBody responseBody = response.body();
            BufferedReader br;
            InputStreamReader reader = new InputStreamReader(responseBody.byteStream());
            br = new BufferedReader(reader);
            String line;
            while (null != (line = br.readLine())) {
                if (line.contains("href")) {
                    Logger.d(TAG, "line: " + line);
                    String serial_incr = line.substring(line.indexOf(".bz2>") + 5, line.indexOf("</a>"));
                    if(line.contains("_")){
                        String[] parts = serial_incr.split("_");
                        if(parts.length > 1){
                            tempUpdateStatContentFiles.put(parts[0], parts[1]);
                        }
                    }
                }
            }
            reader.close();
            br.close();
        } catch (IOException e){
            content.append("response io error");
        }
        return tempUpdateStatContentFiles;
    }

}
