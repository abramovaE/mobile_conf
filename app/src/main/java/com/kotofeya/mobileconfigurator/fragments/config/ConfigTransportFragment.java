package com.kotofeya.mobileconfigurator.fragments.config;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
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
        utils.getBluetooth().startScan(false);
    }


    @Override
    public void rescan(){
        utils.clearTransivers();
        scannerAdapter.notifyDataSetChanged();
//        scan();
    }

    @Override
    public void setMainTextLabel() {
        mainTxtLabel.setText(R.string.config_transp_main_txt_label);
    }
}
