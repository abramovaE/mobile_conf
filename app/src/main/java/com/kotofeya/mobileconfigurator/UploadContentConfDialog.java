package com.kotofeya.mobileconfigurator;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.fragments.update.UpdateContentFragment;

public class UploadContentConfDialog extends DialogFragment {
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String key = requireArguments().getString(BundleKeys.KEY);
        String value = requireArguments().getString(BundleKeys.VALUE);
        String ip = requireArguments().getString(BundleKeys.IP_KEY);
        boolean isStationary = requireArguments().getBoolean(BundleKeys.IS_STATIONARY_KEY);
        boolean isTransport = requireArguments().getBoolean(BundleKeys.IS_TRANSPORT_KEY);

        Logger.d(Logger.UPDATE_CONTENT_LOG, "ip: " + ip + ", key: " + key + ", value: " + value +
                ", isStationary: " + isStationary + ", isTransport: " + isTransport);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Confirmation is required");
        builder.setMessage("Confirm the upload of " + key);
        FragmentHandler fragmentHandler = ((MainActivity)requireActivity()).getFragmentHandler();

        builder.setPositiveButton("upload", (dialog, id) -> {
            if(value.length() > 20 && value.contains("_")){
                Logger.d(Logger.UPDATE_CONTENT_LOG, "download by ssh " + ", ip: " + ip +
                        ", taskCode: " + TaskCode.UPDATE_TRANSPORT_CONTENT_DOWNLOAD_CODE + ", filepath: " + value);
                OnTaskCompleted onTaskCompleted = ((UpdateContentFragment) fragmentHandler.getCurrentFragment());
                ProgressBarInt progressBarInt = ((UpdateContentFragment) fragmentHandler.getCurrentFragment());
                SshConnection connection = new SshConnection(ip, onTaskCompleted, progressBarInt);
                connection.execute(TaskCode.UPDATE_TRANSPORT_CONTENT_UPLOAD_CODE, value);
            } else {
                String downloadCode = null;
                if(isStationary){
                    downloadCode = TaskCode.UPDATE_STATION_CONTENT_DOWNLOAD_CODE + "";
                }
                else if(isTransport){
                    downloadCode = TaskCode.UPDATE_TRANSPORT_CONTENT_DOWNLOAD_CODE + "";
                }

                Downloader downloader = new Downloader((UpdateContentFragment) fragmentHandler.getCurrentFragment());
                if(ip != null) {
                    Logger.d(Logger.CONTENT_LOG, "downloader execute 1");
                    downloader.execute(value, ip, downloadCode);
                } else {
                    Logger.d(Logger.CONTENT_LOG, "downloader execute 2");
                    downloader.execute(value, "", TaskCode.UPDATE_TRANSPORT_CONTENT_UPLOAD_TO_STORAGE_CODE + "");
                }
            }
        });
        builder.setNegativeButton("cancel", (dialog, id) -> {
        });
        builder.setCancelable(true);
        return builder.create();
    }
}
