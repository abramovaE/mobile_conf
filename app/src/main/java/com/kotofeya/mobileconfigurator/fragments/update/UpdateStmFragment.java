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
import com.kotofeya.mobileconfigurator.TaskCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UpdateStmFragment extends UpdateFragment {

    private static final String PREF_TRANSP = "mobile";
    private static final String PREF_STAT = "stationary";

     @Override
     protected void loadVersion() {
         Logger.d(Logger.UPDATE_STM_LOG, "load version");
         boolean isInternetEnabled = utils.getInternetConnection().hasInternetConnection();
        if(isInternetEnabled){
            Downloader downloader = new Downloader(this);
            downloader.execute(Downloader.STM_VERSION_URL);
        } else {
//            EnableMobileConfDialog dialog = new EnableMobileConfDialog();
//            dialog.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().ENABLE_MOBILE_DIALOG_TAG);
        }
    }

    @Override
    protected void setMainTextLabelText() {
        mainTxtLabel.setText(R.string.update_stm_main_txt_label);
    }

    @Override
    protected ScannerAdapter getScannerAdapter() {
        return new ScannerAdapter(context, utils, ScannerAdapter.UPDATE_STM_TYPE, new ArrayList<>());
    }

    protected void loadUpdates(){
        Downloader downloader = new Downloader(this);
        downloader.execute(Downloader.STM_VERSION_URL);
    }

    @Override
    public void setProgressBarVisible() {

    }

    @Override
    public void setProgressBarGone() {

    }

    public static class UpdateStmConfDialog extends DialogFragment {
        private String getKey(String s, String prefix){
            String key;
            key = prefix + getCity(s.charAt(1)) + s.substring(2, s.indexOf(".tar.bz2")) + ((s.endsWith("b.tar.bz2")) ? "bootloader" : "");
            return key;
        }

        private String getCity(char ch){
            switch (ch){
                case 'P':
                    return " Spb ";
                case 'R':
                    return " Rostov ";
                default:
                    return "";
            }
        }

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
                    transportContent.put(getKey(s, PREF_TRANSP), s);
                } else if (s.startsWith("S")) {
                    stationaryContent.put(getKey(s, PREF_STAT), s);
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
                    downloader.execute(value, ip, TaskCode.UPDATE_STM_DOWNLOAD_CODE + "");
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
