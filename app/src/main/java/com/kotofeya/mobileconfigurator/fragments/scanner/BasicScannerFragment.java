package com.kotofeya.mobileconfigurator.fragments.scanner;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;
import com.kotofeya.mobileconfigurator.network.PostCommand;


public class BasicScannerFragment extends ScannerFragment implements OnTaskCompleted {
    private final Handler myHandler = new Handler();

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
        scannerAdapter = new ScannerAdapter(context, utils, ScannerAdapter.BASIC_SCANNER_TYPE);
        lvScanner.setAdapter(scannerAdapter);
        utils.getBluetooth().stopScan(true);

        Logger.d(Logger.UTILS_LOG, "transivers: " + utils.getTransivers());
        if(utils.getTransivers().isEmpty()) {
            scan();
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mainBtnRescan.setVisibility(View.VISIBLE);
    }

    @Override
    public void onTaskCompleted(Bundle result) {
        int resultCode = result.getInt("resultCode");
        String res = result.getString("result");
        Logger.d(Logger.BASIC_SCANNER_LOG, "resultCode: " + resultCode);
        if(resultCode == TaskCode.TAKE_CODE){
            utils.addTakeInfo(res, true);
            myHandler.post(updateRunnable);
        } else if(resultCode == TaskCode.SSH_ERROR_CODE){
            if(res.contains("Connection refused") || res.contains("Auth fail")){
                utils.removeClient(result.getString("ip"));
            } else {utils.showMessage("Error: " + result);}
        } else if(resultCode == TaskCode.DOWNLOADER_ERROR_CODE){
            utils.showMessage("Error: " + result);
        }

        else if(resultCode == PostCommand.getResponseCode(PostCommand.TAKE_INFO_FULL)){
            utils.addTakeInfoFull(result.getString("ip"), result.getParcelable("takeInfoFull"), true);
            myHandler.post(updateRunnable);
        } else if(resultCode == PostCommand.getResponseCode(PostCommand.TAKE_INFO_FULL_ERROR)){
            if(res.contains("Connection refused") || res.contains("Auth fail")){
                utils.removeClient(result.getString("ip"));
            }
            else {
                utils.showMessage("Error: " + result);
            }
        }
    }


    private void updateUI() {
        scannerAdapter.notifyDataSetChanged();
    }


    final Runnable updateRunnable = new Runnable() {
        public void run() {
            updateUI();
        }
    };

    @Override
    public void onProgressUpdate(Integer downloaded) {
    }

    private void rescan(){
        Logger.d(Logger.BASIC_SCANNER_LOG, "rescan");
        utils.clearClients();
        utils.clearMap();
        utils.clearTransivers();
        scannerAdapter.notifyDataSetChanged();
        scan();
    }

    public void scan(){
        boolean wifiPermission = checkPermission();
        if(wifiPermission) {
            utils.getTakeInfo(this);
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
}
