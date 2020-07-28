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

public class BasicScannerFragment extends Fragment implements OnTaskCompleted {

    private Context context;
    private Utils utils;
    ListView lvScanner;
    ScannerAdapter scannerAdapter;
    Button mainBtnRescan;


    @Override
    public void onAttach(Context context) {
        this.context = context;
        this.utils = ((MainMenu) context).getUtils();
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
        lvScanner = view.findViewById(R.id.lv_scanner);

        TextView mainTxtLabel = ((MainMenu)context).findViewById(R.id.main_txt_label);
        mainTxtLabel.setText(R.string.basic_scan_main_txt_label);

        mainBtnRescan = ((MainMenu)context).findViewById(R.id.main_btn_rescan);
        mainBtnRescan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rescan();
            }
        });

        scannerAdapter = new ScannerAdapter(context, utils, ScannerAdapter.BASIC_SCANNER_TYPE);
        lvScanner.setAdapter(scannerAdapter);
        return view;
    }


    @Override
    public void onTaskCompleted(String result) {
        scannerAdapter.notifyDataSetChanged();
    }




    private void rescan(){
        utils.clearTransivers();
        scannerAdapter.notifyDataSetChanged();
        scan();
    }

    private void scan(){
        List<String> clients = WiFiLocalHotspot.getInstance().getClientList();
        for(String s: clients){
            Transiver transiver = new Transiver(s);
            utils.addSshTransiver(transiver);
            SshConnection connection = new SshConnection(this);
            utils.setCurrentTransiver(transiver);
            connection.execute(transiver, SshConnection.TAKE_COMMAND);
        }

    }

}
