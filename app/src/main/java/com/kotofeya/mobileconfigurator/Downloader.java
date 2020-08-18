package com.kotofeya.mobileconfigurator;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;


import com.kotofeya.mobileconfigurator.activities.MainActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class Downloader extends AsyncTask<String, Integer, Bundle> {


    public static final String OS_VERSION_URL = "http://95.161.210.44/update/rootimg";
    public static final String STM_VERSION_URL = "http://95.161.210.44/update/data/stm";


    public static final String TRANSPORT_CONTENT_VERSION_URL = "http://95.161.210.44/update/content/transp";
    public static final String STATION_CONTENT_VERSION_URL = "http://95.161.210.44/update/content/station";


    public static final String OS_URL = "http://95.161.210.44/update/rootimg/root.img.bz2";

    public static File tempUpdateOsFile;
    public static List<String> tempUpdateStmFiles;


    public static List<String> tempUpdateTransportContentFiles;
    public static List<String> tempUpdateStationaryContentFiles;

//    // TODO: 18.08.2020 for test, delete in release
//    static {
//        tempUpdateTransportContentFiles = new ArrayList<>();
//        tempUpdateStationaryContentFiles = new ArrayList<>();
//        tempUpdateTransportContentFiles.add("spb.2.tar.bz2");
//        tempUpdateTransportContentFiles.add("ros.1.tar.bz2");
//        tempUpdateStationaryContentFiles.add("6424.1.tar.bz2");
//    }



    private static String osVersion;
    private String stmVersion;

    private OnTaskCompleted listener;
    private String currentIp;


    public Downloader(OnTaskCompleted listener) {
        this.listener = listener;

    }

    @Override
    protected Bundle doInBackground(String... url) {
        if(url.length > 1) {
            currentIp = url[1];
        }
        try {
            return getContent(url[0]);
        }
        catch (IOException ex) {
//            Toast.makeText((MainActivity)App.get().getContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            Logger.d(Logger.DOWNLOAD_LOG, "getContentException: " + ex.getMessage() + " " + ex.getCause());

            Bundle bundle = new Bundle();
            bundle.putString("result", "get content exception");
            bundle.putString("ip", currentIp);
            return bundle;
        }
    }


    @Override
    protected void onPreExecute() {
//        createTempUpdateOsFile();
        createUpdateOsFile();
    }


    private void createTempUpdateOsFile(){
        File outputDir = App.get().getCacheDir();
        try {
            Logger.d(Logger.DOWNLOAD_LOG, "tempUpdateOsFile: " + tempUpdateOsFile);

            if(tempUpdateOsFile != null && tempUpdateOsFile.exists()){
                Logger.d(Logger.DOWNLOAD_LOG, "delete exist file");
                tempUpdateOsFile.delete();
            }
            Logger.d(Logger.DOWNLOAD_LOG, " creating new temp file");

            tempUpdateOsFile = new File(outputDir + "/root.img.bz2");
            tempUpdateOsFile.deleteOnExit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void createUpdateOsFile(){
        File outputDir = App.get().getContext().getExternalFilesDir(null);
        try {
            Logger.d(Logger.DOWNLOAD_LOG, "tempUpdateOsFile: " + tempUpdateOsFile);

            if(tempUpdateOsFile != null && tempUpdateOsFile.exists()){
                Logger.d(Logger.DOWNLOAD_LOG, "delete exist file");
                tempUpdateOsFile.delete();
            }
            Logger.d(Logger.DOWNLOAD_LOG, " creating new temp file");

            tempUpdateOsFile = new File(outputDir + "/root.img.bz2");
//            tempUpdateOsFile.deleteOnExit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private File createTempUpdateStmFile(String fileName){
        File outputDir = App.get().getCacheDir();
        File file = new File(outputDir + "/" + fileName);
        try {
            Logger.d(Logger.DOWNLOAD_LOG, "tempUpdateStmFile: " + file);
            if(file.exists()){
                Logger.d(Logger.DOWNLOAD_LOG, "delete exist file");
                file.delete();
            }
            Logger.d(Logger.DOWNLOAD_LOG, " creating new temp file");
            file = new File(outputDir + "/" + fileName);
            file.deleteOnExit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    @Override
    protected void onPostExecute(Bundle result) {
        Logger.d(Logger.DOWNLOAD_LOG, "onPostExecute");
        listener.onTaskCompleted(result);
    }

    private Bundle getContent(String stringUrl) throws IOException {
        Bundle bundle = new Bundle();
        bundle.putString("ip", currentIp);

        Logger.d(Logger.DOWNLOAD_LOG, "getting version, string url: " + stringUrl);
        OutputStream output = null;

        URL url;
        HttpURLConnection c = null;
        InputStream input = null;



        try {
            if(tempUpdateStmFiles != null && tempUpdateStmFiles.contains(stringUrl)){
                url = new URL(STM_VERSION_URL + "/" + stringUrl);
                c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.setConnectTimeout(12000);
                c.setReadTimeout(15000);
                c.connect();
                input = c.getInputStream();
                    File file = createTempUpdateStmFile(stringUrl);
                    file.deleteOnExit();
                    output = new FileOutputStream(file);
                    byte data[] = new byte[4096];
                    int count;
                    while ((count = input.read(data)) != -1) {
                        output.write(data, 0, count);
                        publishProgress((int) (100 * (file.length() / 40755927.0)));
                    }
//                output.close();

                bundle.putString("result", "stm downloaded");
                bundle.putString("filePath", file.getAbsolutePath());
                bundle.putString("ip", currentIp);
                return bundle;
//                    return "stm downloaded";
            }

            else {
                url = new URL(stringUrl);
                c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.setConnectTimeout(12000);
                c.setReadTimeout(15000);
                c.connect();
                input = c.getInputStream();
            switch (stringUrl) {
                case OS_VERSION_URL:
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    String s;
                    while ((s = reader.readLine()) != null) {
                        if (s.contains("ver.")) {
                            osVersion = s;
                        }
                    }
                    reader.close();
                    bundle.putString("result", "Release OS: " + osVersion);
                    return bundle;
//                    return ;

                case STM_VERSION_URL:
                    tempUpdateStmFiles = new ArrayList<>();
                    BufferedReader r = new BufferedReader(new InputStreamReader(input));
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
                    r.close();
                    bundle.putString("result", "Release: " + stmVersion);
                    bundle.putString("ip", currentIp);
                    return  bundle;
//                    return "Release: " + stmVersion;


                case OS_URL:
                    output = new FileOutputStream(tempUpdateOsFile);
                    byte data[] = new byte[4096];
                    int count;
                    while ((count = input.read(data)) != -1) {
                        output.write(data, 0, count);
                        publishProgress((int) (100 * (tempUpdateOsFile.length() / 40755927.0)));
                    }
//                    output.close();
                    bundle.putString("result", "Downloaded");
                    bundle.putString("ip", currentIp);
                    App.get().setUpdateOsFileVersion(osVersion);
                    App.get().setUpdateOsFilePath(tempUpdateOsFile.getAbsolutePath());

                    return bundle;


                case TRANSPORT_CONTENT_VERSION_URL:
                    tempUpdateTransportContentFiles = new ArrayList<>();
                    BufferedReader r1 = new BufferedReader(new InputStreamReader(input));
                    while ((s = r1.readLine()) != null) {
//                        if (s.contains("ver.")) {
//                            stmVersion = s.substring(0, s.indexOf("<"));
//                        } else if (s.contains("M")) {
//                            String sub = s.substring(s.lastIndexOf("M"));
//                            tempUpdateStmFiles.add(sub.substring(0, sub.indexOf("<")));
//                        } else if (s.contains("S")) {
//                            String sub = s.substring(s.lastIndexOf("S"));
//                            tempUpdateStmFiles.add(sub.substring(0, sub.indexOf("<")));
//                        }
//
                        tempUpdateTransportContentFiles.add(s);
//
                    }
                    r1.close();
                    bundle.putString("result", "transport content");
                    bundle.putString("ip", currentIp);
                    return bundle;

                case STATION_CONTENT_VERSION_URL:

                    tempUpdateStationaryContentFiles = new ArrayList<>();
                BufferedReader r2 = new BufferedReader(new InputStreamReader(input));
                while ((s = r2.readLine()) != null) {
//                        if (s.contains("ver.")) {
//                            stmVersion = s.substring(0, s.indexOf("<"));
//                        } else if (s.contains("M")) {
//                            String sub = s.substring(s.lastIndexOf("M"));
//                            tempUpdateStmFiles.add(sub.substring(0, sub.indexOf("<")));
//                        } else if (s.contains("S")) {
//                            String sub = s.substring(s.lastIndexOf("S"));
//                            tempUpdateStmFiles.add(sub.substring(0, sub.indexOf("<")));
//                        }
//
                    tempUpdateStationaryContentFiles.add(s);
//
                }
                r2.close();
                bundle.putString("result", "stationary content");
                bundle.putString("ip", currentIp);
                return bundle;

            }
        }

        }finally {
            try {
                if (output != null) output.close();
                if (input != null) input.close();
            } catch (IOException e) {
                Logger.d(Logger.DOWNLOAD_LOG, "exception: " + e);
            }
            if (c != null) c.disconnect();
        }
        return bundle;
    }



    @Override
    protected void onProgressUpdate(Integer... values) {
//        Logger.d(Logger.DOWNLOAD_LOG, "progress count: " + values[0]);
        listener.onProgressUpdate(values[0]);
        super.onProgressUpdate(values);
    }
}
