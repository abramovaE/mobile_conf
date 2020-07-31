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


public abstract class UpdateFragment extends Fragment implements OnTaskCompleted{

    protected Context context;
    protected Utils utils;
    ListView lvScanner;
    ScannerAdapter scannerAdapter;
    Button mainBtnRescan;
    TextView scannerLabel;
    Button scannerButton;
    String version = "version";
    TextView mainTxtLabel;


    @Override
    public void onAttach(Context context) {
        this.context = context;
        this.utils = ((MainMenu) context).getUtils();
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        Logger.d(Logger.UPDATE_OS_LOG, "onStart");
        super.onStart();
        scannerLabel.setVisibility(View.VISIBLE);
        scannerLabel.setText(version);
        scannerButton.setVisibility(View.VISIBLE);
        mainBtnRescan.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Logger.d(Logger.UPDATE_OS_LOG, "onCreate");
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.scanner_fragment, container, false);

        lvScanner = view.findViewById(R.id.lv_scanner);
        scannerLabel = view.findViewById(R.id.scanner_label);
        scannerButton = view.findViewById(R.id.scanner_btn);
        mainTxtLabel = ((MainMenu)context).findViewById(R.id.main_txt_label);
        mainBtnRescan = ((MainMenu)context).findViewById(R.id.main_btn_rescan);

        mainBtnRescan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                utils.clearTransivers();
                scannerAdapter.notifyDataSetChanged();
                scan();
            }
        });



//        scannerButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Logger.d(Logger.UPDATE_OS_LOG, "check updates button was pressed");
////                scannerLabel.setText("");
//                progressBar.setVisibility(View.VISIBLE);
//
//                loadUpdates();
//            }
//        });
//
//        scannerAdapter = new ScannerAdapter(context, utils, ScannerAdapter.UPDATE_OS_TYPE);
//        lvScanner.setAdapter(scannerAdapter);
//
//        utils.getBluetooth().stopScan(true);
//        utils.clearTransivers();
//        scannerAdapter.notifyDataSetChanged();
//        loadVersion();
//        scan();
        return view;
    }




    @Override
    public void onResume() {
        Logger.d(Logger.UPDATE_OS_LOG, "onResume");
        super.onResume();
    }


    protected void scan(){
        List<String> clients = WiFiLocalHotspot.getInstance().getClientList();
        for(String s: clients){
            Transiver transiver = new Transiver(s);
            utils.addSshTransiver(transiver);
            SshConnection connection = new SshConnection(this);
            connection.execute(transiver, SshConnection.TAKE_COMMAND);
        }
    }


    @Override
    public void onTaskCompleted(String result) {

    }

    @Override
    public void onProgressUpdate(Integer downloaded) {

    }
}
