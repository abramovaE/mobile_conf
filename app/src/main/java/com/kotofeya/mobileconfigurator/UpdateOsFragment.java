package com.kotofeya.mobileconfigurator;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

public class UpdateOsFragment extends UpdateFragment {

    ProgressBar progressBar;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mainTxtLabel.setText(R.string.update_os_main_txt_label);
        progressBar = view.findViewById(R.id.scanner_progressBar);

        scannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.d(Logger.UPDATE_OS_LOG, "check updates button was pressed");
                progressBar.setVisibility(View.VISIBLE);
                loadUpdates();
            }
        });

        scannerAdapter = new ScannerAdapter(context, utils, ScannerAdapter.UPDATE_OS_TYPE);
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
        Logger.d(Logger.UPDATE_OS_LOG, "onTaskCompleted, result: " + result);
        if(result.contains("Release OS: ")){
            version = result;
            scannerLabel.setText(result);
        }

        else if(result.contains("Downloaded")){
            Transiver transiver = utils.getCurrentTransiver();
            utils.removeTransiver(transiver);
            utils.setCurrentTransiver(null);
            scannerAdapter.notifyDataSetChanged();
            Toast.makeText(context, "Downloaded", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }

        else {
            Logger.d(Logger.UPDATE_OS_LOG, "notifyDataSetChanged, transivers: " + utils.getTransivers().size());
            scannerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onProgressUpdate(Integer downloaded) {
        progressBar.setProgress(downloaded);
    }
    private void loadVersion(){
        Downloader downloader = new Downloader(this);
        downloader.execute(Downloader.OS_VERSION_URL);
    }

    private void loadUpdates(){
        Downloader downloader = new Downloader(this);
        downloader.execute(Downloader.OS_URL);
    }
}
