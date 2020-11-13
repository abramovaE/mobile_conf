package com.kotofeya.mobileconfigurator;


import android.os.AsyncTask;
import android.os.Bundle;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Downloader extends AsyncTask<String, Integer, Bundle> implements TaskCode{
    public static final String CITY_URL = "http://95.161.210.44/update/city.json";
    public static final String OS_VERSION_URL = "http://95.161.210.44/update/rootimg";
    public static final String STM_VERSION_URL = "http://95.161.210.44/update/data/stm";
    public static final String TRANSPORT_CONTENT_VERSION_URL = "http://95.161.210.44/update/content/transp";
    public static final String STATION_CONTENT_VERSION_URL = "http://95.161.210.44/update/content/station";
    public static final String OS_URL = "http://95.161.210.44/update/rootimg/root.img.bz2";

    public static File tempUpdateOsFile;
    public static List<String> tempUpdateStmFiles;
    public static List<String> tempUpdateTransportContentFiles;
    public static Map<String, String> tempUpdateStationaryContentFiles;

    public static Map<String, String> tempUpdateContentFiles;

    private static String osVersion;
    private String stmVersion;
    private OnTaskCompleted listener;
    private String currentIp;
    private int currentAction;

    public Downloader(OnTaskCompleted listener) {
        this.listener = listener;
    }

    @Override
    protected Bundle doInBackground(String... url) {
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
            Logger.d(Logger.DOWNLOAD_LOG, "getContentException: " + ex.getMessage() + " " + ex.getCause());
            Bundle bundle = new Bundle();
            bundle.putString("result", ex.getMessage());
            bundle.putString("ip", currentIp);
            bundle.putInt("resultCode", DOWNLOADER_ERROR_CODE);
            return bundle;
        }
    }

    private void createUpdateOsFile() {
        File outputDir = App.get().getContext().getExternalFilesDir(null);
        Logger.d(Logger.DOWNLOAD_LOG, "tempUpdateOsFile: " + tempUpdateOsFile);
        if(tempUpdateOsFile != null && tempUpdateOsFile.exists()){
            Logger.d(Logger.DOWNLOAD_LOG, "delete exist file");
            tempUpdateOsFile.delete();
        }
        Logger.d(Logger.DOWNLOAD_LOG, " creating new temp os file");
        tempUpdateOsFile = new File(outputDir + "/root.img.bz2");
    }


    private File createTempUpdateFile(String fileName){
        File outputDir = App.get().getCacheDir();
        File file = new File(outputDir + "/" + fileName);
        if(file.exists()){
            file.delete();
        }
        file = new File(outputDir + "/" + fileName);
        file.deleteOnExit();
        return file;
    }

    private HttpURLConnection getConnection(URL url) throws IOException {
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("GET");
        c.setConnectTimeout(12000);
        c.setReadTimeout(15000);
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
            File file = createTempUpdateFile(tempFileName);
            writeToFile(input, file);
            return file.getAbsolutePath();
        }
        finally {
            if(c != null) {
                c.disconnect();
            }
        }
    }


    private URL getURL(int currentAction, String stringUrl) throws MalformedURLException {
        switch (currentAction){
            case UPDATE_TRANSPORT_CONTENT_DOWNLOAD_CODE:
                return new URL(TRANSPORT_CONTENT_VERSION_URL + "/" + stringUrl);
            case UPDATE_STATION_CONTENT_DOWNLOAD_CODE:
                return new URL(STATION_CONTENT_VERSION_URL + "/" + stringUrl);
            case  UPDATE_STM_DOWNLOAD_CODE:
                return new URL(STM_VERSION_URL + "/" + stringUrl);
        }
        return new URL(stringUrl);
    }

    private String getTempFileName(int currentAction, String stringUrl) throws MalformedURLException {
        switch (currentAction){
            case UPDATE_TRANSPORT_CONTENT_DOWNLOAD_CODE:
            case UPDATE_STATION_CONTENT_DOWNLOAD_CODE:
                return stringUrl.substring(4);
            case  UPDATE_STM_DOWNLOAD_CODE:
                return stringUrl;
        }
        return stringUrl;
    }

    @Override
    protected void onPostExecute(Bundle result) {
        Logger.d(Logger.DOWNLOAD_LOG, "onPostExecute");
        listener.onTaskCompleted(result);
    }

    private Bundle getContent(String stringUrl) throws Exception {
        Bundle bundle = new Bundle();
        bundle.putString("ip", currentIp);
        Logger.d(Logger.DOWNLOAD_LOG, "getContent: " + stringUrl + ", action: " + currentAction);
        URL url;
        try {
            if(currentAction == UPDATE_STM_DOWNLOAD_CODE ||
                    currentAction == UPDATE_TRANSPORT_CONTENT_DOWNLOAD_CODE ||
                    currentAction == UPDATE_STATION_CONTENT_DOWNLOAD_CODE){
                url = getURL(currentAction, stringUrl);
                String tempFilePath = downloadToFile(url, getTempFileName(currentAction, stringUrl));
                bundle.putInt("resultCode", currentAction);
                bundle.putString("filePath", tempFilePath);
                return bundle;
            }

            else {
                url = new URL(stringUrl);
                HttpURLConnection c = getConnection(url);
                InputStream input = c.getInputStream();
                String s;
            switch (stringUrl) {
                case OS_VERSION_URL:
                    try(BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
                        while ((s = reader.readLine()) != null) {
                            if (s.contains("ver.")) {
                                osVersion = s;
                            }
                        }
                    }
                    bundle.putString("result", "Release OS: " + osVersion);
                    bundle.putInt("resultCode", UPDATE_OS_VERSION_CODE);
                    return bundle;

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
                    bundle.putInt("resultCode", UPDATE_STM_VERSION_CODE);
                    bundle.putString("result", "Release: " + stmVersion);
                    return  bundle;

                case OS_URL:
                    createUpdateOsFile();
                    writeToFile(input, tempUpdateOsFile);
                    bundle.putInt("resultCode", UPDATE_OS_DOWNLOAD_CODE);
                    App.get().setUpdateOsFileVersion(osVersion);
                    App.get().setUpdateOsFilePath(tempUpdateOsFile.getAbsolutePath());
                    return bundle;

                case TRANSPORT_CONTENT_VERSION_URL:
                    tempUpdateTransportContentFiles = new ArrayList<>();
                    try(BufferedReader r1 = new BufferedReader(new InputStreamReader(input))) {
                        while ((s = r1.readLine()) != null) {
                            if (s.contains("href")) {
                                Logger.d(Logger.DOWNLOAD_LOG, "s transp: " + s);
                                Logger.d(Logger.DOWNLOAD_LOG, "sub s transp: " + s.substring(s.indexOf("./") + 2, s.indexOf("\">")));
                                tempUpdateTransportContentFiles.add(s.substring(s.indexOf("./") + 2, s.indexOf("\">")));
                            }
                        }
                    }
                    bundle.putInt("resultCode", TRANSPORT_CONTENT_VERSION_CODE);
                    bundle.putString("result", "transport content");
                    return bundle;

                case STATION_CONTENT_VERSION_URL:
                    tempUpdateStationaryContentFiles = new HashMap<>();
                    try(BufferedReader r2 = new BufferedReader(new InputStreamReader(input))) {
                        while ((s = r2.readLine()) != null) {
                            if (s.contains("href")) {
                                String serial_incr = s.substring(s.indexOf(".bz2>") + 5, s.indexOf("</a>"));
                                tempUpdateStationaryContentFiles.put(serial_incr.split("_")[0], serial_incr.split("_")[1]);
                            }
                        }
                    }
                    bundle.putInt("resultCode", STATION_CONTENT_VERSION_CODE);
                    bundle.putString("result", "stationary content");
                    return bundle;

                case CITY_URL:
                    url = new URL(CITY_URL);
                    Logger.d(Logger.DOWNLOAD_LOG, "url: " + url);
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
                    bundle.putInt("resultCode", DOWNLOAD_CITIES_CODE);
                    bundle.putString("result", res);
                    return bundle;
            }
        }

        } catch (IOException e) {
            Logger.d(Logger.DOWNLOAD_LOG, "exception: " + e);
            bundle.putInt("resultCode", TaskCode.DOWNLOADER_ERROR_CODE);
        }
        return bundle;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        listener.onProgressUpdate(values[0]);
        super.onProgressUpdate(values);
    }
}