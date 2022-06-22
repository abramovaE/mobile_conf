package com.kotofeya.mobileconfigurator.fragments.update;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Downloader;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class SettingsUpdateCoreFragment extends UpdateFragment{
    private static final String TAG = SettingsUpdateCoreFragment.class.getSimpleName();
    private Thread timerThread;
    private LocalTime localTime;

    @Override
    public void setMainTextLabelText() {
        viewModel.setMainTxtLabel("Update the core");
    }

    @Override
    public RvAdapterType getAdapterType() {
        return RvAdapterType.SETTINGS_UPDATE_CORE;
    }

    @Override
    public void onStart() {
        Logger.d(Logger.STM_LOG_LOG, "onStart()");
        super.onStart();
        binding.versionLabel.setVisibility(View.GONE);
        binding.checkVersionBtn.setVisibility(View.GONE);
//        coreUpdateSsidIteration = App.get().getCoreUpdateSsidIteration();
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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        binding.downloadCoreUpdateFilesBtn.setVisibility(View.VISIBLE);

        binding.downloadCoreUpdateFilesBtn.setOnClickListener(v -> {
            binding.scannerProgressBar.setVisibility(View.VISIBLE);
            Downloader downloader = new Downloader(SettingsUpdateCoreFragment.this);
            downloader.execute(Downloader.CORE_URLS);
        });
        binding.downloadCoreUpdateFilesTv.setVisibility(View.VISIBLE);
        updateFilesTv();
        viewModel.getClients().observe(getViewLifecycleOwner(), this::updateClients);
        viewModel.isRescanPressed.observe(getViewLifecycleOwner(), this::rescanPressed);
        scannerFragmentVM.getTime().observe(getViewLifecycleOwner(), this::updateTime);
        return view;
    }

    private void rescanPressed(Boolean aBoolean) {
        if(aBoolean) {
            stopTimer();
        }
    }

    private void updateTime(String s) {
        binding.scannerTimer.setText(s);
    }

    private void updateClients(List<String> clients) {
        viewModel.updateTransivers();
    }

    private void updateFilesTv(){
        StringBuilder sb = new StringBuilder();
        new LinkedList<>(Arrays.asList(App.get().getUpdateCoreFilesPath()))
                .forEach(it -> sb.append(it.getName()).append("\n"));
        binding.downloadCoreUpdateFilesTv.setText(sb.toString());
    }

    @Override
    public void onTaskCompleted(Bundle result) {

//        viewModel.set_progressTvVisibility();
        binding.scannerProgressBar.setVisibility(View.GONE);

        int resultCode = result.getInt(BundleKeys.RESULT_CODE_KEY);
        String res = result.getString("result");
        String ip = result.getString(BundleKeys.IP_KEY);
        String serial = result.getString(BundleKeys.SERIAL_KEY);
        int newIteration = result.getInt(BundleKeys.NEW_ITERATION);
        Logger.d(TAG, "onTaskCompleted(), newIteration: " + newIteration);

        if(resultCode == Downloader.UPDATE_CORE_DOWNLOAD_CODE){
            Toast.makeText(requireActivity(),
                    "Файлы, необходимые для обновления скачаны", Toast.LENGTH_SHORT).show();
            StringBuilder sb = new StringBuilder();
            new LinkedList<>(Arrays.asList(Downloader.tempUpdateCoreFiles)).forEach(it -> sb.append(it.getName()).append("\n"));
            binding.downloadCoreUpdateFilesTv.setText(sb.toString());
        }
        if(resultCode == TaskCode.DOWNLOADER_ERROR_CODE){
            Toast.makeText(requireActivity(),
                    "При загрузке файлов произошла ошибка", Toast.LENGTH_SHORT).show();
        }
        if(res != null && res.contains("загружен")){

            clearTextLabel();
            startTimer();
            App.get().putSsidIteration(serial, newIteration);
            if(newIteration == Downloader.tempUpdateCoreFiles.length){
                App.get().resetCoreFilesCounter(serial);
            }
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setMessage(res);
            dialog.setCancelable(false);
            dialog.setPositiveButton("Ok", (dialog1, which) ->
                    clientsHandler.removeClient(ip));


            dialog.show();
        }
    }


    @Override
    protected void updateUI(List<Transiver> transceivers){
//        Logger.d(TAG, "updateUI()");
        super.updateUI(transceivers);

        Map<String, Integer> coreUpdateSsidIteration = App.get().getCoreUpdateSsidIteration();

        for(Transiver t: transceivers){
            String ip = t.getIp();
            if(ip != null) {

//                Logger.d(Logger.UPDATE_CORE_LOG, "coreUpdateIteration: " + t.getSsid() + " " + SshConnection.getCoreUpdateIteration(t.getSsid()));
                Integer coreUpdateIteration = coreUpdateSsidIteration.get(t.getSsid());
                if(coreUpdateIteration != null){
                    if(coreUpdateIteration > 0 && coreUpdateIteration < 4){
                        setProgressTvText(getProgressTvText(t.getSsid(), coreUpdateIteration));
                        Logger.d(TAG, "coreUpdateIteration ip: " + ip +
                                ", iteration: " + coreUpdateIteration);
                        if(coreUpdateIteration == 1){
//                            String version = CustomViewModel.getVersion(t.getSsid());
//                            version = (version == null) ? "old" : "new";
//                            if(version.equals("new")){
//                                Logger.d(TAG, "transceiver has already updated");
//                            } else {
                                if(t.getOsVersion().contains("pre")){
                                    uploadFile(ip, t.getSsid(), coreUpdateIteration);
                                    return;
                                } else{
                                    Logger.d(TAG, "iteration 1 was failed, start updating again");
                                    App.get().resetCoreFilesCounter(t.getSsid());
//                                    uploadFile(ip, t.getSsid(), 0);
                                }
//                            }
                        } else {
                            uploadFile(ip, t.getSsid(), coreUpdateIteration);
                            return;
                        }
//                        stopTimer();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void clearTextLabel(){
        Logger.d(Logger.UPDATE_CORE_LOG, "clearTextLabel()");
        viewModel.set_progressTvVisibility(false);
        viewModel.set_progressTvText("");
    }

    public void startTimer(){
        Logger.d(TAG, "startTimer()");
        localTime = LocalTime.of(0, 0, 0, 0);
        Runnable runnable = new CountDownRunner();
        if(timerThread != null){
            timerThread.interrupt();
        }
        timerThread = new Thread(runnable);
        timerThread.start();
        binding.scannerTimer.setVisibility(View.VISIBLE);
    }

    public void stopTimer(){
        Logger.d(TAG, "stopTimer()");
        if(timerThread != null){
            timerThread.interrupt();
        }
        binding.scannerTimer.setVisibility(View.GONE);
    }

    @Override
    public void adapterItemOnClick(Transiver transiver) {
        String ssid = transiver.getSsid();
        String ip = viewModel.getIp(ssid);
        if(ip != null) {
            Logger.d(Logger.SCANNER_ADAPTER_LOG, "Update core was pressed");
            if (Downloader.isCoreUpdatesDownloadCompleted()) {
//                showChooseFileDialog(ip, ssid);
                showConfirmDialog(ip, ssid);
            } else {
                fragmentHandler.showMessage("Please, download files for update from server");
            }
        }
    }


    private void showConfirmDialog(String ip, String ssid){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.confirmation_is_required);
        builder.setMessage("Confirm the update the core");

        builder.setPositiveButton("Update", (dialog, id) -> {
            if(Downloader.isCoreUpdatesDownloadCompleted()){
//                SshConnection.resetCoreFilesCounter(ip);
                App.get().resetCoreFilesCounter(ssid);
                ((SettingsUpdateCoreFragment) fragmentHandler.getCurrentFragment())
                        .uploadFile(ip, ssid, 0);
            } else {
                Toast.makeText(getActivity(),
                        "please, download files from server at start",
                        Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("cancel", (dialog, id) -> { });
        builder.setCancelable(true);
        builder.show();
    }
//
//    private void showChooseFileDialog(String ip, String ssid){
//        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
//        builder.setTitle("Select the file to upload");
//
//        String[] array = new String[Downloader.tempUpdateCoreFiles.length];
//        for(int i = 0; i < Downloader.tempUpdateCoreFiles.length; i++){
//            array[i] = Downloader.tempUpdateCoreFiles[i].getName();
//        }
//
//        ArrayAdapter<String> adapter = new ArrayAdapter(requireActivity(),
//                android.R.layout.select_dialog_item, array);
//
//        DialogInterface.OnClickListener myClickListener = (dialog, which) -> uploadFile(ip, ssid, which);
//        builder.setAdapter(adapter, myClickListener);
//        builder.setCancelable(true);
//        builder.show();
//    }


    class CountDownRunner implements Runnable {
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    doWork();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
//                    binding.scannerTimer.setVisibility(View.GONE);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void doWork() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        localTime = localTime.plusSeconds(1L);
        scannerFragmentVM.setTime(localTime.format(dtf));
    }


    public void setProgressTvText(String text){
        viewModel.set_progressTvVisibility(true);
        viewModel.set_progressTvText(text);
    }


    public void uploadFile(String ip, String serial, int coreUpdateIteration){
        Logger.d(TAG, "uploadFile(), serial: " + serial + ", iteration: " + coreUpdateIteration);
        setProgressTvText(getProgressTvText(serial, coreUpdateIteration));
        SshConnection connection = new SshConnection(ip, this, this, coreUpdateIteration, serial);
        connection.execute(SshConnection.UPDATE_CORE_UPLOAD_CODE);
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
}