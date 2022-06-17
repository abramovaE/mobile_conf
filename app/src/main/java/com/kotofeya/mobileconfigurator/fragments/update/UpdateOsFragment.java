package com.kotofeya.mobileconfigurator.fragments.update;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Downloader;
import com.kotofeya.mobileconfigurator.InternetConn;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

public class UpdateOsFragment extends UpdateFragment {

    private static final String TAG = UpdateOsFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding.labelVersion.setVisibility(View.VISIBLE);
        String version = getString(R.string.storage_os) + ": " + App.get().getUpdateOsFileVersion();
        binding.labelVersion.setText(version);
        return binding.getRoot();
    }

    @Override
    public void loadVersion() {
        Logger.d(TAG, new Throwable().getStackTrace()[0].getMethodName());
        boolean isInternetEnabled = InternetConn.hasInternetConnection();
        if(isInternetEnabled){
            Downloader downloader = new Downloader(this);
            downloader.execute(Downloader.OS_VERSION_URL);
        }
    }

    @Override
    public void setMainTextLabelText() {
        viewModel.setMainTxtLabel(getString(R.string.update_os_main_txt_label));
    }

    @Override
    public RvAdapterType getAdapterType() {
        return RvAdapterType.UPDATE_OS_TYPE;
    }


    public void loadUpdates() {
        Downloader downloader = new Downloader(this);
        downloader.execute(Downloader.OS_URL);
    }

    @Override
    public void onTaskCompleted(Bundle bundle) {
        super.onTaskCompleted(bundle);
        if (bundle.getInt(BundleKeys.RESULT_CODE_KEY) ==
                TaskCode.UPDATE_OS_DOWNLOAD_CODE) {
            String version = getString(R.string.storage_os) + ": " + App.get().getUpdateOsFileVersion();
            binding.labelVersion.setText(version);
        }
    }

    @Override
    public void adapterItemOnClick(Transiver transiver) {
        if (Downloader.tempUpdateOsFile.length() > 1000) {
            String ip = transiver.getIp();
            showUpdateOsConfirmDialog(ip);
        }
    }

    private void showUpdateOsConfirmDialog(String ip){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.confirmation_is_required);
        builder.setMessage(R.string.confirm_upload_update_os);
        builder.setPositiveButton(getString(R.string.upload_btn), (dialog, id) -> uploadUpdates(ip));
        builder.setNegativeButton(getString(R.string.cancel_btn), (dialog, id) -> {});
        builder.setCancelable(true);
        builder.show();
    }

    private void uploadUpdates(String ip){
        SshConnection connection = new SshConnection(
                ip,
                UpdateOsFragment.this,
                UpdateOsFragment.this);
        connection.execute(SshConnection.UPDATE_OS_UPLOAD_CODE);
    }
}