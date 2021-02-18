package com.kotofeya.mobileconfigurator.server;

import retrofit2.Call;
import retrofit2.http.POST;

public interface AuthorizationApi {

    // TODO: 15.02.2021 need script adress
    @POST("autorization")
    Call<Boolean> autorization(String login, String password);
}
