package com.kotofeya.mobileconfigurator.fragments.update;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.ProgressBarInt;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.activities.InterfaceUpdateListener;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.fragments.scanner.ScannerFragment;
import com.kotofeya.mobileconfigurator.rv_adapter.AdapterListener;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterFactory;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.ArrayList;
import java.util.LinkedList;

public abstract class UpdateFragment extends ScannerFragment
        implements OnTaskCompleted, ProgressBarInt,
        IUpdateFragment, InterfaceUpdateListener, AdapterListener {

    private static final String TAG = UpdateFragment.class.getSimpleName();
    protected String version = "version";
    ProgressBar progressBar;

    protected static final int MOBILE_SETTINGS_RESULT = 0;

    protected AlertDialog scanClientsDialog;



    protected FragmentHandler fragmentHandler;


    protected abstract void setMainTextLabelText();
    protected abstract RvAdapterType getAdapterType();

    @Override
    public void scan(){
        utils.getTakeInfo();
    }

    @Override
    public void clientsScanFinished() {
        scanClientsDialog.dismiss();
        utils.getTakeInfo();
    }


    @Override
    public void onStart() {
        Logger.d(TAG, "onStart");
        super.onStart();
        binding.versionLabel.setVisibility(View.VISIBLE);
        binding.versionLabel.setText(version);
        binding.checkVersionBtn.setVisibility(View.VISIBLE);
        viewModel.setMainBtnRescanVisibility(View.VISIBLE);
//        scan();
    }


    private void rescan(){
        Logger.d(TAG, "rescan");
        utils.clearClients();
        utils.clearMap();
        viewModel.clearTransivers();
        scanClientsDialog = utils.getScanClientsDialog();
        scanClientsDialog.show();
        utils.updateClients(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);

        fragmentHandler = ((MainActivity) requireActivity()).getFragmentHandler();

        ImageButton mainBtnRescan = requireActivity().findViewById(R.id.main_btn_rescan);
        mainBtnRescan.setOnClickListener(v -> rescan());

        progressBar = binding.scannerProgressBar;

        binding.checkVersionBtn.setOnClickListener(v -> {
            Logger.d(TAG, "check updates button was pressed");
            boolean isInternetEnabled = utils.getInternetConnection().hasInternetConnection();
            if(isInternetEnabled){
                progressBar.setVisibility(View.VISIBLE);
                loadUpdates();
            }
            else {
                EnableMobileConfDialog dialog = new EnableMobileConfDialog();
                FragmentHandler fragmentHandler = ((MainActivity)getActivity()).getFragmentHandler();
                dialog.show(fragmentHandler.getFragmentManager(),
                        FragmentHandler.ENABLE_MOBILE_DIALOG_TAG);
            }
        });

        setMainTextLabelText();
        rvAdapter = RvAdapterFactory.getRvAdapter(getAdapterType(),
                new ArrayList<>(), this);
        binding.rvScanner.setAdapter(rvAdapter);
        if(utils.getNewBleScanner() != null) {
            utils.getNewBleScanner().stopScan();
        }
        loadVersion();

        binding.progressTv.setVisibility(View.GONE);

        viewModel.getIsGetTakeInfoFinished().observe(getViewLifecycleOwner(), this::updateScannerProgressBarTv);
        scan();
        return binding.getRoot();
    }

//    @Override
//    public void scan(){
//        utils.getTakeInfo();
//    }

    private void updateScannerProgressBarTv(Boolean aBoolean) {
        if(!aBoolean){
            binding.progressTv.setText(Utils.MESSAGE_TAKE_INFO);
            binding.progressTv.setVisibility(View.VISIBLE);
        } else {
            binding.progressTv.setVisibility(View.GONE);
        }
    }

    @Override
    public void onTaskCompleted(Bundle result) {
        Logger.d(TAG, "on task completed");

        int resultCode = result.getInt(BundleKeys.RESULT_CODE_KEY);
        String resultStr = result.getString(BundleKeys.RESULT_KEY);
        String ipStr = result.getString(BundleKeys.IP_KEY);
        Logger.d(TAG, "ssh task completed: ip: " + ipStr + ", resultCode: " + resultCode);

        switch (resultCode){
            case 1002:
                fragmentHandler.showMessage("Error: " + result);
//                MessageDialog.showMessage(fr, "Error: " + result);
//                 utils.showMessage("Error: " + result);
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
                Logger.d(TAG, "transportContent: " + result);
                break;
            case TaskCode.STATION_CONTENT_VERSION_CODE:
                Logger.d(TAG, "stationContent: " + result);
                break;
            case TaskCode.UPDATE_OS_DOWNLOAD_CODE:
                Logger.d(TAG, "downloaded: " + result);
                progressBar.setVisibility(View.GONE);
                fragmentHandler.showMessage(getString(R.string.downloaded));
//                utils.showMessage(getString(R.string.downloaded));
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
                    Logger.d(TAG, "result: " + result + ", remove client: " + ipStr);
                    utils.removeClient(ipStr);
                } else {
                    Logger.d(TAG, "ssh error: " + result);
                    fragmentHandler.showMessage("Error: " + result);
//                    utils.showMessage("Error: " + result);
                }
                break;
            case TaskCode.DOWNLOADER_ERROR_CODE:
                Logger.d(TAG, "downloader error: " + result);
                fragmentHandler.showMessage("Error: " + result);
//                utils.showMessage("Error: " + result);
                progressBar.setVisibility(View.GONE);
                break;
            case TaskCode.UPDATE_TRANSPORT_CONTENT_UPLOAD_TO_STORAGE_CODE:
                    String tempFilePath = result.getString(BundleKeys.FILE_PATH_KEY);
                    Logger.d(TAG, "downloading completed, tfp: " + tempFilePath);
                    App.get().saveUpdateContentFilePaths(tempFilePath);
                    binding.downloadCoreUpdateFilesTv.setVisibility(View.VISIBLE);
                    updateFilesTv();
        }
    }

    private void updateFilesTv(){
        StringBuilder sb = new StringBuilder();
        sb.append("Сохраненные файлы: \n");
        new LinkedList<>(App.get().getUpdateContentFilePaths())
                .forEach(it -> sb.append(it.substring(it.lastIndexOf("/") + 1, it.indexOf("_"))).append("\n"));
        binding.downloadCoreUpdateFilesTv.setText(sb.toString());
    }

    @Override
    public void onProgressUpdate(Integer downloaded) {
        progressBar.setProgress(downloaded);
    }

    @Override
    public void setProgressBarVisible() {
        getActivity().runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
    }

    @Override
    public void setProgressBarGone(){
        requireActivity().runOnUiThread(() -> progressBar.setVisibility(View.GONE));
    }

    @Override
    public void clearProgressBar(){
        requireActivity().runOnUiThread(() -> progressBar.setProgress(0));
    }

    private void uploaded(String ip){
        Logger.d(TAG, "uploaded: " + ip);
        Transiver transiver = viewModel.getTransiverByIp(ip);
        viewModel.removeTransiver(transiver);
        rvAdapter.notifyDataSetChanged();
        fragmentHandler.showMessage(getString(R.string.uploaded));
//        utils.showMessage(getString(R.string.uploaded));
        progressBar.setVisibility(View.GONE);
    }

    private void downloadBySsh(String ip, int taskCode, Bundle bundle, int progressBarVisibility){
        String filePath = bundle.getString(BundleKeys.FILE_PATH_KEY);
        Logger.d(TAG, "download by ssh " + ", ip: " + ip + ", taskCode: " + taskCode + ", filepath: " + filePath);
        progressBar.setVisibility(progressBarVisibility);
        SshConnection connection = new SshConnection(this, this);
        connection.execute(ip, taskCode, filePath);
    }

    private void setVersion(String version){
        Logger.d(TAG, "set version: " + version);
        this.version = version;
        binding.versionLabel.setText(version);
    }

    public static class EnableMobileConfDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Logger.d(TAG, "show enable mobile config dialog");
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(R.string.mobile_internet_title);
            builder.setMessage(R.string.mobile_internet_message);
            builder.setPositiveButton(getString(R.string.ok), (dialog, id) -> {
                Logger.d(TAG, "ok btn was pressed, show settings");
                startActivityForResult(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS), MOBILE_SETTINGS_RESULT);
            });
            builder.setNegativeButton(getString(R.string.cancel), (dialog, id) ->
                    Logger.d(TAG, "cancel btn was pressed, keep working without mobile internet"));
            builder.setCancelable(true);
            return builder.create();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MOBILE_SETTINGS_RESULT) {
            loadVersion();
        }
    }
}