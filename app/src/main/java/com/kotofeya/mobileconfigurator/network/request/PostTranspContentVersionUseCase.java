package com.kotofeya.mobileconfigurator.network.request;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.network.NetworkUtils;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PostTranspContentVersionUseCase {

        private static final String TAG = PostTranspContentVersionUseCase.class.getSimpleName();
        private PostTranspContentVersionListener listener;
        private String urlCommand;

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
                    public void onResponse(Call call, Response response) {
                        int code = response.code();
                        if(code == 200) {
                            List<String> files = NetworkUtils.getTransportContent(response);
                            listener.postTranspContentVersionSuccessful(urlCommand, files);
                        } else {
                            listener.postTranspContentVersionFailed(urlCommand, code + "");
                        }
                        response.close();
                    }
                    @Override
                    public void onFailure(Call call, IOException e) {
                        listener.postTranspContentVersionFailed(urlCommand, e.getLocalizedMessage());
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}