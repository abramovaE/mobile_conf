package com.kotofeya.mobileconfigurator.fragments.update;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Downloader;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.activities.MainActivity;

import java.util.HashMap;
import java.util.Map;

public class UpdateContentFragment extends UpdateFragment {

    LinearLayout label;

    @Override
    public void onTaskCompleted(Bundle bundle) {
        super.onTaskCompleted(bundle);

    }

    @Override
    void loadUpdates() {
    }

    @Override
    protected void scan() {
        super.scan();
        utils.getBluetooth().startScan(true);
    }

    @Override
    void loadVersion() {
        Logger.d(Logger.UPDATE_CONTENT_LOG, "update content fragment load version");
        Downloader transpDownloader = new Downloader(this);
        transpDownloader.execute(Downloader.TRANSPORT_CONTENT_VERSION_URL);

        Downloader stationDownloader = new Downloader(this);
        stationDownloader.execute(Downloader.STATION_CONTENT_VERSION_URL);
    }

    @Override
    void setMainTextLabelText() {
        mainTxtLabel.setText(R.string.update_content_main_txt_label);
    }

    @Override
    ScannerAdapter getScannerAdapter() {
        return new ScannerAdapter(context, utils, ScannerAdapter.UPDATE_CONTENT_TYPE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        label = view.findViewById(R.id.update_content_label);
        return view;
    }

    @Override
    public void onStart() {
        Logger.d(Logger.UPDATE_CONTENT_LOG, "update content fragment onStart");
        super.onStart();
        versionLabel.setVisibility(View.GONE);
        checkVersionButton.setVisibility(View.GONE);
        mainBtnRescan.setVisibility(View.VISIBLE);
        label.setVisibility(View.VISIBLE);

        utils.getBluetooth().startScan(true);



    }

    public static class UpdateContentConfDialog extends DialogFragment {

        boolean isTransport;
        boolean isStationary;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            this.isTransport = getArguments().getBoolean("isTransport");
            this.isStationary = getArguments().getBoolean("isStationary");

            Logger.d(Logger.UPDATE_CONTENT_LOG, "isTransport: " + isTransport +", isStationary: " + isStationary);
            String ip = getArguments().getString("ip");
            Map<String, String> transportContent = new HashMap<>();
            for (String s : Downloader.tempUpdateTransportContentFiles) {
                Logger.d(Logger.TRANSPORT_CONTENT_LOG, "transport content file: " + s);
                transportContent.put(s.substring(0, s.indexOf("/")), s);
            }
            AlertDialog.Builder builder = createUpdateContentConfDialog(ip, transportContent);
            builder.setCancelable(true);
            return builder.create();
        }

        public static class UploadContentConfDialog extends DialogFragment{
            @NonNull
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                String key = getArguments().getString("key");
                String value = getArguments().getString("value");
                String ip = getArguments().getString("ip");
                boolean isStationary = getArguments().getBoolean("isStationary");
                boolean isTransport = getArguments().getBoolean("isTransport");

                Logger.d(Logger.UPDATE_CONTENT_LOG, "key: " + key + ", value: " + value +
                        ", isStationary: " + isStationary + ", isTransport: " + isTransport);
                AlertDialog.Builder builder = new AlertDialog.Builder((App.get().getFragmentHandler().getCurrentFragment()).getActivity());
                builder.setTitle("Confirmation is required");
                builder.setMessage("Confirm the upload of " + key);
                builder.setPositiveButton("upload", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String downloadCode = null;
                        if(isStationary){
                            downloadCode = TaskCode.UPDATE_STATION_CONTENT_DOWNLOAD_CODE + "";
                        }
                        else if(isTransport){
                            downloadCode = TaskCode.UPDATE_TRANSPORT_CONTENT_DOWNLOAD_CODE + "";
                        }
                        Downloader downloader = new Downloader((UpdateContentFragment) App.get().getFragmentHandler().getCurrentFragment());
                        downloader.execute(value, ip, downloadCode);
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



        private AlertDialog.Builder createUpdateContentConfDialog(String ip, Map<String, String> contentMap){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Choose the city for upload");
            String[] content = contentMap.keySet().toArray(new String[contentMap.size()]);
            builder.setItems(content,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Logger.d(Logger.UPDATE_CONTENT_LOG, "dialogContent: " + content[which]);
                            Bundle bundle = new Bundle();
                            bundle.putString("key", "transp " + content[which]);
                            bundle.putString("value", contentMap.get(content[which]));
                            bundle.putString("ip", ip);
                            bundle.putBoolean("isTransport", isTransport);
                            bundle.putBoolean("isStationary", isStationary);
                            UploadContentConfDialog d = new UploadContentConfDialog();
                            d.setArguments(bundle);
                            d.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().CONFIRMATION_DIALOG_TAG);
                        }
                    });
            return builder;
        }
    }
}
