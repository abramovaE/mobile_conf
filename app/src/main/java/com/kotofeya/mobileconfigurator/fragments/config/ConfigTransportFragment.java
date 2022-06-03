package com.kotofeya.mobileconfigurator.fragments.config;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;
import com.kotofeya.mobileconfigurator.transivers.Transiver;


public class ConfigTransportFragment extends ConfigFragment {

    @Override
    public void scan(){
        utils.setRadioType(Utils.TRANSP_RADIO_TYPE);
        utils.getNewBleScanner().startScan();
    }

    @Override
    protected RvAdapterType getAdapterType() {
        return RvAdapterType.CONFIG_TRANSPORT;
    }

    @Override
    public void setMainTextLabel() {
        viewModel.setMainTxtLabel(getString(R.string.config_transp_main_txt_label));
    }

    @Override
    public void onTaskCompleted(Bundle result) {}

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getTranspInformers().observe(getViewLifecycleOwner(), this::updateUI);
    }

    @Override
    public void adapterItemOnClick(Transiver transiver) {
        String ssid = transiver.getSsid();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKeys.SSID_KEY, ssid);
        fragmentHandler.changeFragmentBundle(FragmentHandler.TRANSPORT_CONTENT_FRAGMENT, bundle);
    }
}
