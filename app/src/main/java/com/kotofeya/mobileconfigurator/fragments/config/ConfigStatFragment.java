package com.kotofeya.mobileconfigurator.fragments.config;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.WiFiLocalHotspot;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.fragments.update.UpdateFragment;
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
//        scan();
    }

    @Override
    public void onTaskCompleted(Bundle result) {
        Logger.d(Logger.CONTENT_LOG, "result: " + result);
        String res = result.getString("result");
        if (res.split("\n").length > 10) {
            Transiver transiver = new Transiver(null, res);
            utils.addSshTransiver(transiver);
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
            connection.execute(s, SshConnection.TAKE_COMMAND);
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
