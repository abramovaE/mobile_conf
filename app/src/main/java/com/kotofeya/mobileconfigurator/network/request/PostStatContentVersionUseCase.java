package com.kotofeya.mobileconfigurator.network.request;

import androidx.annotation.NonNull;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.network.NetworkUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PostStatContentVersionUseCase {

        private static final String TAG = PostStatContentVersionUseCase.class.getSimpleName();
        private final PostStatContentVersionListener listener;
        private final String urlCommand;

        public PostStatContentVersionUseCase(PostStatContentVersionListener listener, String urlCommand) {
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
                        int code = response.code();
                        if(code == HttpURLConnection.HTTP_OK) {
                            Map<String, String> files = NetworkUtils.getStationContent(response);
                            listener.postStatContentVersionSuccessful(urlCommand, files);
                        } else {
                            listener.postStatContentVersionFailed(urlCommand, code + "");
                        }
                        response.close();
                    }
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        listener.postStatContentVersionFailed(urlCommand, e.getLocalizedMessage());
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}