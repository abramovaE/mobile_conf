package com.kotofeya.mobileconfigurator.fragments.update;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;

public class UpdateContentFragment extends UpdateFragment {

    LinearLayout label;

    @Override
    public void onTaskCompleted(Bundle bundle) {
        super.onTaskCompleted(bundle);
        scannerAdapter.notifyDataSetChanged();
    }

    @Override
    void loadUpdates() {
    }

    @Override
    void loadVersion() {
    }

    @Override
    void setMainTextLabelText() {
        mainTxtLabel.setText(R.string.update_content_main_txt_label);
    }

    @Override
    ScannerAdapter getScannerAdapter() {
        return new ScannerAdapter(context, utils, ScannerAdapter.UPDATE_CONTENT_TYPE);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        label = view.findViewById(R.id.update_content_label);
        return view;
    }

    @Override
    public void onStart() {
        Logger.d(Logger.UPDATE_CONTENT_LOG, "onStart");
        super.onStart();
        versionLabel.setVisibility(View.GONE);
        checkVersionButton.setVisibility(View.GONE);
        mainBtnRescan.setVisibility(View.VISIBLE);
        label.setVisibility(View.VISIBLE);
    }




}
