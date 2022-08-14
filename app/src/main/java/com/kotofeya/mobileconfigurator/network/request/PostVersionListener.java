package com.kotofeya.mobileconfigurator.network.request;

public interface PostVersionListener {
    void postVersionSuccessful(String command, String ip, String version);
    void postVersionFailed(String command, String ip, String error);
}
