package com.kotofeya.mobileconfigurator.fragments.config;

import android.os.Bundle;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;
import com.kotofeya.mobileconfigurator.Utils;

public class ConfigTransportFragment extends ConfigFragment {
    @Override
    public ScannerAdapter getScannerAdapter() {
        return new ScannerAdapter(context, utils, ScannerAdapter.CONFIG_TRANSPORT);
    }
    @Override
    public void scan(){
        utils.setRadioType(Utils.TRANSP_RADIO_TYPE);
        utils.getBluetooth().startScan(true);
    }

//
//    @Override
//    public void rescan(){
//        utils.clearTransivers();
//        scannerAdapter.notifyDataSetChanged();
////        scan();
//    }

    @Override
    public void setMainTextLabel() {
        mainTxtLabel.setText(R.string.config_transp_main_txt_label);
    }

    @Override
    public void onTaskCompleted(Bundle result) {}

    @Override
    public void onProgressUpdate(Integer downloaded) {

    }


}
