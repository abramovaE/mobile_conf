package com.kotofeya.mobileconfigurator.network.request;

public interface PostOsVersionListener {
    void postOsVersionSuccessful(String command, String version);
    void postOsVersionFailed(String command, String error);
}
