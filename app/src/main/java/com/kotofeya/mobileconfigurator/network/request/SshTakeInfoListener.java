package com.kotofeya.mobileconfigurator.network.request;

public interface SshTakeInfoListener {
    void sshTakeInfoSuccessful(String response);
    void sshTakeInfoFailed(String ip, String error);
}
