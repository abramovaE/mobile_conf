package com.kotofeya.mobileconfigurator;


import android.os.AsyncTask;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;


public class Downloader extends AsyncTask<Void, Void, Boolean> {


    private static final String OS_VERSION_URL = "http://95.161.210.44/update/rootimg";
    private static final String OS_URL = "http://95.161.210.44/update/rootimg/";


//    private static final String SERVER = "http://95.161.210.44/update/PACK.tar.bz2";
//    private static final String VERSION_URL = "http://95.161.210.44/update";
//    private static final String CITY_URL = "http://95.161.210.44/update/city.json";


    private File tempFile;
    //    private File tempCityfile;
    private String osVersion;


    private OnTaskCompleted listener;



//    private boolean isUpdating;


//    public boolean isUpdating() {
//        return isUpdating;
//    }


    public Downloader(OnTaskCompleted listener) {
        this.listener = listener;

    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            return getContent();

        } catch (IOException ex) {
            return null;
        }
    }


    @Override
    protected void onPreExecute() {

        Logger.d(Logger.DOWNLOAD_LOG, "downloading os server version");
//        isUpdating = true;
        File outputDir = App.get().getCacheDir();
        try {
            tempFile = new File(outputDir + "/root.img.bz2");
//            tempFile = File.createTempFile("root", ".img.bz2", outputDir);

            tempFile.deleteOnExit();
//            tempCityfile = File.createTempFile("city", ".json", outputDir);
//            tempCityfile.deleteOnExit();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onPostExecute(Boolean isNeedUpdate) {
        Logger.d(Logger.DOWNLOAD_LOG, "onPostExecute");
        listener.onTaskCompleted("Release OS: " + osVersion);
    }

    private Boolean getContent() throws IOException {

        Logger.d(Logger.DOWNLOAD_LOG, "getting version");

        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection c = null;

        try {
            URL versionUrl = new URL(OS_VERSION_URL);
            c = (HttpURLConnection) versionUrl.openConnection();
            c.setRequestMethod("GET");
            c.setConnectTimeout(2000);
            c.setReadTimeout(5000);
            c.connect();

            input = c.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String s;
            while ((s = reader.readLine()) != null) {
                if (s.contains("ver.")) {
                    osVersion = s;
                    Logger.d(Logger.DOWNLOAD_LOG, "server osVersion is " + osVersion);
                }
            }
            reader.close();


                Logger.d(Logger.DOWNLOAD_LOG, "start os update downloading");
//
                URL url=new URL(OS_URL);
                c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.setReadTimeout(5000);
                c.connect();

                input = c.getInputStream();
                output = new FileOutputStream(tempFile);

                Logger.d(Logger.DOWNLOAD_LOG, "outputfile: " + tempFile.getAbsolutePath());
                byte data[] = new byte[4096];
                int count;
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }
                c.disconnect();
                return true;

        } finally {
            try {
                if (output != null) output.close();
                if (input != null) input.close();
            } catch (IOException e) {
                Logger.d(Logger.DOWNLOAD_LOG, "exception: " + e);
            }
            if (c != null) c.disconnect();
        }

    }





//    private void updateBase(){
//
//        Logger.d(Logger.WIFI_LOG, "start base updating");
//        try {
//            Logger.d(Logger.WIFI_LOG, "tempfile created:" + tempFile);
//            InputStream fi = new FileInputStream(tempFile);
//            InputStream bi = new BufferedInputStream(fi);
//            InputStream bz2 = new BZip2CompressorInputStream(bi);
//            TarArchiveInputStream tarIn = new TarArchiveInputStream(bz2);
//            ArchiveEntry entry = null;
//            while (null != (entry = tarIn.getNextEntry())) {
//                Logger.d(Logger.DOWNLOAD_LOG, entry.getName());
//
//                if (entry.getSize() < 1) {
//                    continue;
//                }
//                String entryName = entry.getName();
//                String data1 = new String(IOUtils.toByteArray(tarIn), "cp1251");
//                JSONObject jsonObject = new JSONObject(data1);
//                if (entryName.contains("perepicJSON")) {
//                    String ssid = entryName.substring(entryName.indexOf("perepicJSON/") + 12, entryName.indexOf("/stat"));
//                    String jsonCrc = jsonObject.getJSONObject("data").getString("crc");
////                    String baseCrc = App.get().getDBHelper().getStatCrc(ssid);
//                    String lang = entryName.substring(entryName.indexOf("stat") + 5, entryName.indexOf("json") -1);
////                    if (!baseCrc.equals(jsonCrc)) {
//                        Logger.d(Logger.OTHER_LOG, "write to database, stationary, ssid: " + ssid  + ", lang: " + lang + ", jsonCrc: " + jsonCrc);
//                        App.get().getDBHelper().addStatToTable(ssid, jsonObject.toString(), lang, jsonCrc);
////                    }
//                }
//
//                else if(entryName.contains("transport")){
//                    JSONObject data = jsonObject.getJSONObject("data");
//                    int increment = data.getInt("increment");
//                    String city = entryName.substring(entryName.indexOf("ts") + 3, entryName.lastIndexOf("_"));
//                    String tableName = "transport" + "_" + city;
//                    int tableIncr =  App.get().getDBHelper().getTableIncrement(tableName);
//                    if (tableIncr != increment) {
//                        Route[] routes = new Gson().fromJson(data.get("routes").toString(), Route[].class);
//                        Logger.d(Logger.OTHER_LOG, "jsonroutes: " + Arrays.toString(routes));
//                        Logger.d(Logger.DB_LOG, "updating table: " + tableName);
//                        Logger.d(Logger.OTHER_LOG, "write to database, transport: " + tableName + ", routes count: " + routes.length);
//                        App.get().getDBHelper().updateTransportTable(tableName, routes, increment);
//                    }
//
//                }
//            }
//            tarIn.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }





}
