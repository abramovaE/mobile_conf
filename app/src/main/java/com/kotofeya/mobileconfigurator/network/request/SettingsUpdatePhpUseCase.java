package com.kotofeya.mobileconfigurator.network.request;

import androidx.annotation.NonNull;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.network.NetworkUtils;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SettingsUpdatePhpUseCase implements Runnable{
    private static final String TAG = PostInfo.class.getSimpleName();

    private final String ip;
    private final SettingsUpdatePhpListener settingsUpdatePhpListener;

    public SettingsUpdatePhpUseCase(SettingsUpdatePhpListener settingsUpdatePhpListener, String ip) {
        Logger.d(TAG, "UpdatePhpUseCase()");
        this.settingsUpdatePhpListener = settingsUpdatePhpListener;
        this.ip = ip;
    }

    @Override
    public void run() {
        try {
            URL u = new URL(NetworkUtils.getUrl(ip, PostCommand.UPDATE_PHP));
            Logger.d(TAG, "url: " + u);

            OkHttpClient client = new OkHttpClient();
            Request get = new Request.Builder()
                    .url(u)
                    .build();

            client.newCall(get).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if(response.code() == HttpURLConnection.HTTP_OK){
                        String content = NetworkUtils.responseToString(response);
                        settingsUpdatePhpListener.updateSuccessful(ip, content);
                    } else {
                        settingsUpdatePhpListener.updateFailed(ip, "responseCode: " + response);
                    }
                    response.close();
                }
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    settingsUpdatePhpListener.updateFailed(ip, e.getMessage());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}