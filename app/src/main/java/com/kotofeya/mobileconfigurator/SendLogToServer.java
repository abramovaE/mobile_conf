package com.kotofeya.mobileconfigurator;

import android.os.Bundle;

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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

public class SendLogToServer implements Runnable {

    private static String url_post_log = "http://95.161.210.44/mobile_conf_post_log.php";
    private String logReport;
    private OnTaskCompleted listener;

    public SendLogToServer(String logReport, OnTaskCompleted listener) {
        this.logReport = logReport;
        this.listener = listener;
    }

    @Override
    public void run() {
        Logger.d(Logger.MAIN_LOG, "check user");
        URL u;
        try {
            u = new URL(url_post_log);
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

            Logger.d(Logger.MAIN_LOG, "send log file response: " + response);
            Bundle result = new Bundle();
            result.putInt("resultCode", TaskCode.SEND_LOG_TO_SERVER_CODE);


            if(response == 200){
                JSONObject jsonObject = new JSONObject(content.toString());
                int code = Integer.parseInt(jsonObject.getString("code"));
                result.putInt("code", code);
            }
            else {
                result.putInt("code", 0);
            }

            listener.onTaskCompleted(result);
            reader.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private HttpURLConnection getConnection(URL url) throws IOException {
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("POST");
        c.setConnectTimeout(12000);
        c.setReadTimeout(15000);
        OutputStream os = c.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

        writer.write(logReport);
        writer.flush();
        writer.close();
        os.close();
        c.connect();
        return c;
    }
}
