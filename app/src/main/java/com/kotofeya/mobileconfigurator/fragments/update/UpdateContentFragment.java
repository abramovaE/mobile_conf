package com.kotofeya.mobileconfigurator.fragments.update;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Downloader;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.RvAdapter;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.user.UserFactory;
import com.kotofeya.mobileconfigurator.user.UserType;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class UpdateContentFragment extends UpdateFragment {

    LinearLayout label;
    private TextView downloadContentUpdateFilesTv;


    @Override
    public void onTaskCompleted(Bundle bundle) {
        super.onTaskCompleted(bundle);
    }

    @Override
    protected void loadUpdates() {
    }

    @Override
    protected void scan() {
        super.scan();
        utils.getNewBleScanner().startScan();
    }

    @Override
    protected void loadVersion() {
        Logger.d(Logger.UPDATE_CONTENT_LOG, "load version");
        boolean isInternetEnabled = utils.getInternetConnection().hasInternetConnection();
        if(isInternetEnabled) {
            Downloader transpDownloader = new Downloader(this);
            transpDownloader.execute(Downloader.TRANSPORT_CONTENT_VERSION_URL);
            Downloader stationDownloader = new Downloader(this);
            stationDownloader.execute(Downloader.STATION_CONTENT_VERSION_URL);
        }
    }

    @Override
    protected void setMainTextLabelText() {
        mainTxtLabel.setText(R.string.update_content_main_txt_label);
    }

    @Override
    protected int getAdapterType() {
        return RvAdapter.UPDATE_CONTENT_TYPE;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        label = view.findViewById(R.id.update_content_label);
        downloadContentUpdateFilesTv = view.findViewById(R.id.downloadCoreUpdateFilesTv);
        downloadContentUpdateFilesTv.setVisibility(View.VISIBLE);
        updateFilesTv();
        return view;
    }


    private void updateFilesTv(){
        StringBuilder sb = new StringBuilder();
        new LinkedList<>(App.get().getUpdateContentFilePaths()).stream()
                .forEach(it -> sb.append(it).append("\n"));
        downloadContentUpdateFilesTv.setText(sb.toString());
    }


    @Override
    public void onStart() {
        Logger.d(Logger.UPDATE_CONTENT_LOG, "update content fragment onStart");
        super.onStart();
        versionLabel.setVisibility(View.GONE);
//        checkVersionButton.setVisibility(View.GONE);
        mainBtnRescan.setVisibility(View.VISIBLE);
        label.setVisibility(View.VISIBLE);
        utils.getNewBleScanner().startScan();


        // TODO: 24.08.2021 download content from server
        checkVersionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> transportContent = getTransportContent();
                createUploadContentToStorageDialog(transportContent).create().show();

            }
        });
    }


    private static Map<String, String> getTransportContent(){
        Map<String, String> transportContent = new HashMap<>();
         for (String s : Downloader.tempUpdateTransportContentFiles) {
            UserType userType = UserFactory.getUserType();
            if(userType.equals(UserType.USER_FULL)) {
                Logger.d(Logger.TRANSPORT_CONTENT_LOG, "transport content file: " + s);
                transportContent.put(s.substring(0, s.indexOf("/")), s);
            } else if(userType.equals(UserType.USER_TRANSPORT)){
                String login = App.get().getLogin();
                String region = login.substring(login.lastIndexOf("_") + 1);
                if(s.contains(region) || s.contains("zzz")) {
                    transportContent.put(s.substring(0, s.indexOf("/")), s);
                }
            }
        }
        return transportContent;
    }

    private AlertDialog.Builder createUploadContentToStorageDialog(Map<String, String> contentMap){
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
                        UpdateContentConfDialog.UploadContentConfDialog d = new UpdateContentConfDialog.UploadContentConfDialog();
                        d.setArguments(bundle);
                        d.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().CONFIRMATION_DIALOG_TAG);
                    }
                });
        return builder;
    }

    public static class UploadContentToStorageDialog extends DialogFragment{
//        @NonNull
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            AlertDialog.Builder builder = new AlertDialog.Builder((App.get().getFragmentHandler().getCurrentFragment()).getActivity());
//            builder.setTitle("Confirmation is required");
//            builder.setMessage("Choose the file for upload");
//            Map<String>
//            String[] content = getTransportContent().keySet().toArray(new String[getTransportContent().size()]);
//            builder.setItems(content,
//                    new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            String downloadCode = TaskCode.UPDATE_TRANSPORT_CONTENT_DOWNLOAD_CODE + "";
//                            Downloader downloader = new Downloader((UpdateContentFragment) App.get().getFragmentHandler().getCurrentFragment());
//                            downloader.execute(downloadCode);
//                        }
//                    });
//            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int id) {
//                }
//            });
//            builder.setCancelable(true);
//            return builder.create();
//        }
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
            Map<String, String> transportContent = getTransportContent();
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

                Logger.d(Logger.UPDATE_CONTENT_LOG, "ip: " + ip + ", key: " + key + ", value: " + value +
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

                        if(ip != null) {
                            downloader.execute(value, ip, downloadCode);
                        }
                        else {
                            downloader.execute(value, "", TaskCode.UPDATE_TRANSPORT_CONTENT_UPLOAD_TO_STORAGE_CODE + "");
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



//update content -
//ssid = серийцный номер
//каррен инкр - текущая версия
//сервер инкр = актуальная версия