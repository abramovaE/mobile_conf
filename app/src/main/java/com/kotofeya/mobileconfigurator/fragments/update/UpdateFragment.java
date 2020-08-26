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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.transivers.Transiver;
import com.kotofeya.mobileconfigurator.WiFiLocalHotspot;

import java.util.List;


public abstract class UpdateFragment extends Fragment implements OnTaskCompleted {

    public Context context;
    public Utils utils;
    public Button mainBtnRescan;


    @Override
    public void onAttach(Context context) {
        this.context = context;
        this.utils = ((MainActivity) context).getUtils();
        super.onAttach(context);

    }


    ListView lvScanner;
    ScannerAdapter scannerAdapter;

    TextView versionLabel;
    Button checkVersionButton;

    String version = "version";
    TextView mainTxtLabel;

    ProgressBar progressBar;

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
        versionLabel = view.findViewById(R.id.scanner_label0);
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
            SshConnection connection = new SshConnection(this);
            connection.execute(s, SshConnection.TAKE_CODE);
        }
    }


    @Override
    public void onTaskCompleted(Bundle bundle) {
        int resultCode = bundle.getInt("resultCode");
        String result = bundle.getString("result");
        String ip = bundle.getString("ip");

        Logger.d(Logger.UPDATE_OS_LOG, "resultCode: " + resultCode);
        Logger.d(Logger.UPDATE_OS_LOG, "ip: " + ip);

        if(resultCode == TaskCode.TAKE_CODE){
            utils.addTakeInfo(result, true);
            scannerAdapter.notifyDataSetChanged();
        }

        else if(resultCode == TaskCode.UPDATE_OS_UPLOAD_CODE){
            Transiver transiver = utils.getTransiverByIp(ip);
            utils.removeTransiver(transiver);
            scannerAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
            utils.showMessage("Uploaded");

        }

        else if(resultCode == TaskCode.UPDATE_OS_VERSION_CODE){
            version = result;
            versionLabel.setText(result);
        }

        else if(resultCode == TaskCode.UPDATE_OS_DOWNLOAD_CODE){
            progressBar.setVisibility(View.GONE);
            utils.showMessage("Downloaded");
        }

        else if(resultCode == TaskCode.UPDATE_STM_VERSION_CODE){
            version = result;
            versionLabel.setText(result);
        }

        else if(resultCode == TaskCode.UPDATE_STM_DOWNLOAD_CODE){
            Logger.d(Logger.UPDATE_OS_LOG, "ip: " + ip + ", filepath: " + bundle.getString("filePath"));
            SshConnection connection = new SshConnection(this);
            String filePath = bundle.getString("filePath");
            connection.execute(ip, SshConnection.UPDATE_STM_UPLOAD_CODE, filePath);
            progressBar.setVisibility(View.GONE);
        }

        else if(resultCode == TaskCode.UPDATE_STM_UPLOAD_CODE){
            Transiver transiver = utils.getTransiverByIp(ip);
            utils.removeTransiver(transiver);
            scannerAdapter.notifyDataSetChanged();
            utils.showMessage("Uploaded");
            progressBar.setVisibility(View.GONE);
        }

        else if(resultCode == TaskCode.SSH_ERROR_CODE || resultCode == TaskCode.DOWNLOADER_ERROR_CODE){
            utils.showMessage("Error: " + result);
            progressBar.setVisibility(View.GONE);
        }
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
