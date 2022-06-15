package com.kotofeya.mobileconfigurator.fragments.config;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

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
        viewModel.getStationaryInformers().observe(getViewLifecycleOwner(), this::updateUI);
        viewModel.isClientsScanning().observe(getViewLifecycleOwner(), this::updateClientsScanning);
    }

    public void basicScan(){
        clientsHandler.pollConnectedClients();
    }


    private void updateClientsScanning(Boolean aBoolean) {
        if(!aBoolean){
            clientsHandler.pollConnectedClients();
        }
    }

    @Override
    public void adapterItemOnClick(Transiver transiver) {
        String ssid = transiver.getSsid();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKeys.SSID_KEY, ssid);
        fragmentHandler.changeFragmentBundle(FragmentHandler.STATION_CONTENT_FRAGMENT, bundle);
    }
}
