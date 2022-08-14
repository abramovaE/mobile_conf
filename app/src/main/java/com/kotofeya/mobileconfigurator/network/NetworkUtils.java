package com.kotofeya.mobileconfigurator.network;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;
import okhttp3.ResponseBody;

public class NetworkUtils {

    public static String getUrl(String ip, String command)  throws IOException {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("timeN", "14.07.21_16.32"));
        params.add(new BasicNameValuePair("user", "dirvion"));
        params.add(new BasicNameValuePair("secret", "fasterAnDfaster"));
        params.add(new BasicNameValuePair("command", command));
        return "http://" + ip + "/interface/index.php?" + getQuery(params);
    }

    private static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
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
}
