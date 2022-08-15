package com.kotofeya.mobileconfigurator;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.kotofeya.mobileconfigurator.network.DownloadFileUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Downloader extends AsyncTask<String, Integer, Bundle> implements TaskCode{
    private static final String TAG = Downloader.class.getSimpleName();

    public static final String CITY_URL = "http://95.161.210.44/update/city.json";
    public static final String STM_VERSION_URL = "http://95.161.210.44/update/data/stm";
    public static List<String> tempUpdateStmFiles;

    private String stmVersion;
    private OnTaskCompleted listener;
    private String currentIp;
    private int currentAction;
    private URL url;

    public Downloader(OnTaskCompleted listener) {
        this.listener = listener;
    }


    @Override
    protected Bundle doInBackground(String... url) {


        Logger.d(TAG, "doInBackground(), ulr.length: " + url.length + " " + Arrays.toString(url));

        if(url.length > 1) {
            currentIp = url[1];
        }
        if (url.length > 2){
            currentAction = Integer.parseInt(url[2]);
        }
        try {
            return getContent(url[0]);
        }
        catch (Exception ex) {
            Logger.d(TAG, "getContentException: " + ex.getMessage() + " " + ex.getCause());
            Bundle bundle = new Bundle();
            bundle.putString(BundleKeys.RESULT, ex.getMessage());
            bundle.putString(BundleKeys.IP_KEY, currentIp);
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

    private void writeToFile(InputStream input, File file) throws IOException {
        try(OutputStream output = new FileOutputStream(file)) {
            byte data[] = new byte[4096];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
                publishProgress((int) (100 * (file.length() / 40755927.0)));
            }
        }
    }

    private String downloadToFile(URL url, String tempFileName) throws IOException {
        HttpURLConnection c = getConnection(url);
        try (InputStream input = c.getInputStream()){
            File file = DownloadFileUtils.createTempUpdateFile(tempFileName);
            writeToFile(input, file);
            return file.getAbsolutePath();
        } finally {
            if(c != null) {
                c.disconnect();
            }
        }
    }


    private URL getURL(int currentAction, String stringUrl) throws MalformedURLException {
        Logger.d(TAG, "getUrl: " + currentAction + " " + stringUrl);

        switch (currentAction){
            case  UPDATE_STM_DOWNLOAD_CODE:
                return new URL(STM_VERSION_URL + "/" + stringUrl);
        }
        Logger.d(TAG, "getUrl result: " + stringUrl);
        return new URL(stringUrl);
    }

    private String getTempFileName(int currentAction, String stringUrl) throws MalformedURLException {
        Logger.d(TAG, "getTempFileName: " + stringUrl + ", currentAction: " + currentAction);
        switch (currentAction){
            case  UPDATE_STM_DOWNLOAD_CODE:
                return stringUrl;
        }
        Logger.d(TAG, "tempFileName result: " + stringUrl);
        return stringUrl;
    }

    @Override
    protected void onPostExecute(Bundle result) {
//        Logger.d(Logger.DOWNLOAD_LOG, "onPostExecute, result: " + result);
        listener.onTaskCompleted(result);
    }

    private Bundle getContent(String stringUrl) throws Exception {

        Bundle bundle = new Bundle();
        bundle.putString(BundleKeys.IP_KEY, currentIp);
        Logger.d(TAG, "getContent: " + stringUrl + ", action: " + currentAction);
        try {
            if(currentAction == UPDATE_STM_DOWNLOAD_CODE){

                url = getURL(currentAction, stringUrl);
                Logger.d(TAG, "url: " + url);
                String tempFileName = getTempFileName(currentAction, stringUrl);
                String tempFilePath = downloadToFile(url, getTempFileName(currentAction, stringUrl));
//                Logger.d(TAG, "tempFilePath: " + tempFilePath);
                bundle.putInt(BundleKeys.RESULT_CODE_KEY, currentAction);
                bundle.putString("filePath", tempFilePath);
                return bundle;
            } else {
                HttpURLConnection c;
                InputStream input;
                String s;

                    url = new URL(stringUrl);

                    c = getConnection(url);
                    input = c.getInputStream();

                    switch (stringUrl) {
                        case STM_VERSION_URL:
                            tempUpdateStmFiles = new ArrayList<>();
                            try(BufferedReader r = new BufferedReader(new InputStreamReader(input))) {
                                while ((s = r.readLine()) != null) {
                                    if (s.contains("ver.")) {
                                        stmVersion = s.substring(0, s.indexOf("<"));
                                    } else if (s.contains("M")) {
                                        String sub = s.substring(s.lastIndexOf("M"));
                                        tempUpdateStmFiles.add(sub.substring(0, sub.indexOf("<")));
                                    } else if (s.contains("S")) {
                                        String sub = s.substring(s.lastIndexOf("S"));
                                        tempUpdateStmFiles.add(sub.substring(0, sub.indexOf("<")));
                                    }
                                }
                            }
                            bundle.putInt(BundleKeys.RESULT_CODE_KEY, UPDATE_STM_VERSION_CODE);
                            bundle.putString(BundleKeys.RESULT_KEY, "Release: " + stmVersion);
                            return  bundle;
                        case CITY_URL:
                            url = new URL(CITY_URL);
                            Logger.d(TAG, "url: " + url);
                            c = getConnection(url);
                            input = c.getInputStream();
                            byte cityData[] = new byte[4096];
                            int cityCount;
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            while ((cityCount = input.read(cityData)) != -1) {
                                byteArrayOutputStream.write(cityData, 0, cityCount);
                            }
                            input.close();
                            String res = (new String(byteArrayOutputStream.toByteArray(), "cp1251"));
                            byteArrayOutputStream.close();
                            bundle.putInt(BundleKeys.RESULT_CODE_KEY, DOWNLOAD_CITIES_CODE);
                            bundle.putString(BundleKeys.RESULT_KEY, res);
                            return bundle;
                    }

        }
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