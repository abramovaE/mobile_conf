package com.kotofeya.mobileconfigurator.fragments.update;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import androidx.fragment.app.DialogFragment;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Downloader;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.ScannerAdapter;
import com.kotofeya.mobileconfigurator.SshConnection;

import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.network.PostInfo;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class SettingsUpdateCoreFragment extends UpdateFragment {

    private Button downloadCoreUpdateFilesBtn;
    private TextView downloadCoreUpdateFilesTv;
    private TextView progressTv;

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
        if(Downloader.isCoreUpdatesDownloadCompleted()) {
            StringBuilder sb = new StringBuilder();
            (new LinkedList<>(Arrays.asList(Downloader.tempUpdateCoreFiles))).stream().forEach(it -> sb.append(it.getName()).append("\n"));
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
        progressTv = view.findViewById(R.id.progressTv);
        progressTv.setVisibility(View.VISIBLE);

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
            new LinkedList<>(Arrays.asList(Downloader.tempUpdateCoreFiles)).stream().forEach(it -> sb.append(it.getName()).append("\n"));
            downloadCoreUpdateFilesTv.setText(sb.toString());
        }

        if(resultCode == TaskCode.SSH_ERROR_CODE){

        }
        else {

        }

        if(res != null && res.contains("загружен")){
            Logger.d(Logger.UPDATE_CORE_LOG, "getActivity: " + getActivity());
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setMessage(res);
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                    scan();
                }
            });
            dialog.show();
            clearTextLabel();
        }
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
                    progressTv.setText(getProgressTvText(t.getSsid(), coreUpdateIteration));
                    uploadFile(ip, t.getSsid(), coreUpdateIteration);
                    break;
                }
            }
        }
    }

    @Override
    public void setProgressBarGone() {
        super.setProgressBarGone();
//        progressTv.setText("");
    }

    @Override
    public void clearTextLabel(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressTv.setText("");
            }
        });
    }

    public void setProgressTvText(String text){
        progressTv.setText(text);
    }


    public void uploadFile(String ip, String serial, int coreUpdateIteration){

        ((SettingsUpdateCoreFragment) App.get().getFragmentHandler().getCurrentFragment())
                .setProgressTvText(getProgressTvText(serial, coreUpdateIteration));
        SshConnection connection = new SshConnection(
                (SettingsUpdateCoreFragment) App.get().getFragmentHandler().getCurrentFragment(),
                (SettingsUpdateCoreFragment) App.get().getFragmentHandler().getCurrentFragment());
        connection.execute(ip, SshConnection.UPDATE_CORE_UPLOAD_CODE);
    }

    private String getProgressTvText(String serial, int coreUpdateIteration){
        switch (coreUpdateIteration){
            case 0:
            case 1:
                return  "Загружаем файл " +
                        Downloader.tempUpdateCoreFiles[coreUpdateIteration].getName() +
                        " на трансивер " + serial;
            case 2:
                return "Загружаем файлы " +
                        Downloader.tempUpdateCoreFiles[2].getName() + ", " + Downloader.tempUpdateCoreFiles[3].getName() +
                        " на трансивер " + serial;

        }
        return "";
    }


    public static class UpdateCoreConfDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String ip = getArguments().getString("ip");
            String serial = getArguments().getString("serial");
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.confirmation_is_required);
            builder.setMessage("Confirm the update the core");
            builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {
                    if(Downloader.isCoreUpdatesDownloadCompleted()){
                        SshConnection.resetCoreFilesCounter(ip);
                        ((SettingsUpdateCoreFragment) App.get().getFragmentHandler().getCurrentFragment())
                                .uploadFile(ip, serial, 0);
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



}
