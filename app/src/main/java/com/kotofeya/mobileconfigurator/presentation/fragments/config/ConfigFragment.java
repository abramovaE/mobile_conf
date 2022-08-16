package com.kotofeya.mobileconfigurator.presentation.fragments.config;

import android.view.View;

import com.kotofeya.mobileconfigurator.presentation.fragments.scanner.ScannerFragment;
import com.kotofeya.mobileconfigurator.presentation.rv_adapter.AdapterListener;

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