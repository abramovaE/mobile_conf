package com.kotofeya.mobileconfigurator.fragments.update;

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

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Downloader;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.RvAdapter;
import com.kotofeya.mobileconfigurator.UploadContentConfDialog;
import com.kotofeya.mobileconfigurator.user.UserFactory;
import com.kotofeya.mobileconfigurator.user.UserType;

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
        sb.append("Сохраненные файлы: \n");
        new LinkedList<>(App.get().getUpdateContentFilePaths()).stream()
                .forEach(it -> sb.append(it.substring(it.lastIndexOf("/") + 1, it.indexOf("_"))).append("\n"));
        downloadContentUpdateFilesTv.setText(sb.toString());
    }


    @Override
    public void onStart() {
        Logger.d(Logger.UPDATE_CONTENT_LOG, "update content fragment onStart");
        super.onStart();
        versionLabel.setVisibility(View.GONE);
        mainBtnRescan.setVisibility(View.VISIBLE);
        label.setVisibility(View.VISIBLE);
        utils.getNewBleScanner().startScan();
        checkVersionButton.setText("Загрузить файлы для обновления в память телефона");
        checkVersionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> transportContent = getTransportContent();
                createUploadContentToStorageDialog(transportContent).create().show();
            }
        });

    }


    private String getTransportFileKey(String s, boolean isInternetEnabled){
        if(isInternetEnabled){
            return s.substring(0, s.indexOf("/"));
        } else {
            return s.substring(s.lastIndexOf("/") + 1).split("_")[0];
        }
    }


    private Map<String, String> addToTransportContent(Map<String, String> transportContent,
                                                             String key, String value){
        UserType userType = UserFactory.getUserType();
        if(userType.equals(UserType.USER_FULL)) {
            transportContent.put(key, value);
        } else if(userType.equals(UserType.USER_TRANSPORT)){
            String login = App.get().getLogin();
            String region = login.substring(login.lastIndexOf("_") + 1);
            if(value.contains(region) || value.contains("zzz")) {
                transportContent.put(key, value);
            }
        }
        return transportContent;
    }

    private Map<String, String> getTransportContent(){
        Map<String, String> transportContent = new HashMap<>();
        boolean isInternetEnabled = utils.getInternetConnection().hasInternetConnection();
        if(!isInternetEnabled) {
            for (String s : App.get().getUpdateContentFilePaths()) {
                String key = getTransportFileKey(s, isInternetEnabled);
                transportContent = addToTransportContent(transportContent, key, s);
            }
        } else {
            for (String s : Downloader.tempUpdateTransportContentFiles) {
                String key = getTransportFileKey(s, isInternetEnabled);
                transportContent = addToTransportContent(transportContent, key, s);
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
                        UploadContentConfDialog d = new UploadContentConfDialog();
                        d.setArguments(bundle);
                        d.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().CONFIRMATION_DIALOG_TAG);
                    }
                });
        return builder;
    }


}



//update content -
//ssid = серийцный номер
//каррен инкр - текущая версия
//сервер инкр = актуальная версия