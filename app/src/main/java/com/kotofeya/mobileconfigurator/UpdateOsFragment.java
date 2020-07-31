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



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mainTxtLabel.setText(R.string.update_os_main_txt_label);

        scannerAdapter = new ScannerAdapter(context, utils, ScannerAdapter.UPDATE_OS_TYPE);
        lvScanner.setAdapter(scannerAdapter);

        utils.getBluetooth().stopScan(true);
        utils.clearTransivers();
        scannerAdapter.notifyDataSetChanged();
        loadVersion();
        scan();
        return view;
    }




    private void loadVersion(){
        Downloader downloader = new Downloader(this);
        downloader.execute(Downloader.OS_VERSION_URL);
    }

    void loadUpdates(){
        Downloader downloader = new Downloader(this);
        downloader.execute(Downloader.OS_URL);
    }
}
