package com.kotofeya.mobileconfigurator.network.request;

public interface SettingsUpdatePhpListener {
    void updateSuccessful(String ip, String response);
    void updateFailed(String ip, String errorMessage);
}
