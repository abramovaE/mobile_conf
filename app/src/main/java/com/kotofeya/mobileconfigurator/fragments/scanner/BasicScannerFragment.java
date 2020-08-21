package com.kotofeya.mobileconfigurator.fragments.scanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.WiFiLocalHotspot;

import java.util.List;

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
        return view;
    }


    @Override
    public void onTaskCompleted(Bundle result) {
        int resultCode = result.getInt("resultCode");
        String res = result.getString("result");

        Logger.d(Logger.UPDATE_OS_LOG, "resultCode: " + resultCode);

        if(resultCode == TaskCode.TAKE_CODE){
            utils.addTakeInfo(res, true);
            scannerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onProgressUpdate(Integer downloaded) {
    }

    private void rescan(){
        utils.clearTransivers();
        scannerAdapter.notifyDataSetChanged();
        scan();
    }

    public void scan(){
        List<String> clients = WiFiLocalHotspot.getInstance().getClientList();
        for(String s: clients){
            SshConnection connection = new SshConnection(this);
            connection.execute(s, SshConnection.TAKE_CODE);
        }
    }
}
