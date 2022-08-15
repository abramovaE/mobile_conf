package com.kotofeya.mobileconfigurator.fragments.config;

import android.view.View;

import com.kotofeya.mobileconfigurator.fragments.scanner.ScannerFragment;
import com.kotofeya.mobileconfigurator.rv_adapter.AdapterListener;

public abstract class ConfigFragment extends ScannerFragment implements AdapterListener {

    public AdapterListener getAdapterListener(){
        return this;
    }

    @Override
    public void onStart() {
        super.onStart();
        setMainTextLabelText();
        viewModel.setMainBtnRescanVisibility(View.GONE);
    }
}