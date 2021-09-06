package com.kotofeya.mobileconfigurator.fragments.scanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterFactory;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.activities.CustomViewModel;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.ArrayList;
import java.util.List;


public class BleScannerFragment extends ScannerFragment {

    private CustomViewModel viewModel;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mainTxtLabel.setText(R.string.ble_scan_main_txt_label);
        rvAdapter = RvAdapterFactory.getRvAdapter(context, utils, RvAdapterType.BLE_SCANNER_TYPE, new ArrayList<>());
        rvScanner.setAdapter(rvAdapter);
        return view;
    }

    public void scan(){
        utils.setRadioType(Utils.ALL_RADIO_TYPE);
        utils.getNewBleScanner().startScan();
    }

    @Override
    public void onStart() {
        super.onStart();
        mainBtnRescan.setVisibility(View.GONE);
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.viewModel = ViewModelProviders.of(getActivity(), new CustomViewModel.ModelFactory()).get(CustomViewModel.class);
        viewModel.getTransivers().observe(getViewLifecycleOwner(), this::updateUI);
    }

    private void updateUI(List<Transiver> transivers){
        Logger.d(Logger.BLE_SCANNER_LOG, "update ui");
        rvAdapter.setObjects(transivers);
        rvAdapter.notifyDataSetChanged();
    }
}
