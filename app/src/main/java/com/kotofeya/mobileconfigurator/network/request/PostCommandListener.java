package com.kotofeya.mobileconfigurator.network.request;

public interface PostCommandListener {
    void postCommandSuccessful(String response);
    void postCommandFailed(String error);
}
