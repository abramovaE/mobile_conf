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

public class UpdateOsFragment extends Fragment implements OnTaskCompleted {

    private Context context;
    private Utils utils;
    ScannerAdapter scannerAdapter;
    Button mainBtnRescan;

    TextView scannerLabel;
    Button scannerButton;

    Downloader downloader;

    @Override
    public void onAttach(Context context) {
        this.context = context;
        this.utils = ((MainMenu) context).getUtils();
        super.onAttach(context);
    }


    @Override
    public void onStart() {
        super.onStart();
        scannerLabel.setVisibility(View.VISIBLE);
        scannerLabel.setText("version");

        scannerButton.setVisibility(View.VISIBLE);
        mainBtnRescan.setVisibility(View.VISIBLE);
        utils.getBluetooth().stopScan(true);
        utils.clearTransivers();
        scannerAdapter.notifyDataSetChanged();
        loadUpdates();
        scan();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.scanner_fragment, container, false);
        ListView lvScanner = view.findViewById(R.id.lv_scanner);
        scannerLabel = view.findViewById(R.id.scanner_label);
        scannerButton = view.findViewById(R.id.scanner_btn);


        TextView mainTxtLabel = ((MainMenu)context).findViewById(R.id.main_txt_label);
        mainTxtLabel.setText(R.string.update_os_main_txt_label);
        mainBtnRescan = ((MainMenu)context).findViewById(R.id.main_btn_rescan);

        mainBtnRescan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                utils.clearTransivers();
                scannerAdapter.notifyDataSetChanged();
                scan();
            }
        });

        scannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.d(Logger.UPDATE_OS_LOG, "check updates button was pressed");
                scannerLabel.setText("");
                loadUpdates();
            }
        });

        scannerAdapter = new ScannerAdapter(context, utils, ScannerAdapter.UPDATE_OS_TYPE);
        lvScanner.setAdapter(scannerAdapter);
        return view;
    }


    @Override
    public void onTaskCompleted(String result) {
        Logger.d(Logger.UPDATE_OS_LOG, "onTaskCompleted");


        if(result.contains("Release OS: ")){
            scannerLabel.setText(result);
        }

        else if(result.contains("updateos:")){
            Transiver transiver = utils.getCurrentTransiver();
            utils.removeTransiver(transiver);
            utils.setCurrentTransiver(null);
            scannerAdapter.notifyDataSetChanged();
        }

        else {
            scannerAdapter.notifyDataSetChanged();
        }


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

    private void loadUpdates(){
        downloader = new Downloader(this);
        downloader.execute();
    }

}
