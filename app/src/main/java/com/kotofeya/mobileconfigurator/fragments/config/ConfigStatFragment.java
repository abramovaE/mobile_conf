package com.kotofeya.mobileconfigurator.fragments.config;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.activities.InterfaceUpdateListener;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

public class ConfigStatFragment extends ConfigFragment implements InterfaceUpdateListener {

    private AlertDialog scanClientsDialog;

    @Override
    public void setMainTextLabel() {
        viewModel.setMainTxtLabel(getString(R.string.config_stat_main_txt_label));
    }

    @Override
    public void scan(){
        utils.setRadioType(Utils.STAT_RADIO_TYPE);
        utils.getNewBleScanner().startScan();
    }

    @Override
    protected RvAdapterType getAdapterType() {
        return RvAdapterType.CONFIG_STATION;
    }

    @Override
    public void onTaskCompleted(Bundle result) {
        utils.getNewBleScanner().startScan();
    }

    public void basicScan(){
        utils.getTakeInfo();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(viewModel.needScanStationaryTransivers()){
            utils.getNewBleScanner().stopScan();
            basicScan();
        }
        viewModel.getStationaryInformers().observe(getViewLifecycleOwner(), this::updateUI);
    }

    @Override
    public void clientsScanFinished() {
        scanClientsDialog.dismiss();
        utils.getTakeInfo();
    }

    @Override
    public void adapterItemOnClick(Transiver transiver) {
        String ssid = transiver.getSsid();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKeys.SSID_KEY, ssid);
        fragmentHandler.changeFragmentBundle(FragmentHandler.STATION_CONTENT_FRAGMENT, bundle);
    }
}
