package com.kotofeya.mobileconfigurator.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface JSONPlaceHolderApi {
    @GET("/posts/{id}")
    public Call<Post> getPostWithID(@Path("id") int id);

    @FormUrlEncoded
    @POST("/interface/index.php")
    public Call<PostResponse> postCommand(@Field("timeN") String time, @Field("user") String user,
                                    @Field("secret") String secret, @Field("command") String command);



}
