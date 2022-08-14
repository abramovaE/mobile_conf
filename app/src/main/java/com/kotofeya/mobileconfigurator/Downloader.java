package com.kotofeya.mobileconfigurator;

import android.os.AsyncTask;
import android.os.Bundle;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Downloader extends AsyncTask<String, Integer, Bundle> implements TaskCode{
    private static final String TAG = Downloader.class.getSimpleName();

    public static final String CITY_URL = "http://95.161.210.44/update/city.json";
    public static final String OS_VERSION_URL = "http://95.161.210.44/update/rootimg";
    public static final String STM_VERSION_URL = "http://95.161.210.44/update/data/stm";
    public static final String TRANSPORT_CONTENT_VERSION_URL = "http://95.161.210.44/update/content/transp";
    public static final String STATION_CONTENT_VERSION_URL = "http://95.161.210.44/update/content/station";
    public static final String OS_URL = "http://95.161.210.44/update/rootimg/root.img.bz2";

//    public static final String CORE_URLS = "core_urls";
//    private static final String CORE_URLS_DIR = "http://95.161.210.44/update/1.4-1.5/";

//    // TODO: 25.08.2021 rename last file
//    private static final String[] CORE_URLS_FILE_NAMES = {
//            "root_prepare_1.4-1.5.img.bz2",
//            "boot-old.img.bz2",
//            "boot-new.img.bz2",
//            "root-1.5.6-release.img.bz2"
//    };

//    private static final String[] COREURLS = {
//            CORE_URLS_DIR + CORE_URLS_FILE_NAMES[0],
//            CORE_URLS_DIR + CORE_URLS_FILE_NAMES[1],
//            CORE_URLS_DIR + CORE_URLS_FILE_NAMES[2],
//            CORE_URLS_DIR + CORE_URLS_FILE_NAMES[3],
//    };

//    private static List<Boolean> IS_CORE_FILES_EXIST = new ArrayList<>();
//    public static File tempUpdateOsFile;
    public static List<String> tempUpdateStmFiles;
    public static List<String> tempUpdateTransportContentFiles;
    public static Map<String, String> tempUpdateStationaryContentFiles;

    private static String osVersion;
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
        Logger.d(TAG, "doInBackground(), ulr.length: " + url.length);
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

//    private void createUpdateOsFile() {
//
//        File outputDir = App.get().getApplicationContext().getExternalFilesDir(null);
//        Logger.d(TAG, "tempUpdateOsFile: " + tempUpdateOsFile);
//        if(tempUpdateOsFile != null && tempUpdateOsFile.exists()){
//            Logger.d(TAG, "delete exist file");
//            tempUpdateOsFile.delete();
//        }
//        Logger.d(TAG, " creating new temp os file");
//        tempUpdateOsFile = new File(outputDir + "/root.img.bz2");
//    }


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
        switch (currentAction){
            case UPDATE_TRANSPORT_CONTENT_DOWNLOAD_CODE:
            case UPDATE_TRANSPORT_CONTENT_UPLOAD_TO_STORAGE_CODE:
                return new URL(TRANSPORT_CONTENT_VERSION_URL + "/" + stringUrl);
            case UPDATE_STATION_CONTENT_DOWNLOAD_CODE:
                return new URL(STATION_CONTENT_VERSION_URL + "/" + stringUrl);
            case  UPDATE_STM_DOWNLOAD_CODE:
                return new URL(STM_VERSION_URL + "/" + stringUrl);
        }
        return new URL(stringUrl);
    }

    private String getTempFileName(int currentAction, String stringUrl) throws MalformedURLException {
        Logger.d(TAG, "getTempFileName: " + stringUrl + ", currentAction: " + currentAction);
        switch (currentAction){
            case UPDATE_TRANSPORT_CONTENT_DOWNLOAD_CODE:
            case UPDATE_STATION_CONTENT_DOWNLOAD_CODE:
                return stringUrl.substring(4);
            case  UPDATE_STM_DOWNLOAD_CODE:
                return stringUrl;
            case UPDATE_TRANSPORT_CONTENT_UPLOAD_TO_STORAGE_CODE:
                return stringUrl.replace("/", "_");
        }
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
            if(currentAction == UPDATE_STM_DOWNLOAD_CODE ||
                    currentAction == UPDATE_TRANSPORT_CONTENT_DOWNLOAD_CODE ||
                    currentAction == UPDATE_STATION_CONTENT_DOWNLOAD_CODE ||
                    currentAction == UPDATE_TRANSPORT_CONTENT_UPLOAD_TO_STORAGE_CODE){
                url = getURL(currentAction, stringUrl);
                String tempFilePath = downloadToFile(url, getTempFileName(currentAction, stringUrl));
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
                        case OS_VERSION_URL:
                            Logger.d(TAG, "osVersion url: " + stringUrl);

                            try(BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
                                while ((s = reader.readLine()) != null) {
                                    if (s.contains("ver.")) {
                                        Logger.d(TAG, "osVersion: " + osVersion + ", ip: " + currentIp);
                                        osVersion = s;
                                    }
                                }
                            }
                            bundle.putString(BundleKeys.RESULT_KEY,
                                    App.get().getString(R.string.release_os) +  ": " + osVersion);

                            Logger.d(TAG, "act ver: " + App.get().getString(R.string.release_os) +  ": " + osVersion);
                            bundle.putInt(BundleKeys.RESULT_CODE_KEY, UPDATE_OS_VERSION_CODE);
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
                            bundle.putInt(BundleKeys.RESULT_CODE_KEY, UPDATE_STM_VERSION_CODE);
                            bundle.putString(BundleKeys.RESULT_KEY, "Release: " + stmVersion);
                            return  bundle;
//                        case OS_URL:
//                            createUpdateOsFile();
//                            writeToFile(input, tempUpdateOsFile);
//                            bundle.putInt(BundleKeys.RESULT_CODE_KEY, UPDATE_OS_DOWNLOAD_CODE);
//                            App.get().setUpdateOsFileVersion(osVersion);
//                            App.get().setUpdateOsFilePath(tempUpdateOsFile.getAbsolutePath());
//                            return bundle;
                        case TRANSPORT_CONTENT_VERSION_URL:
                            tempUpdateTransportContentFiles = new ArrayList<>();
                            try(BufferedReader r1 = new BufferedReader(new InputStreamReader(input))) {
                                while ((s = r1.readLine()) != null) {
                                    if (s.contains("href")) {
                                        String subS = s.substring(s.indexOf("./") + 2, s.indexOf("\">"));
                                        Logger.d(TAG, "s transp: " + s);
                                        Logger.d(TAG, "subS transp: " + subS);
                                        tempUpdateTransportContentFiles.add(subS);
                                    }
                                }
                            }
                            bundle.putInt(BundleKeys.RESULT_CODE_KEY, TRANSPORT_CONTENT_VERSION_CODE);
                            bundle.putString(BundleKeys.RESULT_KEY, "transport content");
                            return bundle;
                        case STATION_CONTENT_VERSION_URL:
                            tempUpdateStationaryContentFiles = new HashMap<>();
                            try(BufferedReader r2 = new BufferedReader(new InputStreamReader(input))) {
                                while ((s = r2.readLine()) != null) {
                                    Logger.d(TAG, "stat_content_version s: " + s);
                                    if (s.contains("href")) {
                                        String serial_incr = s.substring(s.indexOf(".bz2>") + 5, s.indexOf("</a>"));
                                        tempUpdateStationaryContentFiles.put(serial_incr.split("_")[0], serial_incr.split("_")[1]);
                                    }
                                }
                            }
                            bundle.putInt(BundleKeys.RESULT_CODE_KEY, STATION_CONTENT_VERSION_CODE);
                            bundle.putString(BundleKeys.RESULT_KEY, "stationary content");
                            return bundle;
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

//    public static boolean isCoreUpdatesDownloadCompleted(){
//        Logger.d(TAG, "isCoreUpdatesDownloadCompleted: " +
//                (!IS_CORE_FILES_EXIST.isEmpty() &&  IS_CORE_FILES_EXIST.stream().allMatch(it->true)));
//        return !IS_CORE_FILES_EXIST.isEmpty() && IS_CORE_FILES_EXIST.stream().allMatch(it->true);
//    }
}