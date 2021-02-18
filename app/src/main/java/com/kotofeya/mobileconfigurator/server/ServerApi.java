package com.kotofeya.mobileconfigurator.server;

import retrofit2.http.POST;

public interface ServerApi {

    // TODO: 15.02.2021 change the script name
    @POST("autorization")
    boolean autorization(String login, String password);
}
