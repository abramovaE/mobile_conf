package com.kotofeya.mobileconfigurator.fragments.update;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Downloader;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;

public class UpdateOsFragment extends UpdateFragment {

    TextView storageVersionTxt;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        storageVersionTxt = view.findViewById(R.id.scanner_label1);
        storageVersionTxt.setVisibility(View.VISIBLE);
        storageVersionTxt.setText(getString(R.string.storage_os) + ": " + App.get().getUpdateOsFileVersion());
        return view;
    }

    @Override
    public void loadVersion() {
        Logger.d(Logger.UPDATE_OS_LOG, "load version");
        boolean isInternetEnabled = utils.getInternetConnection().hasInternetConnection();
        if(isInternetEnabled){
            Downloader downloader = new Downloader(this);
            downloader.execute(Downloader.OS_VERSION_URL);
        }
    }

    @Override
    protected void setMainTextLabelText() {
        mainTxtLabel.setText(R.string.update_os_main_txt_label);
    }

    @Override
    protected RvAdapterType getAdapterType() {
        return RvAdapterType.UPDATE_OS_TYPE;
    }


    public void loadUpdates() {
        Downloader downloader = new Downloader(this);
        downloader.execute(Downloader.OS_URL);
    }

    @Override
    public void onTaskCompleted(Bundle bundle) {
        super.onTaskCompleted(bundle);
        if (bundle.getInt(BundleKeys.RESULT_CODE_KEY) == TaskCode.UPDATE_OS_DOWNLOAD_CODE) {
            storageVersionTxt.setText(getString(R.string.storage_os) + ": " + App.get().getUpdateOsFileVersion());
        }
    }

    public static class UpdateOsConfDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String ip = getArguments().getString(BundleKeys.IP_KEY);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.confirmation_is_required);
            builder.setMessage("Confirm the upload of the updates");
            builder.setPositiveButton("upload", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    SshConnection connection = new SshConnection(
                            ((UpdateOsFragment) App.get().getFragmentHandler().getCurrentFragment()),
                            ((UpdateOsFragment) App.get().getFragmentHandler().getCurrentFragment()));
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

    public static class UpdatePhpConfDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String ip = getArguments().getString(BundleKeys.IP_KEY);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.confirmation_is_required);
            builder.setMessage("Confirm the update of PHP version");
            builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Thread thread = new Thread(new PostInfo((SettingsUpdatePhpFragment) App.get().getFragmentHandler().getCurrentFragment(), ip,
                            PostCommand.UPDATE_PHP));
                    thread.start();
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