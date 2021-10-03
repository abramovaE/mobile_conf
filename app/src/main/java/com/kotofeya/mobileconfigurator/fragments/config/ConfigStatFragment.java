package com.kotofeya.mobileconfigurator.fragments.config;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapter;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterFactory;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;

import java.util.ArrayList;

public class ConfigStatFragment extends ConfigFragment {

    @Override
    public RvAdapter getRvAdapter() {
        return RvAdapterFactory.getRvAdapter(context, utils, RvAdapterType.CONFIG_STATION, new ArrayList<>());
    }

    @Override
    public void setMainTextLabel() {
        mainTxtLabel.setText(R.string.config_stat_main_txt_label);
    }

    @Override
    public void scan(){
        utils.setRadioType(Utils.STAT_RADIO_TYPE);
        utils.getNewBleScanner().startScan();
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
            Logger.d(Logger.CONFIG_LOG, "scan for ip");
            utils.getNewBleScanner().stopScan();
            basicScan();
        }
        viewModel.getStationaryInformers().observe(getViewLifecycleOwner(), this::updateUI);
    }
}
