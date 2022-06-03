package com.kotofeya.mobileconfigurator.fragments.scanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterFactory;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;

import java.util.ArrayList;

public class BleScannerFragment extends ScannerFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        rvAdapter = RvAdapterFactory.getRvAdapter(
                RvAdapterType.BLE_SCANNER_TYPE, new ArrayList<>(), null);
        binding.rvScanner.setAdapter(rvAdapter);
        viewModel.setMainTxtLabel(getString(R.string.ble_scan_main_txt_label));
        return view;
    }

    @Override
    public void scan(){
        utils.setRadioType(Utils.ALL_RADIO_TYPE);
        utils.getNewBleScanner().startScan();
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.setMainBtnRescanVisibility(View.GONE);
        utils.getNewBleScanner().stopScan();
        viewModel.clearTransivers();
        scan();
    }

    @Override
    public void onStop() {
        super.onStop();
        Logger.d(Logger.BLE_SCANNER_LOG, "onStop");
        utils.getNewBleScanner().stopScan();
        viewModel.clearTransivers();
    }
}
