package com.kotofeya.mobileconfigurator.fragments.config;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.SshConnectionRunnable;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.WiFiLocalHotspot;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.List;

public class ConfigStatFragment extends ConfigFragment {

    private final Handler myHandler = new Handler();

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
        utils.getBluetooth().startScan(true);
    }





//    @Override
//    public void rescan(){
//        utils.clearTransivers();
//        scannerAdapter.notifyDataSetChanged();
//    }

    @Override
    public void onTaskCompleted(Bundle result) {
        Logger.d(Logger.CONTENT_LOG, "resultCode: " + result.getInt("resultCode"));
        if(result.getInt("resultCode") != 0){
            Logger.d(Logger.CONTENT_LOG, "result: " + result);
        }
        String res = result.getString("result");
        if(result.getInt("resultCode") == TaskCode.TAKE_CODE){
            utils.addTakeInfo(res, false);
            myHandler.post(updateRunnable);
        }
        if(!utils.getBluetooth().getmScanning().get()) {
            utils.getBluetooth().startScan(true);
        }
//        scannerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onProgressUpdate(Integer downloaded) {

    }

    public void basicScan(){
        utils.getTakeInfo(this);
//        List<String> clients = WiFiLocalHotspot.getInstance().getClientList();
//        for(String s: clients){
//            SshConnection connection = new SshConnection(this);
//            connection.execute(s, SshConnection.TAKE_CODE);
//        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
//        if(utils.needScanStationaryTransivers()){
//            Logger.d(Logger.CONFIG_LOG, "scan for ip");
//            basicScan();
//        }


        if(utils.needScanStationaryTransivers()){
            Logger.d(Logger.CONFIG_LOG, "scan for ip");
            utils.getBluetooth().stopScan(true);
            basicScan();
        }


        return view;
    }


    private void updateUI()
    {
        scannerAdapter.notifyDataSetChanged();
    }

    final Runnable updateRunnable = new Runnable() {
        public void run() {
            updateUI();
        }
    };
}
