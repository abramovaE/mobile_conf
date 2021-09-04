package com.kotofeya.mobileconfigurator;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.kotofeya.mobileconfigurator.fragments.update.UpdateContentFragment;



//911 901 18 40 Татьяна николаевна
//911 285 50 41 Татьяна Владимировна

public class UploadContentConfDialog extends DialogFragment {
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String key = getArguments().getString("key");
        String value = getArguments().getString("value");
        String ip = getArguments().getString("ip");
        boolean isStationary = getArguments().getBoolean("isStationary");
        boolean isTransport = getArguments().getBoolean("isTransport");

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
//                            ((MainActivity)getActivity()).
                    OnTaskCompleted onTaskCompleted = ((UpdateContentFragment) App.get().getFragmentHandler().getCurrentFragment());
                    ProgressBarInt progressBarInt = ((UpdateContentFragment) App.get().getFragmentHandler().getCurrentFragment());
                    SshConnection connection = new SshConnection(onTaskCompleted, progressBarInt);
                    connection.execute(ip, TaskCode.UPDATE_TRANSPORT_CONTENT_UPLOAD_CODE, filePath);

                }
                else {
                    String downloadCode = null;
                    if(isStationary){
                        downloadCode = TaskCode.UPDATE_STATION_CONTENT_DOWNLOAD_CODE + "";
                    }
                    else if(isTransport){
                        downloadCode = TaskCode.UPDATE_TRANSPORT_CONTENT_DOWNLOAD_CODE + "";
                    }
                    Downloader downloader = new Downloader((UpdateContentFragment) App.get().getFragmentHandler().getCurrentFragment());

                    if(ip != null) {
                        downloader.execute(value, ip, downloadCode);
                    }
                    else {
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
