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


public class BleScannerFragment extends Fragment {

    private Context context;
    private Utils utils;
    ListView lvScanner;
    ScannerAdapter scannerAdapter;

    @Override
    public void onAttach(Context context) {
        this.context = context;
        this.utils = ((MainMenu) context).getUtils();
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.scanner_fragment, container, false);
        lvScanner = view.findViewById(R.id.lv_scanner);

        TextView mainTxtLabel = ((MainMenu)context).findViewById(R.id.main_txt_label);
        mainTxtLabel.setText(R.string.ble_scan_main_txt_label);


        utils.getTransivers().clear();
        utils.setTransiversLv(lvScanner);
        scan();
        scannerAdapter = new ScannerAdapter(context, utils, ScannerAdapter.BLE_SCANNER_TYPE);
        lvScanner.setAdapter(scannerAdapter);
        scannerAdapter.notifyDataSetChanged();
        return view;
    }


    private void scan(){
        utils.setRadioType(Utils.ALL_RADIO_TYPE);
        utils.getBluetooth().startScan(false);
    }




}
