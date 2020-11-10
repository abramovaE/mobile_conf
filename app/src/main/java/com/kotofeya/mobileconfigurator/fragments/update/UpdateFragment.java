package com.kotofeya.mobileconfigurator.fragments.update;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Downloader;
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
import java.util.Map;


public abstract class UpdateFragment extends Fragment implements OnTaskCompleted {

    public Context context;
    public Utils utils;
    public Button mainBtnRescan;

    private final Handler myHandler = new Handler();

    ListView lvScanner;
    ScannerAdapter scannerAdapter;

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
        versionLabel = view.findViewById(R.id.scanner_label0);
        checkVersionButton = view.findViewById(R.id.scanner_btn);
        mainTxtLabel = ((MainActivity)context).findViewById(R.id.main_txt_label);
        mainBtnRescan = ((MainActivity)context).findViewById(R.id.main_btn_rescan);

        mainBtnRescan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                utils.clearClients();
                utils.clearMap();
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
        loadVersion();

        if(utils.getTransivers().isEmpty()) {
            scan();
        }
        return view;
    }

    @Override
    public void onResume() {
        Logger.d(Logger.UPDATE_OS_LOG, "onResume");
        super.onResume();
    }


    protected void scan(){
        utils.getTakeInfo(this);
    }


    @Override
    public void onTaskCompleted(Bundle bundle) {
        int resultCode = bundle.getInt("resultCode");
        String result = bundle.getString("result");
        String ip = bundle.getString("ip");
        Logger.d(Logger.UPDATE_LOG, "ip: " + ip + ", resultCode: " + resultCode);
        StringBuilder sbt = new StringBuilder();
        StringBuilder sbs = new StringBuilder();

        switch (resultCode){
            case TaskCode.TAKE_CODE:
                utils.addTakeInfo(result, true);
                myHandler.post(updateRunnable);
                break;

            case TaskCode.UPDATE_OS_UPLOAD_CODE:
            case TaskCode.UPDATE_STM_UPLOAD_CODE:
            case TaskCode.UPDATE_TRANSPORT_CONTENT_UPLOAD_CODE:
                uploaded(ip);
                break;

            case TaskCode.UPDATE_OS_VERSION_CODE:
            case TaskCode.UPDATE_STM_VERSION_CODE:
                setVersion(result);
                break;

            case TaskCode.TRANSPORT_CONTENT_VERSION_CODE:
                Logger.d(Logger.UPDATE_LOG, "transportContent: " + result);
                break;

            case TaskCode.STATION_CONTENT_VERSION_CODE:
                Logger.d(Logger.UPDATE_LOG, "stationContent: " + result);
                break;

            case TaskCode.UPDATE_OS_DOWNLOAD_CODE:
                progressBar.setVisibility(View.GONE);
                utils.showMessage("Downloaded");
                break;

            case TaskCode.UPDATE_STM_DOWNLOAD_CODE:
                downloadBySsh(ip, SshConnection.UPDATE_STM_UPLOAD_CODE, bundle, View.GONE);
                break;

            case TaskCode.UPDATE_TRANSPORT_CONTENT_DOWNLOAD_CODE:
                downloadBySsh(ip, SshConnection.UPDATE_TRANSPORT_CONTENT_UPLOAD_CODE, bundle, View.VISIBLE);
                break;
            case TaskCode.UPDATE_STATION_CONTENT_DOWNLOAD_CODE:
                downloadBySsh(ip, SshConnection.UPDATE_STATION_CONTENT_UPLOAD_CODE, bundle, View.VISIBLE);
                break;
            case TaskCode.SSH_ERROR_CODE:
                progressBar.setVisibility(View.GONE);
                if(result.contains("Connection refused")){
                    utils.removeClient(ip);
                }
                else {utils.showMessage("Error: " + result);}
                break;

            case TaskCode.DOWNLOADER_ERROR_CODE:
                utils.showMessage("Error: " + result);
                progressBar.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onProgressUpdate(Integer downloaded) {
        Logger.d(Logger.UPDATE_LOG, "onProgressUpdate: " + downloaded);
        progressBar.setProgress(downloaded);
    }

    abstract void loadUpdates();
    abstract void loadVersion();
    abstract void setMainTextLabelText();
    abstract ScannerAdapter getScannerAdapter();

    private void updateUI()
    {
        scannerAdapter.notifyDataSetChanged();
    }

    final Runnable updateRunnable = new Runnable() {
        public void run() {
            updateUI();
        }
    };

    private void uploaded(String ip){
        Transiver transiver = utils.getTransiverByIp(ip);
        utils.removeTransiver(transiver);
        scannerAdapter.notifyDataSetChanged();
        utils.showMessage("Uploaded");
        progressBar.setVisibility(View.GONE);
    }

    private void downloadBySsh(String ip, int taskCode, Bundle bundle, int progressBarVisibility){
        String filePath = bundle.getString("filePath");
        Logger.d(Logger.UPDATE_CONTENT_LOG, "ip: " + ip + ", filepath: " + filePath);
        progressBar.setVisibility(progressBarVisibility);
        SshConnection connection = new SshConnection(this);
        connection.execute(ip, taskCode, filePath);
    }

    private void setVersion(String version){
        this.version = version;
        versionLabel.setText(version);
    }
}
