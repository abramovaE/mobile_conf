package com.kotofeya.mobileconfigurator.network.request;

public interface CheckUserListener {
    void checkUserSuccessful(String level);
    void checkUserFailed(String error);
}
