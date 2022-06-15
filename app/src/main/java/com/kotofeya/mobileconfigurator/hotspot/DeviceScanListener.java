package com.kotofeya.mobileconfigurator.hotspot;

import java.util.List;

public interface DeviceScanListener {
    void pingClientsFinished(List<String> clients, boolean isNeedPoll);
}
