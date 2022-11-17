package com.kotofeya.mobileconfigurator.network.request;

import androidx.annotation.NonNull;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.network.NetworkUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CheckUserUseCase implements Runnable {
    public static final String TAG = CheckUserUseCase.class.getSimpleName();

    private static final String URL_CHECK_USER = "http://95.161.210.44/is_mobile_conf_user_valid.php";
    private final String login;
    private final String password;
    private String message = null;

    private final CheckUserListener checkUserListener;

    public CheckUserUseCase(String login,
                            String password,
                            CheckUserListener checkUserListener) {
        this.login = login;
        this.password = password;
        this.checkUserListener = checkUserListener;
    }

    @Override
    public void run() {
        Logger.d(Logger.CHECK_USER_LOG, "Check user: login - " + login + ", password - " + password);
        URL u;
        try {
            u = new URL(URL_CHECK_USER);
            OkHttpClient okHttpClient = new OkHttpClient();
            FormBody formBody = new FormBody.Builder()
                    .add("login", login)
                    .add("password", password)
                    .build();
            Request request = new Request.Builder()
                    .url(u)
                    .post(formBody)
                    .build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    int respCode = response.code();
                    if(respCode == HttpURLConnection.HTTP_OK){
                        String content = NetworkUtils.responseToString(response);
                        try {
                            JSONObject jsonObject = new JSONObject(content);
                            int code = Integer.parseInt(jsonObject.getString("code"));
                            String level = jsonObject.getString("level");
                            if (code == 1) {
                                // TODO: 17.08.2022 remove
//                    level = "full";
                                checkUserListener.checkUserSuccessful(level);
                            } else if (code == 0) {
                                message = App.get().getResources().getString(R.string.incorrect_autorization);
                                checkUserListener.checkUserFailed(message);
                            }
                        } catch (JSONException e){
                            checkUserListener.checkUserFailed(e.getMessage());
                        }
                    } else {
                        checkUserListener.checkUserFailed(respCode + "");
                    }
                }
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    message = App.get().getResources().getString(R.string.server_not_response);
                    checkUserListener.checkUserFailed(message);
                }
            });
        } catch (IOException e) {
            checkUserListener.checkUserFailed(message);
        }
    }
}

