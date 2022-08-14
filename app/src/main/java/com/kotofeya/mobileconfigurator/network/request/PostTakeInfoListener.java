package com.kotofeya.mobileconfigurator.network.request;

public interface PostTakeInfoListener {
    void postTakeInfoSuccessful(String command, String ip, String content, String version);
    void postTakeInfoFailed(String command, String ip, String error);
}
