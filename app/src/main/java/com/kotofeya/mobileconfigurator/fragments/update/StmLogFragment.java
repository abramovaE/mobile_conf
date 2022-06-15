package com.kotofeya.mobileconfigurator.fragments.update;

import android.os.Bundle;
import android.view.View;

import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

public class StmLogFragment extends UpdateFragment {


    @Override
    public void setMainTextLabelText() {
        viewModel.setMainTxtLabel("Stm log");
    }
    @Override
    public RvAdapterType getAdapterType() {
        return RvAdapterType.SETTINGS;
    }

    @Override
    public void onStart() {
        Logger.d(Logger.STM_LOG_LOG, "onStart");
        super.onStart();
        binding.versionLabel.setVisibility(View.GONE);
        binding.checkVersionBtn.setVisibility(View.GONE);
    }

    @Override
    public void adapterItemOnClick(Transiver transiver) {
        String ssid = transiver.getSsid();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKeys.SSID_KEY, ssid);
        fragmentHandler.changeFragmentBundle(FragmentHandler.TRANSIVER_STM_LOG_FRAGMENT, bundle);
    }
}
