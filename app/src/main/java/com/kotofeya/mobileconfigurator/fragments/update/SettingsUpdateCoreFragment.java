package com.kotofeya.mobileconfigurator.fragments.update;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.ClientsDiffUtil;
import com.kotofeya.mobileconfigurator.Downloader;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


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
        scannerFragmentVM.getTime().observe(getViewLifecycleOwner(), this::updateTime);
        return view;
    }

    private void updateTime(String s) {
        binding.scannerTimer.setText(s);
    }

    private void updateClients(List<String> strings) {
        List<String> oldClients = rvAdapter.getObjects().stream()
                .map(it -> it.getSsid()).collect(Collectors.toList());

        ClientsDiffUtil clientsDiffUtil = new ClientsDiffUtil(oldClients, strings);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(clientsDiffUtil);

        rvAdapter.setObjects(rvAdapter.getObjects().stream().
                filter(it -> strings.contains(it.getIp())).collect(Collectors.toList()));
        diffResult.dispatchUpdatesTo(rvAdapter);
    }

    private void updateFilesTv(){
        StringBuilder sb = new StringBuilder();
        new LinkedList<>(Arrays.asList(App.get().getUpdateCoreFilesPath()))
                .forEach(it -> sb.append(it.getName()).append("\n"));
        binding.downloadCoreUpdateFilesTv.setText(sb.toString());
    }

    @Override
    public void onTaskCompleted(Bundle result) {

        binding.scannerProgressBar.setVisibility(View.GONE);

        int resultCode = result.getInt(BundleKeys.RESULT_CODE_KEY);
        String res = result.getString("result");
        String ip = result.getString(BundleKeys.IP_KEY);

        if(resultCode == Downloader.UPDATE_CORE_DOWNLOAD_CODE){
            Toast.makeText(getActivity(), "Файлы, необходимые для обновления скачаны", Toast.LENGTH_SHORT).show();
            StringBuilder sb = new StringBuilder();
            new LinkedList<>(Arrays.asList(Downloader.tempUpdateCoreFiles)).forEach(it -> sb.append(it.getName()).append("\n"));
            binding.downloadCoreUpdateFilesTv.setText(sb.toString());
        }
        if(resultCode == TaskCode.DOWNLOADER_ERROR_CODE){
            Toast.makeText(requireActivity(), "При загрузке файлов произошла ошибка", Toast.LENGTH_SHORT).show();
        }

        if(res != null && res.contains("загружен")){
            clearTextLabel();
            stopTimer();
            startTimer();

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
        Logger.d(TAG, "updateUI()");
        super.updateUI(transceivers);
        stopTimer();
    }


    @Override
    public void clearTextLabel(){
        Logger.d(Logger.UPDATE_CORE_LOG, "clearTextLabel()");

        requireActivity().runOnUiThread(() -> {
            binding.progressTv.setVisibility(View.GONE);
            binding.progressTv.setText("");
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
        binding.scannerTimer.setVisibility(View.VISIBLE);
    }

    public void stopTimer(){
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
                showChooseFileDialog(ip, ssid);
//                showConfirmDialog(ip, ssid);
            } else {
                fragmentHandler.showMessage("Please, download files for update from server");
            }
        }
    }

    private void showChooseFileDialog(String ip, String ssid){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Select the file to upload");

        String[] array = new String[Downloader.tempUpdateCoreFiles.length];
        for(int i = 0; i < Downloader.tempUpdateCoreFiles.length; i++){
            array[i] = Downloader.tempUpdateCoreFiles[i].getName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter(requireActivity(),
                android.R.layout.select_dialog_item, array);

        DialogInterface.OnClickListener myClickListener = (dialog, which) -> uploadFile(ip, ssid, which);
        builder.setAdapter(adapter, myClickListener);
        builder.setCancelable(true);
        builder.show();
    }


    class CountDownRunner implements Runnable {
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    doWork();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    binding.scannerTimer.setVisibility(View.GONE);
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
        binding.progressTv.setVisibility(View.VISIBLE);
        binding.progressTv.setText(text);
    }


    public void uploadFile(String ip, String serial, int coreUpdateIteration){
        setProgressTvText(getProgressTvText(serial, coreUpdateIteration));
        SshConnection connection = new SshConnection(this, this, coreUpdateIteration, ip);
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

}