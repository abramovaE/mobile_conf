package com.kotofeya.mobileconfigurator.network.request;

import androidx.annotation.NonNull;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.network.NetworkUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendLogToServerUseCase {

    public static final String TAG = SendLogToServerUseCase.class.getSimpleName();
    private static final String URL_POST_LOG = "http://95.161.210.44/mobile_conf_post_log.php";
    private final String logReport;
    private final SendLogToServerListener listener;

    public SendLogToServerUseCase(String logReport, SendLogToServerListener listener) {
        Logger.d(TAG, "new SendLogToServerUseCase()");
        this.logReport = logReport;
        this.listener = listener;
    }

    public void run() {
        URL u;
        try {
            u = new URL(URL_POST_LOG);
            OkHttpClient okHttpClient = new OkHttpClient();

            RequestBody formBody = new FormBody.Builder()
                    .add("log", logReport)
                    .build();

            Request request = new Request.Builder()
                    .url(u)
                    .post(formBody)
                    .build();
            okHttpClient.newCall(request).enqueue(new Callback() {

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {

                    String content = NetworkUtils.responseToString(response);
                    int code = response.code();
                    if(code == HttpURLConnection.HTTP_OK){
                        try {
                            JSONObject jsonObject = new JSONObject(content);
                            int respCode = Integer.parseInt(jsonObject.getString("code"));
                            if(respCode == 1){
                                listener.sendLogSuccessful();
                            } else {
                                listener.sendLogFailed(respCode + "");
                            }
                            listener.sendLogFailed(respCode + "");
                        } catch (JSONException e) {
                            listener.sendLogFailed(e.getMessage());
                        }
                    } else {
                        listener.sendLogFailed(code + "");
                    }
                    response.close();
                }
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    listener.sendLogFailed(e.getMessage());

                }
            });

        } catch (IOException e) {
            listener.sendLogFailed(e.getMessage());
        }
    }
}
