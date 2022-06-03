package com.kotofeya.mobileconfigurator.fragments.update;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Downloader;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.UpdateContentConfDialog;
import com.kotofeya.mobileconfigurator.UploadContentConfDialog;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;
import com.kotofeya.mobileconfigurator.transivers.Transiver;
import com.kotofeya.mobileconfigurator.user.UserFactory;
import com.kotofeya.mobileconfigurator.user.UserType;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class UpdateContentFragment extends UpdateFragment {

    @Override
    public void onTaskCompleted(Bundle bundle) {
        super.onTaskCompleted(bundle);
    }

    @Override
    public void scan() {
        super.scan();
        utils.getNewBleScanner().startScan();
    }

    @Override
    public void loadVersion() {
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
        viewModel.setMainTxtLabel(getString(R.string.update_content_main_txt_label));
    }

    @Override
    protected RvAdapterType getAdapterType() {
        return RvAdapterType.UPDATE_CONTENT_TYPE;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        binding.downloadCoreUpdateFilesTv.setVisibility(View.VISIBLE);
        updateFilesTv();
        return view;
    }

    private void updateFilesTv(){
        StringBuilder sb = new StringBuilder();
        sb.append("Сохраненные файлы: \n");
        new LinkedList<>(App.get().getUpdateContentFilePaths())
                .forEach(it -> sb.append(it.substring(it.lastIndexOf("/") + 1, it.indexOf("_"))).append("\n"));
        binding.downloadCoreUpdateFilesTv.setText(sb.toString());
    }

    @Override
    public void onStart() {
        Logger.d(Logger.UPDATE_CONTENT_LOG, "update content fragment onStart");
        super.onStart();
        binding.versionLabel.setVisibility(View.GONE);
        viewModel.setMainBtnRescanVisibility(View.VISIBLE);
        binding.updateContentLabel.setVisibility(View.VISIBLE);
        utils.getNewBleScanner().startScan();
        binding.checkVersionBtn.setText("Загрузить файлы для обновления в память телефона");
        binding.checkVersionBtn.setOnClickListener(v -> {
            Map<String, String> transportContent = getTransportContent();
            createUploadContentToStorageDialog(transportContent).create().show();
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
        UserType userType = UserFactory.getUser().getUserType();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Choose the city for upload");
        String[] content = contentMap.keySet().toArray(new String[contentMap.size()]);
        builder.setItems(content,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Logger.d(Logger.UPDATE_CONTENT_LOG, "dialogContent: " + content[which]);
                        Bundle bundle = new Bundle();
                        bundle.putString(BundleKeys.KEY, "transp " + content[which]);
                        bundle.putString(BundleKeys.VALUE, contentMap.get(content[which]));
                        UploadContentConfDialog d = new UploadContentConfDialog();
                        d.setArguments(bundle);
                        d.show(fragmentHandler.getFragmentManager(), fragmentHandler.CONFIRMATION_DIALOG_TAG);
                    }
                });
        return builder;
    }

    @Override
    public void adapterItemOnClick(Transiver transiver) {

        boolean isTransport = transiver.isTransport();
        boolean isStationary = transiver.isStationary();

        Logger.d(Logger.SCANNER_ADAPTER_LOG, "Update content was pressed, isTransport: " +
                isTransport + ", isStationary: " + isStationary);
        utils.getNewBleScanner().stopScan();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKeys.IP_KEY, transiver.getIp());
        bundle.putBoolean(BundleKeys.IS_TRANSPORT_KEY, isTransport);
        bundle.putBoolean(BundleKeys.IS_STATIONARY_KEY, isStationary);
        DialogFragment dialogFragment = null;
        if (isTransport) {
            dialogFragment = new UpdateContentConfDialog();
        } else if (isStationary) {
            String key = transiver.getSsid();
            if (Downloader.tempUpdateStationaryContentFiles != null &&
                    Downloader.tempUpdateStationaryContentFiles.containsKey(key)) {
                bundle.putString(BundleKeys.KEY, key);
                bundle.putString(BundleKeys.VALUE, key + "/data.tar.bz2");
                dialogFragment = new UploadContentConfDialog();
            }
        }
        if (dialogFragment != null) {
            dialogFragment.setArguments(bundle);
            dialogFragment.show(fragmentHandler.getFragmentManager(),
                    fragmentHandler.CONFIRMATION_DIALOG_TAG);
        }
    }
}

//update content -
//ssid = серийцный номер
//каррен инкр - текущая версия
//сервер инкр = актуальная версия