package com.kotofeya.mobileconfigurator.network;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.network.response.TakeInfoFull;
import com.kotofeya.mobileconfigurator.network.response.TakeInfoTranspContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ResponseParser {
    private static final String TAG = ResponseParser.class.getSimpleName();

    public static String parseVersion(String response){
        return response.substring(response.lastIndexOf("version") + 8);
    }

    public static TakeInfoFull parseTakeInfoFull(String response, double jsonVersion){
        Logger.d(TAG, "response: " + response);
        Logger.d(TAG, "jsonVersion: " + jsonVersion);
        TakeInfoFull takeInfoFull = null;
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(response
                    .replace("<pre>", "")
                    .replace("</pre>", ""));
            JSONObject properties = jsonObject.getJSONObject("properties");

            takeInfoFull = new GsonBuilder()
                .setVersion(jsonVersion)
                    .create()
                .fromJson(properties.toString(), TakeInfoFull.class);

            if(!jsonObject.get("content").toString().equals("null")) {
                try {
                    JSONArray con = jsonObject.getJSONArray("content");
                    if (takeInfoFull.getType().equals("transport")) {
                        setTransportContent(takeInfoFull, con);
                    } else if (takeInfoFull.getType().equals("stationary")) {
                    }
                } catch (JsonSyntaxException | JSONException e) {
                    Logger.d(TAG, "json exception: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return takeInfoFull;
    }

    private static void setTransportContent(TakeInfoFull takeInfoFull, JSONArray content){
        try {
            List<TakeInfoTranspContent> takeInfoTranspContents = new ArrayList<>();
            for (int i = 0; i < content.length(); i++) {
                JSONObject object;
                object = content.getJSONObject(i).getJSONObject("transpContents");
                TakeInfoTranspContent takeInfoTranspContent = new GsonBuilder()
                        .create().fromJson(object.toString(), TakeInfoTranspContent.class);
                takeInfoTranspContents.add(takeInfoTranspContent);
            }
            takeInfoFull.setTranspContents(takeInfoTranspContents);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}