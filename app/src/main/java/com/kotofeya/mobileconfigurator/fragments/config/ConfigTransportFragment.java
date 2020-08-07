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

import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;
import com.kotofeya.mobileconfigurator.Utils;

public class ConfigTransportFragment extends Fragment {


    private Context context;
    private Utils utils;
    private ScannerAdapter scannerAdapter;
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
        mainBtnRescan.setVisibility(View.VISIBLE);
        utils.getBluetooth().stopScan(true);
        utils.clearTransivers();
        scannerAdapter.notifyDataSetChanged();
        scan();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.scanner_fragment, container, false);
        ListView lvScanner = view.findViewById(R.id.lv_scanner);

        TextView mainTxtLabel = ((MainActivity)context).findViewById(R.id.main_txt_label);
        mainTxtLabel.setText(R.string.config_transp_main_txt_label);
        mainBtnRescan = ((MainActivity)context).findViewById(R.id.main_btn_rescan);
        mainBtnRescan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rescan();
            }
        });

        utils.setTransiversLv(lvScanner);
        scannerAdapter = new ScannerAdapter(context, utils, ScannerAdapter.CONFIG_TRANSPORT);
        lvScanner.setAdapter(scannerAdapter);
        return view;
    }

    private void scan(){
        utils.setRadioType(Utils.TRANSP_RADIO_TYPE);
        utils.getBluetooth().startScan(false);
    }


    private void rescan(){
        utils.clearTransivers();
        scannerAdapter.notifyDataSetChanged();
//        scan();
    }
}
