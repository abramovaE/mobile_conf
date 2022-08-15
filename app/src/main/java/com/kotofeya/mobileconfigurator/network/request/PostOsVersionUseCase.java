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

public class PostOsVersionUseCase {

        private static final String TAG = PostOsVersionUseCase.class.getSimpleName();
        private final PostOsVersionListener listener;
        private final String urlCommand;

        public PostOsVersionUseCase(PostOsVersionListener listener, String urlCommand) {
            Logger.d(TAG, "urlCommand: " + urlCommand);
            this.urlCommand = urlCommand;
            this.listener = listener;
        }

        public void newRequest() {
            Logger.d(TAG, "newRequest(), command: " + urlCommand);
            try {
                URL u = new URL(urlCommand);

                OkHttpClient client = new OkHttpClient();
                Request get = new Request.Builder()
                        .url(u)
                        .build();

                client.newCall(get).enqueue(new Callback() {
                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) {
                        String content = NetworkUtils.responseToString(response);
                        Logger.d(TAG, "response: " + response.body());
                        Logger.d(TAG, "code: " + response.code());

                        if(response.code() == 200) {
                            listener.postOsVersionSuccessful(urlCommand, content);
                        } else {
                            listener.postOsVersionFailed(urlCommand, content);
                        }
                        response.close();
                    }
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        listener.postOsVersionFailed(urlCommand, e.getLocalizedMessage());
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}