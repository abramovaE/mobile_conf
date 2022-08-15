package com.kotofeya.mobileconfigurator.network.request;

import java.util.Map;

public interface PostStatContentVersionListener {
    void postStatContentVersionSuccessful(String command, Map<String, String> versions);
    void postStatContentVersionFailed(String command, String error);
}
