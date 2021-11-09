package com.kotofeya.mobileconfigurator;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.kotofeya.mobileconfigurator.fragments.update.UpdateContentFragment;

public class UploadContentConfDialog extends DialogFragment {
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String key = getArguments().getString(BundleKeys.KEY);
        String value = getArguments().getString(BundleKeys.VALUE);
        String ip = getArguments().getString(BundleKeys.IP_KEY);
        boolean isStationary = getArguments().getBoolean(BundleKeys.IS_STATIONARY_KEY);
        boolean isTransport = getArguments().getBoolean(BundleKeys.IS_TRANSPORT_KEY);

        Logger.d(Logger.UPDATE_CONTENT_LOG, "ip: " + ip + ", key: " + key + ", value: " + value +
                ", isStationary: " + isStationary + ", isTransport: " + isTransport);
        AlertDialog.Builder builder = new AlertDialog.Builder((App.get().getFragmentHandler().getCurrentFragment()).getActivity());
        builder.setTitle("Confirmation is required");
        builder.setMessage("Confirm the upload of " + key);
        builder.setPositiveButton("upload", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(value.length() > 20 && value.contains("_")){
                    String filePath = value;
                    Logger.d(Logger.UPDATE_CONTENT_LOG, "download by ssh " + ", ip: " + ip + ", taskCode: " + TaskCode.UPDATE_TRANSPORT_CONTENT_DOWNLOAD_CODE + ", filepath: " + filePath);
                    OnTaskCompleted onTaskCompleted = ((UpdateContentFragment) App.get().getFragmentHandler().getCurrentFragment());
                    ProgressBarInt progressBarInt = ((UpdateContentFragment) App.get().getFragmentHandler().getCurrentFragment());
                    SshConnection connection = new SshConnection(onTaskCompleted, progressBarInt);
                    connection.execute(ip, TaskCode.UPDATE_TRANSPORT_CONTENT_UPLOAD_CODE, filePath);
                } else {
                    String downloadCode = null;
                    if(isStationary){
                        downloadCode = TaskCode.UPDATE_STATION_CONTENT_DOWNLOAD_CODE + "";
                    }
                    else if(isTransport){
                        downloadCode = TaskCode.UPDATE_TRANSPORT_CONTENT_DOWNLOAD_CODE + "";
                    }
                    Downloader downloader = new Downloader((UpdateContentFragment) App.get().getFragmentHandler().getCurrentFragment());
                    if(ip != null) {
                        Logger.d(Logger.CONTENT_LOG, "downloader execute 1");
                        downloader.execute(value, ip, downloadCode);
                    } else {
                        Logger.d(Logger.CONTENT_LOG, "downloader execute 2");
                        downloader.execute(value, "", TaskCode.UPDATE_TRANSPORT_CONTENT_UPLOAD_TO_STORAGE_CODE + "");
                    }
                }
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.setCancelable(true);
        return builder.create();
    }
}
