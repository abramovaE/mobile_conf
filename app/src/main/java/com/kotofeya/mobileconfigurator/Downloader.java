package com.kotofeya.mobileconfigurator;


import android.os.AsyncTask;
import android.os.Bundle;


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
    public static final String STM_URL = "http://95.161.210.44/update/data/stm/";


    public static final String OS_URL = "http://95.161.210.44/update/rootimg/root.img.bz2";

    public static File tempUpdateOsFile;
    public static List<String> tempUpdateStmFiles;

    private String osVersion;
    private String stmVersion;

    private OnTaskCompleted listener;


    public Downloader(OnTaskCompleted listener) {
        this.listener = listener;

    }

    @Override
    protected Bundle doInBackground(String... url) {
        try {
            return getContent(url[0]);
        }
        catch (IOException ex) {
            Logger.d(Logger.DOWNLOAD_LOG, "getContentException: " + ex.getMessage());
            return null;
        }
    }


    @Override
    protected void onPreExecute() {
        createTempUpdateOsFile();
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
//                    bundle.putString("ip", ip);
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
                    return bundle;

//                    return "Downloaded";

//                case STM_URL:
//
//
//
//                    return "Downloaded";

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
