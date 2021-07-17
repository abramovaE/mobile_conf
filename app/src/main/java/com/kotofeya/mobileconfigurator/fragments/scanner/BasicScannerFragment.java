package com.kotofeya.mobileconfigurator.fragments.scanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;
import com.kotofeya.mobileconfigurator.activities.CustomViewModel;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.ArrayList;
import java.util.List;


public class BasicScannerFragment extends ScannerFragment implements OnTaskCompleted {

    private CustomViewModel viewModel;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mainTxtLabel.setText(R.string.basic_scan_main_txt_label);
        mainBtnRescan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rescan();
            }
        });
        scannerAdapter = new ScannerAdapter(context, utils, ScannerAdapter.BASIC_SCANNER_TYPE, new ArrayList<>());
        lvScanner.setAdapter(scannerAdapter);
//        utils.getBluetooth().stopScan(true);
        utils.getNewBleScanner().stopScan();
        this.viewModel = ViewModelProviders.of(getActivity(), new CustomViewModel.ModelFactory()).get(CustomViewModel.class);
//        if(viewModel.getTransivers().getValue() == null || viewModel.getTransivers().getValue().isEmpty()) {
//            scan();
//        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mainBtnRescan.setVisibility(View.VISIBLE);
        utils.getNewBleScanner().stopScan();
        viewModel.clearTransivers();
        scan();
    }

    @Override
    public void onTaskCompleted(Bundle result) {

        int resultCode = result.getInt("resultCode");
        String res = result.getString("result");
        Logger.d(Logger.BASIC_SCANNER_LOG, "resultCode: " + resultCode);

        if(resultCode == TaskCode.SSH_ERROR_CODE){
            if(res.contains("Connection refused") || res.contains("Auth fail")){
                utils.removeClient(result.getString("ip"));
            } else {utils.showMessage("Error: " + result);}
        }
        else if(resultCode == TaskCode.DOWNLOADER_ERROR_CODE){
            utils.showMessage("Error: " + result);
        }
    }

//    private void updateUI() {
//        Logger.d(Logger.BASIC_SCANNER_LOG, "update ui, scanneradapter: " + scannerAdapter);
//
//        scannerAdapter.notifyDataSetChanged();
//    }

    @Override
    public void onProgressUpdate(Integer downloaded) {
    }

    @Override
    public void setProgressBarVisible() {

    }

    @Override
    public void setProgressBarGone() {

    }

    @Override
    public void clearProgressBar() {

    }

    private void rescan(){
        Logger.d(Logger.BASIC_SCANNER_LOG, "rescan");
        utils.clearClients();
        utils.clearMap();
        viewModel.clearTransivers();
//        utils.clearTransivers();
//        scannerAdapter.notifyDataSetChanged();
        scan();
    }

    public void scan(){
        boolean wifiPermission = checkPermission();
        if(wifiPermission) {
            utils.getTakeInfo();
        }
        else {
            askPermission();
        }
    }

    private boolean checkPermission(){
        return true;
    }

    private void askPermission(){
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getTransivers().observe(getViewLifecycleOwner(), this::updateUI);

    }

    private void updateUI(List<Transiver> transivers){
        Logger.d(Logger.BASIC_SCANNER_LOG, "update ui, scanneradapter: " + scannerAdapter);
        scannerAdapter.setObjects(transivers);
        scannerAdapter.notifyDataSetChanged();
    }

}
