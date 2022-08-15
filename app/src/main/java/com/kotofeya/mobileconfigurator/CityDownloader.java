package com.kotofeya.mobileconfigurator;

import android.os.AsyncTask;
import android.os.Bundle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class CityDownloader extends AsyncTask<String, Integer, Bundle> implements TaskCode{
    private static final String TAG = CityDownloader.class.getSimpleName();

    public static final String CITY_URL = "http://95.161.210.44/update/city.json";

    private final OnTaskCompleted listener;

    public CityDownloader(OnTaskCompleted listener) {
        this.listener = listener;
    }

    @Override
    protected Bundle doInBackground(String... url) {
        Logger.d(TAG, "doInBackground(), ulr.length: " + url.length + " " + Arrays.toString(url));
        try {
            return getContent();
        }
        catch (Exception ex) {
            Logger.d(TAG, "getContentException: " + ex.getMessage() + " " + ex.getCause());
            Bundle bundle = new Bundle();
            bundle.putString(BundleKeys.RESULT, ex.getMessage());
            bundle.putInt(BundleKeys.RESULT_CODE_KEY, DOWNLOADER_ERROR_CODE);
            return bundle;
        }
    }

    private HttpURLConnection getConnection(URL url) throws IOException {
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("GET");
        c.setConnectTimeout(12000);
//        c.setReadTimeout(15000);
        c.connect();
        return c;
    }



    @Override
    protected void onPostExecute(Bundle result) {
//        Logger.d(Logger.DOWNLOAD_LOG, "onPostExecute, result: " + result);
        listener.onTaskCompleted(result);
    }

    private Bundle getContent() {

        Bundle bundle = new Bundle();

        try {
            HttpURLConnection c;
            InputStream input;

            URL url = new URL(CITY_URL);
            Logger.d(TAG, "url: " + url);
            c = getConnection(url);
            input = c.getInputStream();
            byte[] cityData = new byte[ 4096 ];
            int cityCount;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while ((cityCount = input.read(cityData)) != -1) {
                byteArrayOutputStream.write(cityData, 0, cityCount);
            }
            input.close();
            String res = (byteArrayOutputStream.toString("cp1251"));
            byteArrayOutputStream.close();
            bundle.putInt(BundleKeys.RESULT_CODE_KEY, DOWNLOAD_CITIES_CODE);
            bundle.putString(BundleKeys.RESULT_KEY, res);
            return bundle;
        } catch (IOException e) {
            Logger.d(TAG, "exception: " + e);
            bundle.putInt(BundleKeys.RESULT_CODE_KEY, TaskCode.DOWNLOADER_ERROR_CODE);
        }
        return bundle;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
//        Logger.d(Logger.DOWNLOAD_LOG, "url: " + url + " on progress update: " + values[0]);
        listener.onProgressUpdate(values[0]);
        super.onProgressUpdate(values);
    }
}