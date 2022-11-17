package com.kotofeya.mobileconfigurator.network.download;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadCityUseCase {
    private static final String TAG = DownloadCityUseCase.class.getSimpleName();
    private static final String CITY_URL = "http://95.161.210.44/update/city.json";
    private final DownloadCityListener downloadCityListener;

    public DownloadCityUseCase(DownloadCityListener listener) {
        this.downloadCityListener = listener;
    }

    public void newRequest() {
        URL url = null;
        try {
            url = new URL(CITY_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        OkHttpClient client = new OkHttpClient();

        Request get = new Request.Builder()
                .url(url)
                .build();
        client.newCall(get).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                int respCode = response.code();
                if(respCode == HttpURLConnection.HTTP_OK){

                    InputStream inputStream = response.body().byteStream();

                    byte[] cityData = new byte[4096];
                    int cityCount;
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    while ((cityCount = inputStream.read(cityData)) != -1) {
                        byteArrayOutputStream.write(cityData, 0, cityCount);
                    }
                    inputStream.close();
                    String res = (byteArrayOutputStream.toString("cp1251"));
                    byteArrayOutputStream.close();

                    downloadCityListener.downloadSuccessful(res);
                } else {
                    downloadCityListener.downloadFailed(respCode + "");
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                downloadCityListener.downloadFailed(e.getLocalizedMessage());
            }
        });

    }
}