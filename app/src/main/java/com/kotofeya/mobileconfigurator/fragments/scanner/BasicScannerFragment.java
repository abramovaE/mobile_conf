package com.kotofeya.mobileconfigurator.fragments.scanner;

import android.os.Bundle;
import android.view.View;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.rv_adapter.AdapterListener;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;


public class BasicScannerFragment extends ScannerFragment implements OnTaskCompleted {

    private static final String TAG = BasicScannerFragment.class.getSimpleName();

    public RvAdapterType getAdapterType(){return RvAdapterType.BASIC_SCANNER_TYPE;}
    public AdapterListener getAdapterListener(){return null;}

    public void setMainTextLabelText(){
        viewModel.setMainTxtLabel(getString(R.string.basic_scan_main_txt_label));
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.setMainBtnRescanVisibility(View.VISIBLE);
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
                Logger.d(TAG, "error: " + res);
                clientsHandler.removeClient(result.getString("ip"));
            } else {
                fragmentHandler.showMessage("Error: " + result);
            }
        }
        else if(resultCode == TaskCode.DOWNLOADER_ERROR_CODE){
            fragmentHandler.showMessage("Error: " + result);
        }
    }
}
