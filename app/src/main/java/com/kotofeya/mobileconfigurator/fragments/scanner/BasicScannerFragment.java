package com.kotofeya.mobileconfigurator.fragments.scanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.activities.InterfaceUpdateListener;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterFactory;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;

import java.util.ArrayList;


public class BasicScannerFragment extends ScannerFragment implements OnTaskCompleted, InterfaceUpdateListener {

    private AlertDialog scanClientsDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rvAdapter = RvAdapterFactory.getRvAdapter(
                RvAdapterType.BASIC_SCANNER_TYPE, new ArrayList<>(), null);
        binding.rvScanner.setAdapter(rvAdapter);
        viewModel.setMainTxtLabel(getString(R.string.basic_scan_main_txt_label));

        ImageButton mainBtnRescan = requireActivity().findViewById(R.id.main_btn_rescan);
        mainBtnRescan.setOnClickListener(v -> rescan());

        utils.getNewBleScanner().stopScan();
        viewModel.getIsGetTakeInfoFinished().observe(getViewLifecycleOwner(), this::updateScannerProgressBarTv);
        binding.progressTv.setVisibility(View.GONE);

        return binding.getRoot();
    }

    private void updateScannerProgressBarTv(Boolean aBoolean) {
        if(!aBoolean){
            binding.progressTv.setText(Utils.MESSAGE_TAKE_INFO);
            binding.progressTv.setVisibility(View.VISIBLE);
        } else {
            binding.progressTv.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.setMainBtnRescanVisibility(View.VISIBLE);
        utils.getNewBleScanner().stopScan();
        viewModel.clearTransivers();
        scan();
    }

    @Override
    public void onTaskCompleted(Bundle result) {
        int resultCode = result.getInt("resultCode");
        String res = result.getString("result");
        Logger.d(Logger.BASIC_SCANNER_LOG, "result: " + result);
        Logger.d(Logger.BASIC_SCANNER_LOG, "resultCode: " + resultCode);
        if(resultCode == TaskCode.SSH_ERROR_CODE){
            if(res.contains("Connection refused") || res.contains("Auth fail")){
                utils.removeClient(result.getString("ip"));
            } else {
                fragmentHandler.showMessage("Error: " + result);
            }
        }
        else if(resultCode == TaskCode.DOWNLOADER_ERROR_CODE){
            fragmentHandler.showMessage("Error: " + result);
        }
    }

    private void rescan(){
        Logger.d(Logger.BASIC_SCANNER_LOG, "rescan");
        utils.clearClients();
        utils.clearMap();
        viewModel.clearTransivers();
        scanClientsDialog = utils.getScanClientsDialog();
        scanClientsDialog.show();
        utils.updateClients(this);
    }

    @Override
    public void scan(){
        if(utils.hasClients()) {
            utils.getTakeInfo();
        }
    }

    @Override
    public void clientsScanFinished() {
        Logger.d(Logger.BASIC_SCANNER_LOG, "clientsScanFinished()");
        scanClientsDialog.dismiss();
        scan();
    }
}
