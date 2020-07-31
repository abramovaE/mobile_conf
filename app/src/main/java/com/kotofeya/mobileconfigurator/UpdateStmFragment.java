package com.kotofeya.mobileconfigurator;

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

import java.util.List;

public class UpdateStmFragment extends UpdateFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mainTxtLabel.setText(R.string.update_stm_main_txt_label);

        scannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.d(Logger.UPDATE_OS_LOG, "check stm scanner button was pressed");
//                progressBar.setVisibility(View.VISIBLE);
//                loadUpdates();
            }
        });



        scannerAdapter = new ScannerAdapter(context, utils, ScannerAdapter.UPDATE_STM_TYPE);
        lvScanner.setAdapter(scannerAdapter);

        utils.getBluetooth().stopScan(true);
        utils.clearTransivers();
        scannerAdapter.notifyDataSetChanged();
        loadVersion();
        scan();
        return view;
    }


    @Override
    public void onTaskCompleted(String result) {

        if(result.contains("Release: ")){
            version = result;
            scannerLabel.setText(result);
        }

        scannerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onProgressUpdate(Integer downloaded) {

    }

    private void loadVersion(){
        Downloader downloader = new Downloader(this);
        downloader.execute(Downloader.STM_VERSION_URL);
    }


}
