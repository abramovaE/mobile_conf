package com.kotofeya.mobileconfigurator;


import android.os.AsyncTask;


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


public class Downloader extends AsyncTask<String, Integer, String> {


    public static final String OS_VERSION_URL = "http://95.161.210.44/update/rootimg";
    public static final String STM_VERSION_URL = "http://95.161.210.44/update/data/stm";

    public static final String OS_URL = "http://95.161.210.44/update/rootimg/root.img.bz2";

    public static File tempUpdateOsFile;
    private String osVersion;
    private String stmVersion;

    private OnTaskCompleted listener;


    public Downloader(OnTaskCompleted listener) {
        this.listener = listener;

    }

    @Override
    protected String doInBackground(String... url) {
        try {
            return getContent(url[0]);
        }
        catch (IOException ex) {
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

    @Override
    protected void onPostExecute(String result) {
        Logger.d(Logger.DOWNLOAD_LOG, "onPostExecute");
        listener.onTaskCompleted(result);
    }

    private String getContent(String stringUrl) throws IOException {
        Logger.d(Logger.DOWNLOAD_LOG, "getting version");
        OutputStream output = null;
        URL url = new URL(stringUrl);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("GET");
        c.setConnectTimeout(12000);
        c.setReadTimeout(15000);
        c.connect();
        InputStream input = c.getInputStream();
        try {
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
                    return "Release OS: " + osVersion;

                case STM_VERSION_URL:
                    BufferedReader r = new BufferedReader(new InputStreamReader(input));
                    while ((s = r.readLine()) != null) {
                        if (s.contains("ver.")) {
                            stmVersion = s.substring(0, s.indexOf("<"));
                        }
                    }
                    r.close();
                    return "Release: " + stmVersion;


                case OS_URL:

                    output = new FileOutputStream(tempUpdateOsFile);
                    byte data[] = new byte[4096];
                    int count;

                    while ((count = input.read(data)) != -1) {
                        output.write(data, 0, count);
                        publishProgress((int) (100 * (tempUpdateOsFile.length()/40755927.0)));
                    }
                    output.close();
                    return "Downloaded";
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
        return "";
    }



    @Override
    protected void onProgressUpdate(Integer... values) {
//        Logger.d(Logger.DOWNLOAD_LOG, "progress count: " + values[0]);
        listener.onProgressUpdate(values[0]);
        super.onProgressUpdate(values);
    }
}
