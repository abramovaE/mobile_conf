package com.kotofeya.mobileconfigurator.fragments.config;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.RvAdapter;
import com.kotofeya.mobileconfigurator.Utils;

import java.util.ArrayList;

public class ConfigTransportFragment extends ConfigFragment {

    @Override
    public RvAdapter getRvAdapter() {
        return new RvAdapter(context, utils, RvAdapter.CONFIG_TRANSPORT, new ArrayList<>());
    }
    @Override
    public void scan(){
        utils.setRadioType(Utils.TRANSP_RADIO_TYPE);
        utils.getNewBleScanner().startScan();
    }

    @Override
    public void setMainTextLabel() {
        mainTxtLabel.setText(R.string.config_transp_main_txt_label);
    }

    @Override
    public void onTaskCompleted(Bundle result) {}

    @Override
    public void onProgressUpdate(Integer downloaded) {}

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getTranspInformers().observe(getViewLifecycleOwner(), this::updateUI);
    }
}
