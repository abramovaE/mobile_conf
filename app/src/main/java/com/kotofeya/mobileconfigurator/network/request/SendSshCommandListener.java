package com.kotofeya.mobileconfigurator.network.request;

public interface SendSshCommandListener {
    void sendSshCommandSuccessful(String response);
    void sendSshCommandFailed(String error);
}
