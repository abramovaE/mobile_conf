package com.kotofeya.mobileconfigurator.fragments.update;

import android.view.View;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.RvAdapter;


public class SettingsWifiFragment extends UpdateFragment {

    @Override
    protected void loadUpdates() {
    }
    @Override
    protected void loadVersion() {
    }
    @Override
    protected void setMainTextLabelText() {
        mainTxtLabel.setText("Wifi settings");
    }
    @Override
    protected int getAdapterType() {
        return RvAdapter.STM_LOG;
    }
    @Override
    public void onStart() {
        Logger.d(Logger.STM_LOG_LOG, "onStart");
        super.onStart();
        versionLabel.setVisibility(View.GONE);
        checkVersionButton.setVisibility(View.GONE);
    }
    @Override
    public void setProgressBarVisible() {
    }
    @Override
    public void setProgressBarGone() {

    }
}
