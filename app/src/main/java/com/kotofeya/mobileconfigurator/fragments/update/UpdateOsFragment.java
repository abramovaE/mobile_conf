package com.kotofeya.mobileconfigurator.fragments.update;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Downloader;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.TaskCode;

public class UpdateOsFragment extends UpdateFragment {

    TextView storageVersionTxt;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        storageVersionTxt = view.findViewById(R.id.scanner_label1);
        storageVersionTxt.setVisibility(View.VISIBLE);
        storageVersionTxt.setText("Storage OS: " + App.get().getUpdateOsFileVersion());
        return view;
    }

    @Override
    void loadVersion() {
        boolean isInternetEnabled = utils.getInternetConnection().hasInternetConnection();
        if(isInternetEnabled){
            Downloader downloader = new Downloader(this);
            downloader.execute(Downloader.OS_VERSION_URL);
        } else {
//            EnableMobileConfDialog dialog = new EnableMobileConfDialog();
//            dialog.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().ENABLE_MOBILE_DIALOG_TAG);
        }
    }




    @Override
    void setMainTextLabelText() {
        mainTxtLabel.setText(R.string.update_os_main_txt_label);
    }

    @Override
    ScannerAdapter getScannerAdapter() {
        return new ScannerAdapter(context, utils, ScannerAdapter.UPDATE_OS_TYPE);
    }



    void loadUpdates() {
        Downloader downloader = new Downloader(this);
        downloader.execute(Downloader.OS_URL);
    }

    @Override
    public void onTaskCompleted(Bundle bundle) {
        super.onTaskCompleted(bundle);
        if (bundle.getInt("resultCode") == TaskCode.UPDATE_OS_DOWNLOAD_CODE) {
            storageVersionTxt.setText("Storage OS: " + App.get().getUpdateOsFileVersion());
        }
    }


    public static class UpdateOsConfDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String ip = getArguments().getString("ip");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Confirmation is required");
            builder.setMessage("Confirm the upload of the updates");
            builder.setPositiveButton("upload", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    View view = App.get().getFragmentHandler().getCurrentFragment().getView();
                    ProgressBar progressBar = view.findViewById(R.id.scanner_progressBar);
                    progressBar.setVisibility(View.VISIBLE);
                    SshConnection connection = new SshConnection(((UpdateOsFragment) App.get().getFragmentHandler().getCurrentFragment()));
                    connection.execute(ip, SshConnection.UPDATE_OS_UPLOAD_CODE);
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
}