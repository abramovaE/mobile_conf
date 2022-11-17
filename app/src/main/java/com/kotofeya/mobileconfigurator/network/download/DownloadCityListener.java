package com.kotofeya.mobileconfigurator.network.download;

public interface DownloadCityListener {
    void downloadSuccessful(String result);
    void downloadFailed(String error);
}
