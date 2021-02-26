package com.kotofeya.mobileconfigurator;


import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class CheckUser extends AsyncTask<Void, Void, Void> {
    private static String url_checkUser = "http://95.161.210.44/is_mobile_conf_user_valid.php";
    private String login;
    private String password;
    private MyCustomCallBack callback;
    private Context context;
    private String message = null;
    private boolean isUserValid;

    public CheckUser(Context context, String login, String password, MyCustomCallBack callback) {
        this.login = login;
        this.password = password;
        this.callback = callback;
        this.context = context;
    }

    public interface MyCustomCallBack
    {
        void doIfUserValid();
    }

    protected Void doInBackground(Void... voids) {

        Logger.d(Logger.MAIN_LOG, "check user");
        URL u;
        try {
            u = new URL(url_checkUser);
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

            Logger.d(Logger.MAIN_LOG, "check user response: " + response);
            if(response == 200){
                JSONObject jsonObject = new JSONObject(content.toString());
                int code = Integer.parseInt(jsonObject.getString("code"));
                if(code == 1){
                    isUserValid = true;
                    message = "Успешно";
                }
                else if (code == 0){
                    isUserValid = false;
                    message = "Неправильный логин или пароль";
                }
            }
            else {
                message = "Сервер не отвечает";
            }

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
        return null;
    }

    @Override
    protected void onPostExecute(Void s) {
        Logger.d(Logger.MAIN_LOG, "is user valid: on post execute");
        isUserValid = true;
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        if(callback != null && isUserValid){
            callback.doIfUserValid();
        }
    }


    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (NameValuePair pair : params)
        {
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

    private HttpURLConnection getConnection(URL url) throws IOException {
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("POST");
        c.setConnectTimeout(12000);
        c.setReadTimeout(15000);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("login", login));
        params.add(new BasicNameValuePair("password", password));
        OutputStream os = c.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(getQuery(params));
        writer.flush();
        writer.close();
        os.close();
        c.connect();
        return c;
    }
}