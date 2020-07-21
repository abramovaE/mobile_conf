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

public class ConfigStatFragment extends Fragment {


    private Context context;
    private Utils utils;
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
        ListView lvScanner = view.findViewById(R.id.lv_scanner);

        TextView mainTxtLabel = ((MainMenu)context).findViewById(R.id.main_txt_label);
        mainTxtLabel.setText(R.string.config_stat_main_txt_label);
        Button mainBtnRescan = ((MainMenu)context).findViewById(R.id.main_btn_rescan);
        mainBtnRescan.setVisibility(View.VISIBLE);
        mainBtnRescan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rescan();
            }
        });

        utils.getTransivers().clear();
        utils.setTransiversLv(lvScanner);
        scan();
        scannerAdapter = new ScannerAdapter(context, utils, ScannerAdapter.CONFIG_STATION);
        lvScanner.setAdapter(scannerAdapter);
        return view;
    }



    private void scan(){
        utils.setRadioType(Utils.STAT_RADIO_TYPE);
        utils.getBluetooth().startScan(false);
    }


    private void rescan(){
        utils.getTransivers().clear();
        scannerAdapter.notifyDataSetChanged();
//        scan();
    }
}
