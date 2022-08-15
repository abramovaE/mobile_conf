package com.kotofeya.mobileconfigurator.fragments.update;

import android.os.Bundle;
import android.view.View;

import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;
import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;

public class SettingsNetworkFragment extends UpdateFragment {

    private static final String TAG = SettingsNetworkFragment.class.getSimpleName();
    @Override
    public void setMainTextLabelText() {
        viewModel.setMainTxtLabel("Network settings");
    }
    @Override
    public RvAdapterType getAdapterType() {
        return RvAdapterType.SETTINGS;
    }

    @Override
    public void onStart() {
        Logger.d(TAG, "onStart()");
        super.onStart();
        binding.version1.setVisibility(View.GONE);
        binding.checkVersionBtn.setVisibility(View.GONE);
    }

    @Override
    public void adapterItemOnClick(Transceiver transceiver) {
        String ssid = transceiver.getSsid();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKeys.SSID_KEY, ssid);
        fragmentHandler.changeFragmentBundle(FragmentHandler.TRANSCEIVER_SETTINGS_NETWORK_FRAGMENT, bundle);
    }
}
