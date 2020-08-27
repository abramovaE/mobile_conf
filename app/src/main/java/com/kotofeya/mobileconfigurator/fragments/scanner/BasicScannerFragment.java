package com.kotofeya.mobileconfigurator.fragments.scanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.SshConnectionRunnable;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.WiFiLocalHotspot;

public class BasicScannerFragment extends ScannerFragment implements OnTaskCompleted {

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
//        utils.clearTransivers();
//        scannerAdapter.notifyDataSetChanged();

        if(utils.getTransivers().isEmpty()) {
            scan();
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mainBtnRescan.setVisibility(View.VISIBLE);
//        utils.getBluetooth().stopScan(true);
//        utils.clearTransivers();
//        scannerAdapter.notifyDataSetChanged();
//        scan();
    }

    @Override
    public void onTaskCompleted(Bundle result) {
        int resultCode = result.getInt("resultCode");
        String res = result.getString("result");
        Logger.d(Logger.BASIC_SCANNER_LOG, "resultCode: " + resultCode);
        if(resultCode == TaskCode.TAKE_CODE){
            utils.addTakeInfo(res, true);
            scannerAdapter.notifyDataSetChanged();
        }
        else if(resultCode == TaskCode.SSH_ERROR_CODE){
            if(res.contains("Connection refused")){
                utils.removeClient(result.getString("ip"));
            }
            else {utils.showMessage("Error: " + result);}
        }
        else if(resultCode == TaskCode.DOWNLOADER_ERROR_CODE){
            utils.showMessage("Error: " + result);
        }
    }

    @Override
    public void onProgressUpdate(Integer downloaded) {
    }

    private void rescan(){
        utils.clearClients();
        utils.clearMap();
        utils.clearTransivers();
        scannerAdapter.notifyDataSetChanged();
        scan();
    }

    public void scan(){
        utils.setClients(WiFiLocalHotspot.getInstance().getClientList());
        for(String s: utils.getClients()){
            SshConnection connection = new SshConnection(this);
            connection.execute(s, SshConnection.TAKE_CODE);
        }
    }
}
