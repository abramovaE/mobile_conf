package com.kotofeya.mobileconfigurator.network.request;

import androidx.annotation.NonNull;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.network.NetworkUtils;

import java.io.IOException;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PostVersionUseCase {

        private static final String TAG = PostVersionUseCase.class.getSimpleName();
        private final String ip;
        private final PostVersionListener listener;
        private final String urlCommand;

        public PostVersionUseCase(PostVersionListener listener, String ip, String urlCommand) {
            Logger.d(TAG, "urlCommand: " + urlCommand + ", ip: " + ip);
            this.urlCommand = urlCommand;
            this.listener = listener;
            this.ip = ip;
        }

        public void newRequest() {
            Logger.d(TAG, "newRequest(), command: " + urlCommand);
            try {
                URL u = new URL(NetworkUtils.getUrl(ip, urlCommand));

                OkHttpClient client = new OkHttpClient();
                Request get = new Request.Builder()
                        .url(u)
                        .build();

                client.newCall(get).enqueue(new Callback() {
                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) {
                        String content = NetworkUtils.responseToString(response);
                        Logger.d(TAG, "response: " + content);
                        Logger.d(TAG, "code: " + response.code());

                        if(content.contains("404")){
                            listener.postVersionFailed(urlCommand, ip, content);
                        } else {
                            if(response.code() == 200) {
                                listener.postVersionSuccessful(urlCommand, ip, content);
                            } else {
                                listener.postVersionFailed(urlCommand, ip, content);
                            }
                        }
                        response.close();
                    }
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        listener.postVersionFailed(urlCommand, ip, e.getLocalizedMessage());
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}