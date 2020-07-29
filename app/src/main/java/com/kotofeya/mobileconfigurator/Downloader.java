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


public class Downloader extends AsyncTask<String, Void, Boolean> {


    public static final String OS_VERSION_URL = "http://95.161.210.44/update/rootimg";
    public static final String OS_URL = "http://95.161.210.44/update/rootimg/root.img.bz2";


//    private static final String SERVER = "http://95.161.210.44/update/PACK.tar.bz2";
//    private static final String VERSION_URL = "http://95.161.210.44/update";
//    private static final String CITY_URL = "http://95.161.210.44/update/city.json";


    public static File tempUpdateOsFile;
    //    private File tempCityfile;
    private String osVersion;


    private OnTaskCompleted listener;

    private boolean isSuccessful;


//    private boolean isUpdating;


//    public boolean isUpdating() {
//        return isUpdating;
//    }


    public Downloader(OnTaskCompleted listener) {
        this.listener = listener;

    }

    @Override
    protected Boolean doInBackground(String... url) {
        try {

                return getContent(url[0]);


//            return getContent();

        }
        catch (IOException ex) {
            return null;
        }
    }


    @Override
    protected void onPreExecute() {

        isSuccessful = false;
        Logger.d(Logger.DOWNLOAD_LOG, "downloading os server version");
//        isUpdating = true;
        File outputDir = App.get().getCacheDir();
        try {
            tempUpdateOsFile = new File(outputDir + "/root.img.bz2");
            tempUpdateOsFile.deleteOnExit();
//            tempCityfile = File.createTempFile("city", ".json", outputDir);
//            tempCityfile.deleteOnExit();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onPostExecute(Boolean isSuccessful) {
        Logger.d(Logger.DOWNLOAD_LOG, "onPostExecute");
        this.isSuccessful = isSuccessful;

        listener.onTaskCompleted("Release OS: " + osVersion);
    }

    private Boolean getContent(String stringUrl) throws IOException {

        Logger.d(Logger.DOWNLOAD_LOG, "getting version");

        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection c = null;
        URL url = new URL(stringUrl);
        c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("GET");
        c.setConnectTimeout(12000);
        c.setReadTimeout(15000);
        c.connect();
        input = c.getInputStream();
        try {


            switch (stringUrl) {
                case OS_VERSION_URL:
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    String s;
                    while ((s = reader.readLine()) != null) {
                        if (s.contains("ver.")) {
                            osVersion = s;
                            Logger.d(Logger.DOWNLOAD_LOG, "server osVersion is " + osVersion);
                        }
                    }
                    reader.close();
                    break;

                case OS_URL:
                    Logger.d(Logger.DOWNLOAD_LOG, "start os update downloading");
                    output = new FileOutputStream(tempUpdateOsFile);
                    Logger.d(Logger.DOWNLOAD_LOG, "outputfile: " + tempUpdateOsFile.getAbsolutePath());
                    byte data[] = new byte[4096];
                    int count;

                    while ((count = input.read(data)) != -1) {
                        output.write(data, 0, count);
                        Logger.d(Logger.DOWNLOAD_LOG, "outputfile downloading: " + count);
                    }

                    Logger.d(Logger.DOWNLOAD_LOG, "outputfile downloaded, temp/file length: " + tempUpdateOsFile.length());
                    output.close();
                    break;
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

        return false;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }


    public File getTempUpdateOsFile() {
        return tempUpdateOsFile;
    }
}
