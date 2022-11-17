package com.kotofeya.mobileconfigurator.network.request;


public interface SendLogToServerListener {
    void sendLogSuccessful();
    void sendLogFailed(String error);

}