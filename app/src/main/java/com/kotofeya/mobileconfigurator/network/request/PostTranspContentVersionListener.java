package com.kotofeya.mobileconfigurator.network.request;

import java.util.List;

public interface PostTranspContentVersionListener {
    void postTranspContentVersionSuccessful(String command, List<String> versions);
    void postTranspContentVersionFailed(String command, String error);
}
