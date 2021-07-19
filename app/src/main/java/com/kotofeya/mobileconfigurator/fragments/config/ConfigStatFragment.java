package com.kotofeya.mobileconfigurator.fragments.config;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;
import com.kotofeya.mobileconfigurator.Utils;

import java.util.ArrayList;

public class ConfigStatFragment extends ConfigFragment {

    @Override
    public ScannerAdapter getScannerAdapter() {
        return new ScannerAdapter(context, utils, ScannerAdapter.CONFIG_STATION, new ArrayList<>());
    }

    @Override
    public void setMainTextLabel() {
        mainTxtLabel.setText(R.string.config_stat_main_txt_label);
    }

    @Override
    public void scan(){
        utils.setRadioType(Utils.STAT_RADIO_TYPE);
        utils.getNewBleScanner().startScan();
//        utils.getBluetooth().startScan(true);
    }

    @Override
    public void onTaskCompleted(Bundle result) {
//        if(!utils.getNewBleScanner().getmScanning().get()) {
            utils.getNewBleScanner().startScan();
//        }
    }

    @Override
    public void onProgressUpdate(Integer downloaded) {

    }


    public void basicScan(){
        utils.getTakeInfo();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if(utils.needScanStationaryTransivers()){
            Logger.d(Logger.CONFIG_LOG, "scan for ip");
            utils.getNewBleScanner().stopScan();
//            utils.getBluetooth().stopScan(true);
            basicScan();
        }
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getStationaryInformers().observe(getViewLifecycleOwner(), this::updateUI);
    }

}
