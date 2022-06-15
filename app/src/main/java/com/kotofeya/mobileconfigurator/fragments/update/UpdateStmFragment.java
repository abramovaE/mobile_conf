package com.kotofeya.mobileconfigurator.fragments.update;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Downloader;
import com.kotofeya.mobileconfigurator.InternetConn;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.HashMap;
import java.util.Map;

public class UpdateStmFragment extends UpdateFragment {

    private static final String PREF_TRANSP = "mobile";
    private static final String PREF_STAT = "stationary";

     @Override
     public void loadVersion() {
         Logger.d(Logger.UPDATE_STM_LOG, "load version");
         boolean isInternetEnabled = InternetConn.hasInternetConnection();
        if(isInternetEnabled){
            Downloader downloader = new Downloader(this);
            downloader.execute(Downloader.STM_VERSION_URL);
        }
    }

    @Override
    public void setMainTextLabelText() {
        viewModel.setMainTxtLabel(getString(R.string.update_stm_main_txt_label));
    }

    @Override
    public RvAdapterType getAdapterType() {
        return RvAdapterType.SETTINGS;
    }


    public void loadUpdates(){
        Downloader downloader = new Downloader(this);
        downloader.execute(Downloader.STM_VERSION_URL);
    }

    @Override
    public void adapterItemOnClick(Transiver transiver) {
        Logger.d(Logger.SCANNER_ADAPTER_LOG, "Update stm was pressed");
        if (Downloader.tempUpdateStmFiles != null && !Downloader.tempUpdateStmFiles.isEmpty()) {
            Bundle bundle = new Bundle();
            bundle.putString(BundleKeys.IP_KEY, transiver.getIp());
            bundle.putBoolean(BundleKeys.IS_TRANSPORT_KEY, transiver.isTransport());
            bundle.putBoolean(BundleKeys.IS_STATIONARY_KEY, transiver.isStationary());
            UpdateStmConfDialog dialog = new UpdateStmConfDialog();
            dialog.setArguments(bundle);
            dialog.show(fragmentHandler.getFragmentManager(),
                    FragmentHandler.CONFIRMATION_DIALOG_TAG);
        }
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
            String ip = getArguments().getString(BundleKeys.IP_KEY);
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

            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle("Choose the stm version for upload");
            builder.setItems(content,
                    (dialog, which) -> {
                        Logger.d(Logger.UPDATE_STM_LOG, "dialogContent: " + content[which]);
                        Bundle bundle = new Bundle();
                        bundle.putString("key", content[which]);
                        bundle.putString("value", commonContent.get(content[which]));
                        bundle.putString(BundleKeys.IP_KEY, ip);
                        UploadStmConfDialog d = new UploadStmConfDialog();
                        d.setArguments(bundle);
                        FragmentHandler fragmentHandler = ((MainActivity)getActivity()).getFragmentHandler();
                        d.show(fragmentHandler.getFragmentManager(), fragmentHandler.CONFIRMATION_DIALOG_TAG);
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
            String ip = getArguments().getString(BundleKeys.IP_KEY);
            Logger.d(Logger.UPDATE_STM_LOG, "key: " + key);
            FragmentHandler fragmentHandler = ((MainActivity)getActivity()).getFragmentHandler();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Confirmation is required");
            builder.setMessage("Confirm the upload of " + key);
            builder.setPositiveButton("upload", (dialog, id) -> {
                Downloader downloader = new Downloader((UpdateStmFragment) fragmentHandler.getCurrentFragment());
                downloader.execute(value, ip, TaskCode.UPDATE_STM_DOWNLOAD_CODE + "");
            });
            builder.setNegativeButton("cancel", (dialog, id) -> { });
            builder.setCancelable(true);
            return builder.create();
        }
    }
}
