package com.kotofeya.mobileconfigurator.fragments.update;

import com.kotofeya.mobileconfigurator.Downloader;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;

public class UpdateStmFragment extends UpdateFragment {


    @Override
    void loadVersion(){
        Downloader downloader = new Downloader(this);
        downloader.execute(Downloader.STM_VERSION_URL);
    }

    @Override
    void setMainTextLabelText() {
        mainTxtLabel.setText(R.string.update_stm_main_txt_label);
    }

    @Override
    ScannerAdapter getScannerAdapter() {
        return new ScannerAdapter(context, utils, ScannerAdapter.UPDATE_STM_TYPE);
    }

    void loadUpdates(){
        Downloader downloader = new Downloader(this);
        downloader.execute(Downloader.STM_VERSION_URL);
    }

}
