package com.kotofeya.mobileconfigurator.network.request;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.network.NetworkUtils;

import java.io.IOException;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PostTakeInfoUseCase {
        private static final String TAG = PostTakeInfoUseCase.class.getSimpleName();

        private  String ip;
        private PostTakeInfoListener listener;
        private String urlCommand;
        private String version;

        public PostTakeInfoUseCase(PostTakeInfoListener listener, String ip, String urlCommand, String version) {
            Logger.d(TAG, "PostInfo: " + urlCommand + ", ip: " + ip + ", version: " + version);
            this.listener = listener;
            this.urlCommand = urlCommand;
            this.ip = ip;
            this.version = version;
        }

        public void newRequest() {
            Logger.d(TAG, "newRequest(), command: " + urlCommand);
            try {
                URL u = new URL(NetworkUtils.getUrl(ip, urlCommand));
                Logger.d(TAG, "url: " + u);

                OkHttpClient client = new OkHttpClient();
                Request get = new Request.Builder()
                        .url(u)
                        .build();

                client.newCall(get).enqueue(new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) {
                        if(response.code() == 200){
                            String content = NetworkUtils.responseToString(response);
                            Logger.d(TAG, "content: " + content);
                            listener.postTakeInfoSuccessful(urlCommand, ip, content, version);
                        }
                        else {
                            listener.postTakeInfoFailed(urlCommand, ip, response.code() + "");
                        }
                        response.close();
                    }
                    @Override
                    public void onFailure(Call call, IOException e) {
                        listener.postTakeInfoFailed(urlCommand, ip, e.getLocalizedMessage());
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
}