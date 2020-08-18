package com.kotofeya.mobileconfigurator.fragments.update;



import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Downloader;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;

public class UpdateOsFragment extends UpdateFragment {

    String storageVersion = "storage version";
    TextView storageVersionTxt;

//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = super.onCreateView(inflater, container, savedInstanceState);
//        mainTxtLabel.setText(R.string.update_os_main_txt_label);
//
//        scannerAdapter = new ScannerAdapter(context, utils, ScannerAdapter.UPDATE_OS_TYPE);
//        lvScanner.setAdapter(scannerAdapter);
//
//        utils.getBluetooth().stopScan(true);
//        utils.clearTransivers();
//        scannerAdapter.notifyDataSetChanged();
//        loadVersion();
//        scan();
//        return view;
//    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  super.onCreateView(inflater, container, savedInstanceState);
        storageVersionTxt = view.findViewById(R.id.scanner_label1);
        storageVersionTxt.setVisibility(View.VISIBLE);
        storageVersionTxt.setText(App.get().getUpdateOsFileVersion());
        return view;
    }

    @Override
    void loadVersion(){
        Downloader downloader = new Downloader(this);
        downloader.execute(Downloader.OS_VERSION_URL);
    }

    @Override
    void setMainTextLabelText() {
        mainTxtLabel.setText(R.string.update_os_main_txt_label);
    }

    @Override
    ScannerAdapter getScannerAdapter() {
        return new ScannerAdapter(context, utils, ScannerAdapter.UPDATE_OS_TYPE);
    }

    void loadUpdates(){
        Downloader downloader = new Downloader(this);
        downloader.execute(Downloader.OS_URL);
    }

    @Override
    public void onTaskCompleted(Bundle bundle) {
        super.onTaskCompleted(bundle);
        storageVersionTxt.setText(App.get().getUpdateOsFileVersion());
    }
}
