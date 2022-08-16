package com.kotofeya.mobileconfigurator.presentation.fragments.config;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.presentation.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.presentation.rv_adapter.RvAdapterType;
import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;

public class ConfigStatFragment extends ConfigFragment {
    @Override
    public RvAdapterType getAdapterType() {
        return RvAdapterType.CONFIG_STATION;
    }

    @Override
    public void setMainTextLabelText() {
        viewModel.setMainTxtLabel(getString(R.string.config_stat_main_txt_label));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        basicScan();
//        viewModel.getTransceivers().observe(getViewLifecycleOwner(), this::updateUI);
        viewModel.getStationaryInformers().observe(getViewLifecycleOwner(), this::updateUI);
        viewModel.isScanning.observe(getViewLifecycleOwner(), this::updateClientsScanning);
    }

    public void basicScan(){
        viewModel.pollConnectedClients();
    }


    private void updateClientsScanning(Boolean aBoolean) {
        if(!aBoolean){
            viewModel.pollConnectedClients();
        }
    }

    @Override
    public void adapterItemOnClick(Transceiver transceiver) {
        String ssid = transceiver.getSsid();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKeys.SSID_KEY, ssid);
        fragmentHandler.changeFragmentBundle(FragmentHandler.STATION_CONTENT_FRAGMENT, bundle);
    }
}
