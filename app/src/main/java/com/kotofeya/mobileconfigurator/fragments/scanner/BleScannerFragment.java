package com.kotofeya.mobileconfigurator.fragments.scanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;
import com.kotofeya.mobileconfigurator.Utils;


public class BleScannerFragment extends ScannerFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mainTxtLabel.setText(R.string.ble_scan_main_txt_label);
        scannerAdapter = new ScannerAdapter(context, utils, ScannerAdapter.BLE_SCANNER_TYPE);
        lvScanner.setAdapter(scannerAdapter);
        return view;
    }

    public void scan(){
        utils.setRadioType(Utils.ALL_RADIO_TYPE);
        utils.getBluetooth().startScan(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        mainBtnRescan.setVisibility(View.GONE);
        utils.getBluetooth().stopScan(true);
        utils.clearTransivers();
        scannerAdapter.notifyDataSetChanged();
        scan();
    }

//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        utils.clearTransivers();
//        scannerAdapter.notifyDataSetChanged();
//    }


    @Override
    public void onStop() {
        super.onStop();
        Logger.d(Logger.BLE_SCANNER_LOG, "onStop");
        utils.getBluetooth().stopScan(false);
        utils.clearTransivers();
        scannerAdapter.notifyDataSetChanged();
    }
}
