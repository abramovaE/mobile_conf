package com.kotofeya.mobileconfigurator.hotspot;

import java.util.List;

public interface DeviceScanListener {
    void scanFinished(List<String> clients);
}
