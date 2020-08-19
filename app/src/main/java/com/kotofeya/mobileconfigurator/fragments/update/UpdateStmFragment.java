package com.kotofeya.mobileconfigurator.fragments.update;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Downloader;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;

import java.util.HashMap;
import java.util.Map;

public class UpdateStmFragment extends UpdateFragment {


    @Override
    void loadVersion(){
        Downloader downloader = new Downloader(this);
        downloader.execute(Downloader.STM_VERSION_URL);
    }

    @Override
    void setMainTextLabelText() {
        mainTxtLabel.setText(R.string.update_stm_main_txt_label);
    }

    @Override
    ScannerAdapter getScannerAdapter() {
        return new ScannerAdapter(context, utils, ScannerAdapter.UPDATE_STM_TYPE);
    }

    void loadUpdates(){
        Downloader downloader = new Downloader(this);
        downloader.execute(Downloader.STM_VERSION_URL);
    }

    public static class UpdateStmConfDialog extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            boolean isTransport = getArguments().getBoolean("isTransport");
            boolean isStationary = getArguments().getBoolean("isStationary");
            String ip = getArguments().getString("ip");
            Map<String, String> transportContent = new HashMap<>();
            Map<String, String> stationaryContent = new HashMap<>();
            String[] content;
            for (String s : Downloader.tempUpdateStmFiles) {
                if (s.startsWith("M")) {
                    String key = "";
                    if (s.startsWith("MP")) {
                        key = "mobile Spb " + s.substring(2, s.indexOf(".tar.bz2"));
                    } else if (s.startsWith("MR")) {
                        key = "mobile Rostov " + s.substring(2, s.indexOf(".tar.bz2"));
                    }
                    if (s.endsWith("b.tar.bz2")) {
                        key = key + " bootloader";
                    }
                    transportContent.put(key, s);
                } else if (s.startsWith("S")) {
                    String key = "";
                    if (s.startsWith("SP")) {
                        key = "stationary Spb " + s.substring(2, s.indexOf(".tar.bz2"));
                    } else if (s.startsWith("SR")) {
                        key = "stationary Rostov " + s.substring(2, s.indexOf(".tar.bz2"));
                    }
                    if (s.endsWith("b.tar.bz2")) {
                        key = key + " bootloader";
                    }
                    stationaryContent.put(key, s);
                }
            }

            Map<String, String> commonContent = new HashMap<>();
            commonContent.putAll(transportContent);
            commonContent.putAll(stationaryContent);

            if (isTransport) {
                content = transportContent.keySet().toArray(new String[transportContent.size()]);
            } else if (isStationary) {
                content = stationaryContent.keySet().toArray(new String[stationaryContent.size()]);
            } else {

                content = commonContent.keySet().toArray(new String[commonContent.size()]);
            }


            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Choose the stm version for upload");
            builder.setItems(content,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Logger.d(Logger.UPDATE_STM_LOG, "dialogContent: " + content[which]);
                            Bundle bundle = new Bundle();
                            bundle.putString("key", content[which]);
                            bundle.putString("value", commonContent.get(content[which]));
                            bundle.putString("ip", ip);
                            UploadStmConfDialog d = new UploadStmConfDialog();
                            d.setArguments(bundle);
                            d.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().CONFIRMATION_DIALOG_TAG);
                        }
                    });
            builder.setCancelable(true);
            return builder.create();
        }


    }

    public static class UploadStmConfDialog extends DialogFragment {
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String key = getArguments().getString("key");
            String value = getArguments().getString("value");
            String ip = getArguments().getString("ip");
            Logger.d(Logger.UPDATE_STM_LOG, "key: " + key);
            AlertDialog.Builder builder = new AlertDialog.Builder((App.get().getFragmentHandler().getCurrentFragment()).getActivity());
            builder.setTitle("Confirmation is required");
            builder.setMessage("Confirm the upload of " + key);
            builder.setPositiveButton("upload", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Downloader downloader = new Downloader((UpdateStmFragment) App.get().getFragmentHandler().getCurrentFragment());
                    downloader.execute(value, ip);
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
