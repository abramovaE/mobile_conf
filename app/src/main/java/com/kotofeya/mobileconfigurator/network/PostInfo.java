package com.kotofeya.mobileconfigurator.network;

import androidx.annotation.NonNull;

import com.kotofeya.mobileconfigurator.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PostInfo implements Runnable {

    private static final String TAG = PostInfo.class.getSimpleName();

    private final String ip;
    private final String urlCommand;
    private final PostInfoListener postInfoListener;

    public PostInfo(String ip, String urlCommand, PostInfoListener postInfoListener) {
        Logger.d(TAG, "PostInfo(): " + urlCommand + ", ip: " + ip);
        this.urlCommand = urlCommand;
        this.ip = ip;
        this.postInfoListener = postInfoListener;
    }


    @Override
    public void run() {
        Logger.d(TAG, "run(), urlCommand: " + urlCommand);
        try {
            URL u = new URL(NetworkUtils.getUrl(ip, urlCommand));
            OkHttpClient okHttpClient = new OkHttpClient();
            Request get = new Request.Builder().url(u).build();
            okHttpClient.newCall(get).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response){
                    int responseCode = response.code();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        String content = NetworkUtils.responseToString(response);
                        postInfoListener.postInfoSuccessful(ip, content);
                    } else {
                        postInfoListener.postInfoFailed(response + "");
                    }
                    response.close();
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    postInfoListener.postInfoFailed(e.getMessage());
                }
            });
        } catch (IOException e) {
            postInfoListener.postInfoFailed(e.getMessage());
        }
    }
}
