package com.kotofeya.mobileconfigurator.fragments.update;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Downloader;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;
import com.kotofeya.mobileconfigurator.SshConnection;

import com.kotofeya.mobileconfigurator.network.PostInfo;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.ArrayList;
import java.util.List;


public class SettingsUpdateCoreFragment extends UpdateFragment {

//    private TextView phpVersion;

    private Button downloadCoreUpdateFilesBtn;
    private TextView downloadCoreUpdateFilesTv;

    @Override
    protected void loadUpdates() {

    }

    @Override
    protected void loadVersion() {

    }

    @Override
    protected void setMainTextLabelText() {

    }

    @Override
    protected ScannerAdapter getScannerAdapter() {
        return new ScannerAdapter(context, utils, ScannerAdapter.SETTINGS_UPDATE_CORE, new ArrayList<>());
    }


    @Override
    public void onStart() {
        Logger.d(Logger.STM_LOG_LOG, "onStart");
        super.onStart();
        versionLabel.setVisibility(View.GONE);
        checkVersionButton.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(Downloader.tempUpdateCoreFiles != null && Downloader.tempUpdateCoreFiles.size() == 4
                && Downloader.IS_CORE_FILES_EXIST.stream().allMatch((it) -> it == true)) {
            StringBuilder sb = new StringBuilder();
            Downloader.tempUpdateCoreFiles.stream().forEach(it -> sb.append(it.getName()).append("\n"));
            downloadCoreUpdateFilesTv.setText(sb.toString());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mainTxtLabel.setText("Update the core");
        downloadCoreUpdateFilesBtn = view.findViewById(R.id.downloadCoreUpdateFilesBtn);
        downloadCoreUpdateFilesBtn.setVisibility(View.VISIBLE);
        downloadCoreUpdateFilesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View view = App.get().getFragmentHandler().getCurrentFragment().getView();
                ProgressBar progressBar = view.findViewById(R.id.scanner_progressBar);
                progressBar.setVisibility(View.VISIBLE);

                Downloader downloader = new Downloader(((SettingsUpdateCoreFragment) App.get().getFragmentHandler().getCurrentFragment()));
                downloader.execute(Downloader.CORE_URLS);

            }
        });

        downloadCoreUpdateFilesTv = view.findViewById(R.id.downloadCoreUpdateFilesTv);
        downloadCoreUpdateFilesTv.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onTaskCompleted(Bundle result) {
        String command = result.getString(PostInfo.COMMAND);
        String ip = result.getString(PostInfo.IP);
        String response = result.getString(PostInfo.RESPONSE);
        Logger.d(Logger.UPDATE_CORE_LOG, "update core upload code: " + result);
        Logger.d(Logger.UPDATE_CORE_LOG, "command: " + command);
        Logger.d(Logger.UPDATE_CORE_LOG, "ip: " + ip);
        Logger.d(Logger.UPDATE_CORE_LOG, "response: " + response);
        progressBar.setVisibility(View.GONE);

        int resultCode = result.getInt("resultCode");
        String res = result.getString("result");
        Logger.d(Logger.UPDATE_CORE_LOG, "res: " + res);

        if(resultCode == Downloader.UPDATE_CORE_DOWNLOAD_CODE){
            Toast.makeText(getActivity(), "Файлы, необходимые для обновления скачаны", Toast.LENGTH_SHORT).show();

            StringBuilder sb = new StringBuilder();
            Downloader.tempUpdateCoreFiles.stream().forEach(it -> sb.append(it.getName()).append("\n"));
            downloadCoreUpdateFilesTv.setText(sb.toString());


//            SshConnection.updateCoreFilesCounter(ip);
//            SshConnection connection = new SshConnection(((SettingsUpdateCoreFragment) App.get().getFragmentHandler().getCurrentFragment()));
//            connection.execute(ip, SshConnection.UPDATE_CORE_UPLOAD_CODE);
        }

        if(res != null && res.contains("загружен")){
            Toast.makeText(getActivity(), res, Toast.LENGTH_LONG).show();
        }

//        else if(resultCode == Downloader.UPDATE_CORE_UPLOAD_CODE){
//            SshConnection connection = new SshConnection(((SettingsUpdateCoreFragment) App.get().getFragmentHandler().getCurrentFragment()));
//            connection.execute(ip, SshConnection.UPDATE_CORE_UPLOAD_CODE);
//        }

//        else {
//            Logger.d(Logger.UPDATE_CORE_LOG, "super.onTaskCompleted");
//            super.onTaskCompleted(result);
//        }



    }


    @Override
    protected void updateUI(List<Transiver> transivers){
        super.updateUI(transivers);

        for(Transiver t: transivers){

            String ip = t.getIp();
            if(ip != null) {
                Logger.d(Logger.UPDATE_CORE_LOG, "coreUpdateIteration ip: " + ip);

                Logger.d(Logger.UPDATE_CORE_LOG, "coreUpdateIteration: " + t.getSsid() + " " + SshConnection.getCoreUpdateIteration(ip));
                int coreUpdateIteration = SshConnection.getCoreUpdateIteration(ip);
                if(coreUpdateIteration > 0 && coreUpdateIteration < 4){
                    SshConnection connection = new SshConnection(((SettingsUpdateCoreFragment) App.get().getFragmentHandler().getCurrentFragment()));
                    connection.execute(ip, SshConnection.UPDATE_CORE_UPLOAD_CODE);
                    break;
                }
            }

        }
    }
}
