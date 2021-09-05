package com.kotofeya.mobileconfigurator.fragments.update;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.ProgressBarInt;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapter;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.activities.CustomViewModel;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterFactory;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public abstract class UpdateFragment extends Fragment implements OnTaskCompleted, ProgressBarInt {

    public Context context;
    public Utils utils;
    public ImageButton mainBtnRescan;
    TextView versionLabel;
    Button checkVersionButton;
    String version = "version";
    TextView mainTxtLabel;
    ProgressBar progressBar;
    protected static final int MOBILE_SETTINGS_RESULT = 0;
    protected CustomViewModel viewModel;

    protected TextView progressTv;


    RecyclerView rvScanner;
    RvAdapter rvAdapter;
    TextView downloadContentUpdateFilesTv;

    protected abstract void loadUpdates();
    protected abstract void loadVersion();
    protected abstract void setMainTextLabelText();
    protected abstract RvAdapterType getAdapterType();

    @Override
    public void onAttach(Context context) {
        this.context = context;
        this.utils = ((MainActivity) context).getUtils();
        this.viewModel = ((MainActivity)context).getViewModel();
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
        Logger.d(Logger.UPDATE_LOG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.scanner_fragment_cl, container, false);
        rvScanner = view.findViewById(R.id.rv_scanner);
        versionLabel = view.findViewById(R.id.scanner_label0);
        checkVersionButton = view.findViewById(R.id.scanner_btn);
        mainTxtLabel = ((MainActivity)context).findViewById(R.id.main_txt_label);
        mainBtnRescan = ((MainActivity)context).findViewById(R.id.main_btn_rescan);
        mainBtnRescan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                utils.clearClients();
                utils.clearMap();
                viewModel.clearTransivers();
                scan();
            }
        });
        progressBar = view.findViewById(R.id.scanner_progressBar);
        checkVersionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.d(Logger.UPDATE_OS_LOG, "check updates button was pressed");
                boolean isInternetEnabled = utils.getInternetConnection().hasInternetConnection();
                if(isInternetEnabled){
                    progressBar.setVisibility(View.VISIBLE);
                    loadUpdates();
                }
                else {
                    EnableMobileConfDialog dialog = new EnableMobileConfDialog();
                    dialog.show(App.get().getFragmentHandler().getFragmentManager(),
                            App.get().getFragmentHandler().ENABLE_MOBILE_DIALOG_TAG);
                }
            }
        });

        setMainTextLabelText();
        rvAdapter = RvAdapterFactory.getRvAdapter(context, utils, getAdapterType(), new ArrayList<>());
        rvScanner.setAdapter(rvAdapter);
        if(utils.getNewBleScanner() != null) {
            utils.getNewBleScanner().stopScan();
        }
        loadVersion();
        progressTv = view.findViewById(R.id.progressTv);
        downloadContentUpdateFilesTv = view.findViewById(R.id.downloadCoreUpdateFilesTv);
        return view;
    }

    @Override
    public void onResume() {
        Logger.d(Logger.UPDATE_OS_LOG, "onResume");
        super.onResume();
    }

    protected void scan(){
        Logger.d(Logger.UPDATE_LOG, "updateFragment scan");
        utils.getTakeInfo();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity(), new CustomViewModel.ModelFactory()).get(CustomViewModel.class);
        viewModel.getTransivers().observe(getViewLifecycleOwner(), this::updateUI);
        scan();
    }

    protected void updateUI(List<Transiver> transivers){
        rvAdapter.setObjects(transivers);
        rvAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTaskCompleted(Bundle result) {
        Logger.d(Logger.UPDATE_LOG, "on task completed");
        String command = result.getString(BundleKeys.COMMAND_KEY);
        String ip = result.getString(BundleKeys.IP_KEY);
        String response = result.getString(BundleKeys.RESPONSE_KEY);
        Logger.d(Logger.UPDATE_LOG, "command: " + command);
        Logger.d(Logger.UPDATE_LOG, "ip: " + ip);
        Logger.d(Logger.UPDATE_LOG, "response: " + response);


        int resultCode = result.getInt(BundleKeys.RESULT_CODE_KEY);
        String resultStr = result.getString(BundleKeys.RESULT_KEY);
        String ipStr = result.getString(BundleKeys.IP_KEY);
        Logger.d(Logger.UPDATE_LOG, "ssh task completed: ip: " + ipStr + ", resultCode: " + resultCode);

        switch (resultCode){

            case 1002:
                 utils.showMessage("Error: " + result);
                 break;
            case TaskCode.UPDATE_OS_UPLOAD_CODE:
            case TaskCode.UPDATE_STM_UPLOAD_CODE:
            case TaskCode.UPDATE_TRANSPORT_CONTENT_UPLOAD_CODE:
                uploaded(ipStr);
                break;
            case TaskCode.UPDATE_OS_VERSION_CODE:
            case TaskCode.UPDATE_STM_VERSION_CODE:
                setVersion(resultStr);
                break;
            case TaskCode.TRANSPORT_CONTENT_VERSION_CODE:
                Logger.d(Logger.UPDATE_LOG, "transportContent: " + result);
                break;
            case TaskCode.STATION_CONTENT_VERSION_CODE:
                Logger.d(Logger.UPDATE_LOG, "stationContent: " + result);
                break;
            case TaskCode.UPDATE_OS_DOWNLOAD_CODE:
                Logger.d(Logger.UPDATE_LOG, "downloaded: " + result);
                progressBar.setVisibility(View.GONE);
                utils.showMessage(getString(R.string.downloaded));
                break;
            case TaskCode.UPDATE_STM_DOWNLOAD_CODE:
                downloadBySsh(ipStr, SshConnection.UPDATE_STM_UPLOAD_CODE, result, View.GONE);
                break;
            case TaskCode.UPDATE_TRANSPORT_CONTENT_DOWNLOAD_CODE:
                downloadBySsh(ipStr, SshConnection.UPDATE_TRANSPORT_CONTENT_UPLOAD_CODE, result, View.VISIBLE);
                break;
            case TaskCode.UPDATE_STATION_CONTENT_DOWNLOAD_CODE:
                downloadBySsh(ipStr, SshConnection.UPDATE_STATION_CONTENT_UPLOAD_CODE, result, View.VISIBLE);
                break;
            case TaskCode.SSH_ERROR_CODE:
                progressBar.setVisibility(View.GONE);
                if(resultStr.contains("Connection refused") || resultStr.contains("Auth fail")){
                    Logger.d(Logger.UPDATE_LOG, "result: " + result + ", remove client: " + ipStr);
                    utils.removeClient(ipStr);
                } else {
                    Logger.d(Logger.UPDATE_LOG, "ssh error: " + result);
                    utils.showMessage("Error: " + result);}
                break;

            case TaskCode.DOWNLOADER_ERROR_CODE:
                Logger.d(Logger.UPDATE_LOG, "downloader error: " + result);
                utils.showMessage("Error: " + result);
                progressBar.setVisibility(View.GONE);
                break;

                case TaskCode.UPDATE_TRANSPORT_CONTENT_UPLOAD_TO_STORAGE_CODE:
                    String tempFilePath = result.getString(BundleKeys.FILE_PATH_KEY);
                    Logger.d(Logger.UPDATE_LOG, "downloading completed, tfp: " + tempFilePath);
                    App.get().saveUpdateContentFilePaths(tempFilePath);
                    downloadContentUpdateFilesTv.setVisibility(View.VISIBLE);
                    updateFilesTv();
        }
    }



    private void updateFilesTv(){
        StringBuilder sb = new StringBuilder();
        sb.append("Сохраненные файлы: \n");
        new LinkedList<>(App.get().getUpdateContentFilePaths()).stream()
                .forEach(it -> sb.append(it.substring(it.lastIndexOf("/") + 1, it.indexOf("_"))).append("\n"));
        downloadContentUpdateFilesTv.setText(sb.toString());
    }

    @Override
    public void onProgressUpdate(Integer downloaded) {
        progressBar.setProgress(downloaded);
    }

    @Override
    public void setProgressBarVisible() {
        Logger.d(Logger.UPDATE_LOG, "set progressbar visible");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void setProgressBarGone(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void clearProgressBar(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(0);
            }
        });
    }

    @Override
    public void clearTextLabel(){
    }

    private void uploaded(String ip){
        Logger.d(Logger.UPDATE_LOG, "uploaded: " + ip);
        Transiver transiver = viewModel.getTransiverByIp(ip);
        viewModel.removeTransiver(transiver);
        rvAdapter.notifyDataSetChanged();
        utils.showMessage(getString(R.string.uploaded));
        progressBar.setVisibility(View.GONE);
    }

    private void downloadBySsh(String ip, int taskCode, Bundle bundle, int progressBarVisibility){
        String filePath = bundle.getString(BundleKeys.FILE_PATH_KEY);
        Logger.d(Logger.UPDATE_CONTENT_LOG, "download by ssh " + ", ip: " + ip + ", taskCode: " + taskCode + ", filepath: " + filePath);
        progressBar.setVisibility(progressBarVisibility);
        SshConnection connection = new SshConnection(this, this);
        connection.execute(ip, taskCode, filePath);
    }

    private void setVersion(String version){
        Logger.d(Logger.UPDATE_LOG, "setv version: " + version);
        this.version = version;
        versionLabel.setText(version);
    }



    public static class EnableMobileConfDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Logger.d(Logger.UPDATE_LOG, "show enable mobile config dialog");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.mobile_internet_title);
            builder.setMessage(R.string.mobile_internet_message);
            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Logger.d(Logger.UPDATE_LOG, "ok btn was pressed, show settings");
                    startActivityForResult(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS), MOBILE_SETTINGS_RESULT);
                }
            });
            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Logger.d(Logger.UPDATE_LOG, "cancel btn was pressed, keep working without mobile internet");

                }
            });
            builder.setCancelable(true);
            return builder.create();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case MOBILE_SETTINGS_RESULT:
                loadVersion();
                break;
        }
    }
}