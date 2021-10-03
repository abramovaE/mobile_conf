package com.kotofeya.mobileconfigurator.fragments.update;

import android.view.View;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;


public class SettingsWifiFragment extends UpdateFragment {

    @Override
    protected void setMainTextLabelText() {
        mainTxtLabel.setText("Wifi settings");
    }
    @Override
    protected RvAdapterType getAdapterType() {
        return RvAdapterType.SETTINGS_WIFI;
    }
    @Override
    public void onStart() {
        Logger.d(Logger.STM_LOG_LOG, "onStart");
        super.onStart();
        versionLabel.setVisibility(View.GONE);
        checkVersionButton.setVisibility(View.GONE);
    }
}
