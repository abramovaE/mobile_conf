package com.kotofeya.mobileconfigurator.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkService {
    public static NetworkService networkService;
    private static final String BASE_URL = "https://95.161.210.44";


    private Retrofit retrofit;

    public static NetworkService getInstance(){
        if(networkService == null){
            networkService = new NetworkService();
        }
        return networkService;
    }

    private NetworkService(){
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public JSONPlaceHolderApi getJsonApi(){
        return retrofit.create(JSONPlaceHolderApi.class);
    }

    public void setBaseUrl(String ip){
        retrofit = new Retrofit.Builder()
                .baseUrl("http://" + ip)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
