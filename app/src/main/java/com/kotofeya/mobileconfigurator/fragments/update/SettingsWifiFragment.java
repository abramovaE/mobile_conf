package com.kotofeya.mobileconfigurator.fragments.update;

import android.os.Bundle;
import android.view.View;

import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;
import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;

public class SettingsWifiFragment extends UpdateFragment {

    private static final String TAG = SettingsWifiFragment.class.getSimpleName();

    @Override
    public void setMainTextLabelText() {
        viewModel.setMainTxtLabel("Wifi settings");
    }

    @Override
    public RvAdapterType getAdapterType() {
        return RvAdapterType.SETTINGS;
    }

    @Override
    public void onStart() {
        Logger.d(TAG, new Throwable().getStackTrace()[0].getMethodName());
        super.onStart();
        binding.version1.setVisibility(View.GONE);
        binding.checkVersionBtn.setVisibility(View.GONE);
    }

    @Override
    public void adapterItemOnClick(Transceiver transvceiver) {
        Logger.d(TAG, new Throwable().getStackTrace()[0].getMethodName());
        String ssid = transvceiver.getSsid();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKeys.SSID_KEY, ssid);
        fragmentHandler.changeFragmentBundle(FragmentHandler.TRANSCEIVER_SETTINGS_WIFI_FRAGMENT, bundle);
    }
}
