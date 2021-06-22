package com.kotofeya.mobileconfigurator.fragments.update;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.ScannerAdapter;

import java.util.ArrayList;

public class SettingsScUartFragment extends UpdateFragment {


    @Override
    protected void loadUpdates() {

    }

    @Override
    protected void loadVersion() {

    }

    @Override
    protected void setMainTextLabelText() {

    }

    @Override
    protected ScannerAdapter getScannerAdapter() {
        return new ScannerAdapter(context, utils, ScannerAdapter.SETTINGS_SCUART, new ArrayList<>());
    }


    @Override
    public void onStart() {
        Logger.d(Logger.STM_LOG_LOG, "onStart");
        super.onStart();
        versionLabel.setVisibility(View.GONE);
        checkVersionButton.setVisibility(View.GONE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mainTxtLabel.setText("ScUart");
        return view;
    }
}
