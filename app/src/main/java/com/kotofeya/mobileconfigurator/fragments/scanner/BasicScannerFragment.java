package com.kotofeya.mobileconfigurator.fragments.scanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.activities.InterfaceUpdateListener;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterFactory;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.activities.CustomViewModel;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.ArrayList;
import java.util.List;


public class BasicScannerFragment extends ScannerFragment implements OnTaskCompleted, InterfaceUpdateListener {

    private AlertDialog scanClientsDialog;
    private CustomViewModel viewModel;
    protected AlertDialog getTakeInfoDialog;

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
        rvAdapter = RvAdapterFactory.getRvAdapter(context, utils, RvAdapterType.BASIC_SCANNER_TYPE, new ArrayList<>());
        rvScanner.setAdapter(rvAdapter);
        utils.getNewBleScanner().stopScan();
        this.viewModel = ViewModelProviders.of(getActivity(), new CustomViewModel.ModelFactory()).get(CustomViewModel.class);
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

    @Override
    public void onProgressUpdate(Integer downloaded) {
    }


    private void rescan(){
        Logger.d(Logger.BASIC_SCANNER_LOG, "rescan");
        utils.clearClients();
        utils.clearMap();
        viewModel.clearTransivers();
        scanClientsDialog = utils.getScanClientsDialog().show();
        utils.updateClients(this);
//        getTakeInfoDialog = utils.getTakeInfoDialog().show();
//        utils.getTakeInfo(this);
    }

    public void scan(){
        if(checkPermission()) {
            getTakeInfoDialog = utils.getTakeInfoDialog().show();
            utils.getTakeInfo(this);
//            scanClientsDialog = utils.getScanClientsDialog().show();
//            utils.updateClients(this);
//            utils.getTakeInfo(this);
        } else {
            askPermission();
        }
    }

    private boolean checkPermission(){
        return true;
    }

    private void askPermission(){ }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getTransivers().observe(getViewLifecycleOwner(), this::updateUI);
    }

    private void updateUI(List<Transiver> transivers){
        Logger.d(Logger.BASIC_SCANNER_LOG, "update ui: " + transivers.size());
        rvAdapter.setObjects(transivers);
        rvAdapter.notifyDataSetChanged();
    }

    @Override
    public void clientsScanFinished() {
        scanClientsDialog.dismiss();
        getTakeInfoDialog = utils.getTakeInfoDialog().show();
        utils.getTakeInfo(this);
    }

    @Override
    public void finishedGetTakeInfo(){
        getTakeInfoDialog.dismiss();
    }
}
