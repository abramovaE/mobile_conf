package com.kotofeya.mobileconfigurator.fragments.config;

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

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.activities.MainActivity;


public abstract class ConfigFragment extends Fragment implements OnTaskCompleted {


    ScannerAdapter scannerAdapter;
    TextView mainTxtLabel;

    public Context context;
    public Utils utils;
    public Button mainBtnRescan;


    @Override
    public void onAttach(Context context) {
        this.context = context;
        this.utils = ((MainActivity) context).getUtils();
        super.onAttach(context);

    }



    @Override
    public void onStart() {
        super.onStart();
        setMainTextLabel();
        mainBtnRescan.setVisibility(View.GONE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.scanner_fragment, container, false);
        ListView lvScanner = view.findViewById(R.id.lv_scanner);

        mainTxtLabel = ((MainActivity)context).findViewById(R.id.main_txt_label);
        mainBtnRescan = ((MainActivity)context).findViewById(R.id.main_btn_rescan);
        mainBtnRescan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rescan();
            }
        });

        utils.setTransiversLv(lvScanner);
        scannerAdapter = getScannerAdapter();
        lvScanner.setAdapter(scannerAdapter);
        utils.getBluetooth().stopScan(true);
//        Logger.d(Logger.CONFIG_LOG, "clearTransivers");
//        utils.clearTransivers();
//        scannerAdapter.notifyDataSetChanged();
        scan();
        return view;
    }

    public abstract ScannerAdapter getScannerAdapter();
    public abstract void setMainTextLabel();
    public abstract void rescan();
    public abstract void scan();

//    @Override
//    public void onStop() {
//        super.onStop();
//        Logger.d(Logger.CONFIG_LOG, "onStop");
//        utils.getBluetooth().stopScan(false);
//        utils.clearTransivers();
//        scannerAdapter.notifyDataSetChanged();
//    }
}
