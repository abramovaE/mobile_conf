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

public class UpdateStmFragment extends Fragment implements SshCompleted {

    private Context context;
    private Utils utils;
    private Transiver currentTransiver;

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
        TextView scannerLabel = view.findViewById(R.id.scanner_label);
        Button scannerButton = view.findViewById(R.id.scanner_btn);


        TextView mainTxtLabel = ((MainMenu)context).findViewById(R.id.main_txt_label);
        mainTxtLabel.setText(R.string.update_stm_main_txt_label);
        Button mainBtnRescan = ((MainMenu)context).findViewById(R.id.main_btn_rescan);

        // TODO: 18.07.20  settext , buttontext and listener
//        scannerLabel.setText("");

//        scannerButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });


        List<String> clients = WiFiLocalHotspot.getInstance().getClientList();
        for(String s: clients){
            Transiver transiver = new Transiver(s);
            utils.getTransivers().add(transiver);
            SshConnection connection = new SshConnection(this);
            currentTransiver = transiver;
            connection.execute(s, SshConnection.UPTIME_COMMAND);
        }
        ScannerAdapter scannerAdapter = new ScannerAdapter(context, utils.getTransivers(), ScannerAdapter.UPDATE_STM_TYPE);
        lvScanner.setAdapter(scannerAdapter);
        return view;
    }


    @Override
    public void onTaskCompleted(String result) {
        Logger.d(Logger.BASIC_SCANNER_LOG, "ssh connection for: " + currentTransiver.getIp() + " completed");
        currentTransiver.setBasicScanInfo(result);
        currentTransiver = null;
    }

}
