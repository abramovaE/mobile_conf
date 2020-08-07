package com.kotofeya.mobileconfigurator.fragments.scanner;

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

import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;
import com.kotofeya.mobileconfigurator.Utils;


public class BleScannerFragment extends Fragment {

    private Context context;
    private Utils utils;
    ListView lvScanner;
    ScannerAdapter scannerAdapter;
    Button mainBtnRescan;

    @Override
    public void onAttach(Context context) {
        this.context = context;
        this.utils = ((MainActivity) context).getUtils();
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        super.onStart();
        mainBtnRescan.setVisibility(View.GONE);
        utils.getBluetooth().stopScan(true);
        utils.clearTransivers();
        scannerAdapter.notifyDataSetChanged();
        scan();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.scanner_fragment, container, false);
        lvScanner = view.findViewById(R.id.lv_scanner);

        TextView mainTxtLabel = ((MainActivity)context).findViewById(R.id.main_txt_label);
        mainTxtLabel.setText(R.string.ble_scan_main_txt_label);

        mainBtnRescan = ((MainActivity)context).findViewById(R.id.main_btn_rescan);


        utils.setTransiversLv(lvScanner);
        scannerAdapter = new ScannerAdapter(context, utils, ScannerAdapter.BLE_SCANNER_TYPE);
        lvScanner.setAdapter(scannerAdapter);
        return view;
    }


    private void scan(){
        utils.setRadioType(Utils.ALL_RADIO_TYPE);
        utils.getBluetooth().startScan(false);
    }




}
