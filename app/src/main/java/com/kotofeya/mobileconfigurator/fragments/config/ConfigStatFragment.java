package com.kotofeya.mobileconfigurator.fragments.config;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.SshConnectionRunnable;
import com.kotofeya.mobileconfigurator.WiFiLocalHotspot;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.List;

public class ConfigStatFragment extends ConfigFragment {

    @Override
    public ScannerAdapter getScannerAdapter() {
        return new ScannerAdapter(context, utils, ScannerAdapter.CONFIG_STATION);
    }

    @Override
    public void setMainTextLabel() {
        mainTxtLabel.setText(R.string.config_stat_main_txt_label);
    }

    @Override
    public void scan(){
        utils.setRadioType(Utils.STAT_RADIO_TYPE);
        utils.getBluetooth().startScan(false);
    }


    @Override
    public void rescan(){
        utils.clearTransivers();
        scannerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTaskCompleted(Bundle result) {
        if(result.getInt("resultCode") != 0){
            Logger.d(Logger.CONTENT_LOG, "result: " + result);
        }
        String res = result.getString("result");
        if (res.split("\n").length > 10) {
            utils.addTakeInfo(res, false);
        }
        scannerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onProgressUpdate(Integer downloaded) {

    }


    public void basicScan(){
        List<String> clients = WiFiLocalHotspot.getInstance().getClientList();
        for(String s: clients){
            SshConnection connection = new SshConnection(this);
            connection.execute(s, SshConnection.TAKE_CODE);
        }
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        basicScan();
        return view;
    }
}
