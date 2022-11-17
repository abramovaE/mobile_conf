package com.kotofeya.mobileconfigurator.network.request;

import androidx.annotation.NonNull;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.network.NetworkUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.http.HTTP;

public class PostTranspContentVersionUseCase {

        private static final String TAG = PostTranspContentVersionUseCase.class.getSimpleName();
        private final PostTranspContentVersionListener listener;
        private final String urlCommand;

        public PostTranspContentVersionUseCase(PostTranspContentVersionListener listener, String urlCommand) {
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
                            List<String> files = NetworkUtils.getTransportContent(response);
                            listener.postTranspContentVersionSuccessful(urlCommand, files);
                        } else {
                            listener.postTranspContentVersionFailed(urlCommand, code + "");
                        }
                        response.close();
                    }
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        listener.postTranspContentVersionFailed(urlCommand, e.getLocalizedMessage());
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                listener.postTranspContentVersionFailed(urlCommand, e.getLocalizedMessage());
            }
        }
}