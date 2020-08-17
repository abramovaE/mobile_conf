package com.kotofeya.mobileconfigurator.fragments.update;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.transivers.Transiver;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.WiFiLocalHotspot;

import java.util.List;


public abstract class UpdateFragment extends Fragment implements OnTaskCompleted {

    protected Context context;
    protected Utils utils;
    ListView lvScanner;
    ScannerAdapter scannerAdapter;
    Button mainBtnRescan;

    TextView versionLabel;
    Button checkVersionButton;

    String version = "version";
    TextView mainTxtLabel;

    ProgressBar progressBar;


    @Override
    public void onAttach(Context context) {
        this.context = context;
        this.utils = ((MainActivity) context).getUtils();
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        Logger.d(Logger.UPDATE_OS_LOG, "onStart");
        super.onStart();
        versionLabel.setVisibility(View.VISIBLE);
        versionLabel.setText(version);
        checkVersionButton.setVisibility(View.VISIBLE);
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
        versionLabel = view.findViewById(R.id.scanner_label);
        checkVersionButton = view.findViewById(R.id.scanner_btn);
        mainTxtLabel = ((MainActivity)context).findViewById(R.id.main_txt_label);
        mainBtnRescan = ((MainActivity)context).findViewById(R.id.main_btn_rescan);

        mainBtnRescan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                utils.clearTransivers();
                scannerAdapter.notifyDataSetChanged();
                scan();
            }
        });


        progressBar = view.findViewById(R.id.scanner_progressBar);
        checkVersionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.d(Logger.UPDATE_OS_LOG, "check updates button was pressed");
                progressBar.setVisibility(View.VISIBLE);
                loadUpdates();
            }
        });

        setMainTextLabelText();
        scannerAdapter = getScannerAdapter();
        lvScanner.setAdapter(scannerAdapter);
        utils.getBluetooth().stopScan(true);
        utils.clearTransivers();
        scannerAdapter.notifyDataSetChanged();
        loadVersion();
        scan();
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
//            Transiver transiver = new Transiver(s);
//            utils.addSshTransiver(transiver);
            SshConnection connection = new SshConnection(this);
            connection.execute(s, SshConnection.TAKE_COMMAND);
        }
    }


    @Override
    public void onTaskCompleted(Bundle bundle) {
        if (bundle != null && bundle.containsKey("result")){
            String result = bundle.getString("result");
            Logger.d(Logger.UPDATE_OS_LOG, "result: " + result);

            if(result.contains("Release")){
                version = result;
                versionLabel.setText(result);
            }

            else if(result.contains("Downloaded")){
                Transiver transiver = utils.getTransiverByIp(bundle.getString("ip"));
                utils.removeTransiver(transiver);
                scannerAdapter.notifyDataSetChanged();
                Toast.makeText(context, "Downloaded", Toast.LENGTH_SHORT).show();
            }

            else if(result.contains("stm downloaded")){
                Logger.d(Logger.UPDATE_OS_LOG, "ip: " + bundle.getString("ip") + ", filepath: " + bundle.getString("filePath"));
                SshConnection connection = new SshConnection(this);
                String filePath = bundle.getString("filePath");
                connection.execute(bundle.getString("ip"), SshConnection.UPDATE_STM_LOAD_FILE_COMMAND, filePath);
            }

            else {
                Logger.d(Logger.UPDATE_OS_LOG, "notifyDataSetChanged, transivers: " + utils.getTransivers().size());
                if (result.split("\n").length > 10) {
                    utils.addTakeInfo(result, true);
                }
                scannerAdapter.notifyDataSetChanged();
            }

        }
        else {
            Logger.d(Logger.UPDATE_OS_LOG, "notifyDataSetChanged, transivers: " + utils.getTransivers().size());
            scannerAdapter.notifyDataSetChanged();
        }
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onProgressUpdate(Integer downloaded) {
        progressBar.setProgress(downloaded);
    }

    abstract void loadUpdates();
    abstract void loadVersion();
    abstract void setMainTextLabelText();
    abstract ScannerAdapter getScannerAdapter();


}
