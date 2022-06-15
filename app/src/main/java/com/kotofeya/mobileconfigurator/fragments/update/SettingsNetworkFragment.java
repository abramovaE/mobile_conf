package com.kotofeya.mobileconfigurator.fragments.update;

import android.os.Bundle;
import android.view.View;

import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

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
        binding.versionLabel.setVisibility(View.GONE);
        binding.checkVersionBtn.setVisibility(View.GONE);
    }

    @Override
    public void adapterItemOnClick(Transiver transiver) {
        String ssid = transiver.getSsid();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKeys.SSID_KEY, ssid);
        fragmentHandler.changeFragmentBundle(FragmentHandler.TRANSIVER_SETTINGS_NETWORK_FRAGMENT, bundle);
    }
}
