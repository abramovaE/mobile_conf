package com.kotofeya.mobileconfigurator.network.request;

import androidx.annotation.NonNull;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.network.NetworkUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PostCommandUseCase {

        private static final String TAG = PostCommandUseCase.class.getSimpleName();
        private final PostCommandListener listener;
        private final String urlCommand;

        public PostCommandUseCase(PostCommandListener listener, String urlCommand) {
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
                        if(response.code() == HttpURLConnection.HTTP_OK) {
                            listener.postCommandSuccessful(content);
                        } else {
                            listener.postCommandFailed(content);
                        }
                        response.close();
                    }
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        listener.postCommandFailed(e.getLocalizedMessage());
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                listener.postCommandFailed(e.getLocalizedMessage());
            }
        }
}