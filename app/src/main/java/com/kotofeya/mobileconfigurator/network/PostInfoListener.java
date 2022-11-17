package com.kotofeya.mobileconfigurator.network;

public interface PostInfoListener {
    void postInfoSuccessful(String ip, String response);
    void postInfoFailed(String error);
}
