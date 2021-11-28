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
import androidx.recyclerview.widget.DiffUtil;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.ClientsDiffUtil;
import com.kotofeya.mobileconfigurator.Downloader;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.SshConnection;

import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.activities.InterfaceUpdateListener;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


public class SettingsUpdateCoreFragment extends UpdateFragment implements InterfaceUpdateListener {

    private Button downloadCoreUpdateFilesBtn;
    private TextView downloadCoreUpdateFilesTv;
    private TextView scannerTimer;
    private Thread timerThread;
    private LocalTime localTime;
    private androidx.appcompat.app.AlertDialog scanClientsDialog;

    @Override
    protected void setMainTextLabelText() {
        mainTxtLabel.setText("Update the core");
    }

    @Override
    protected RvAdapterType getAdapterType() {
        return RvAdapterType.SETTINGS_UPDATE_CORE;
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
            updateFilesTv();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        scannerTimer = view.findViewById(R.id.scanner_timer);
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
        updateFilesTv();
        viewModel.getClients().observe(getViewLifecycleOwner(), this::updateClients);
        return view;
    }

    private void updateClients(List<String> strings) {
        List<String> oldClients = rvAdapter.getObjects().stream().map(it -> it.getSsid()).collect(Collectors.toList());
        List<String> newClients = strings;
        Logger.d(Logger.UPDATE_CORE_LOG, "oldClients: " + oldClients);
        Logger.d(Logger.UPDATE_CORE_LOG, "newClients: " + newClients);
        ClientsDiffUtil clientsDiffUtil = new ClientsDiffUtil(oldClients, newClients);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(clientsDiffUtil);
        rvAdapter.setObjects(rvAdapter.getObjects().stream().
                filter(it -> newClients.contains(it.getIp())).collect(Collectors.toList()));
        diffResult.dispatchUpdatesTo(rvAdapter);
    }

    private void updateFilesTv(){
        StringBuilder sb = new StringBuilder();
        new LinkedList<>(Arrays.asList(App.get().getUpdateCoreFilesPath())).stream()
                .forEach(it -> sb.append(it.getName()).append("\n"));
        downloadCoreUpdateFilesTv.setText(sb.toString());
    }

    @Override
    public void onTaskCompleted(Bundle result) {
        String command = result.getString(BundleKeys.COMMAND_KEY);
        String ip = result.getString(BundleKeys.IP_KEY);
        String response = result.getString(BundleKeys.RESPONSE_KEY);
        Logger.d(Logger.UPDATE_CORE_LOG, "update core upload code: " + result);
        Logger.d(Logger.UPDATE_CORE_LOG, "command: " + command);
        Logger.d(Logger.UPDATE_CORE_LOG, "ip: " + ip);
        Logger.d(Logger.UPDATE_CORE_LOG, "response: " + response);
        Logger.d(Logger.UPDATE_CORE_LOG, "res: " + result.getString("result"));
        Logger.d(Logger.UPDATE_CORE_LOG, "resultCode: " + result.getInt("resultCode"));
        progressBar.setVisibility(View.GONE);


        int resultCode = result.getInt(BundleKeys.RESULT_CODE_KEY);
        String res = result.getString("result");
        Logger.d(Logger.UPDATE_CORE_LOG, "res: " + res);

        if(resultCode == Downloader.UPDATE_CORE_DOWNLOAD_CODE){
            Toast.makeText(getActivity(), "Файлы, необходимые для обновления скачаны", Toast.LENGTH_SHORT).show();
            StringBuilder sb = new StringBuilder();
            new LinkedList<>(Arrays.asList(Downloader.tempUpdateCoreFiles)).stream().forEach(it -> sb.append(it.getName()).append("\n"));
            downloadCoreUpdateFilesTv.setText(sb.toString());
        }
        if(resultCode == TaskCode.DOWNLOADER_ERROR_CODE){
            Toast.makeText(context, "При загрузке файлов произошла ошибка", Toast.LENGTH_SHORT).show();
        }

        if(res != null && res.contains("загружен")){
            clearTextLabel();
            stopTimer();
            startTimer();
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
                }
            });
            dialog.show();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            scanClientsDialog = utils.getScanClientsDialog();
            scanClientsDialog.show();
            utils.updateClients(this);
        }
    }

    @Override
    protected void updateUI(List<Transiver> transivers){
        super.updateUI(transivers);
        stopTimer();
        for(Transiver t: transivers){
            String ip = t.getIp();
            if(ip != null) {
                Logger.d(Logger.UPDATE_CORE_LOG, "coreUpdateIteration ip: " + ip);
                Logger.d(Logger.UPDATE_CORE_LOG, "coreUpdateIteration: " + t.getSsid() + " " + SshConnection.getCoreUpdateIteration(ip));
                int coreUpdateIteration = SshConnection.getCoreUpdateIteration(ip);
                if(coreUpdateIteration > 0 && coreUpdateIteration < 4){
                    progressTv.setVisibility(View.VISIBLE);
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
    }

    @Override
    public void clearTextLabel(){
        Logger.d(Logger.UPDATE_CORE_LOG, "clearTextLabel()");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressTv.setVisibility(View.GONE);
                progressTv.setText("");
            }
        });
    }

    public void startTimer(){
        localTime = LocalTime.of(0, 0, 0, 0);
        Runnable runnable = new CountDownRunner();
        if(timerThread != null){
            timerThread.interrupt();
        }
        timerThread = new Thread(runnable);
        timerThread.start();
        scannerTimer.setVisibility(View.VISIBLE);
    }

    public void stopTimer(){
        if(timerThread != null){
            timerThread.interrupt();
        }
        scannerTimer.setVisibility(View.GONE);
    }

    @Override
    public void clientsScanFinished() {
        scanClientsDialog.dismiss();

    }

    class CountDownRunner implements Runnable {
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    doWork();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    scannerTimer.setVisibility(View.GONE);
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                }
            }
        }
    }

    public void doWork() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
                    localTime = localTime.plusSeconds(1L);
                    scannerTimer.setText(localTime.format(dtf));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void setProgressTvText(String text){
        progressTv.setVisibility(View.VISIBLE);
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
            String ip = getArguments().getString(BundleKeys.IP_KEY);
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
                    } else {
                        Toast.makeText(getActivity(), "please, download files from server at start", Toast.LENGTH_SHORT).show();

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

    public static class DownloadFilesDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
//            builder.setTitle(R.string.confirmation_is_required);
            builder.setMessage("Please, download files for update from server");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

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