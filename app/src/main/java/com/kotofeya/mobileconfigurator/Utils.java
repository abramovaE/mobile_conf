package com.kotofeya.mobileconfigurator;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.activities.CustomViewModel;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;
import com.kotofeya.mobileconfigurator.newBleScanner.CustomBluetooth;
import com.kotofeya.mobileconfigurator.newBleScanner.CustomScanResult;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Utils implements OnTaskCompleted{
    public static final int TRANSP_RADIO_TYPE = 0x80;
    public static final int STAT_RADIO_TYPE = 0x40;
    public static final int ALL_RADIO_TYPE = 0;

    private List<String> clients;
    private int radioType;
    private InternetConn internetConnection;
    private ExecutorService executorService;
    private ExecutorService versionExecutorService;
    private CompletableFuture<Void>[] futures;
    private int futureCounter;
    private Context context;
    private CustomViewModel viewModel;
    private CustomBluetooth newBleScanner;
    private Thread thread;

    public Utils(Context context, CustomBluetooth newBleScanner) {
        this.context = context;
        this.viewModel = ViewModelProviders.of((MainActivity)context, new CustomViewModel.ModelFactory()).get(CustomViewModel.class);
        clients = new CopyOnWriteArrayList<>();
        internetConnection = new InternetConn();
        this.newBleScanner = newBleScanner;
    }

    public void getTakeInfo(){
        Logger.d(Logger.UTILS_LOG, "get take info");
        this.futureCounter = 0;
        String deviceIp = internetConnection.getDeviceIp();
        if(deviceIp != null){
            clients = WiFiLocalHotspot.getInstance().getClientList(deviceIp);
            clients.remove(deviceIp);
            Logger.d(Logger.UTILS_LOG, "clients: " + clients);
            if (clients.size() > 0) {
                executorService = Executors.newCachedThreadPool();
                versionExecutorService = Executors.newCachedThreadPool();
                futures = new CompletableFuture[clients.size()];
                CompletableFuture<Void>[] versFutures = new CompletableFuture[clients.size()];
                for (int i = 0; i < clients.size(); i++) {
                    String ip = clients.get(i);
                    versFutures[i] = CompletableFuture.runAsync(new PostInfo(this, ip, PostCommand.VERSION), versionExecutorService);
                }
                if(versFutures != null){
                    CompletableFuture.allOf(versFutures).thenRun(() -> {
                        versionExecutorService.shutdown();
                        return;
                    });
                }
            }
        }
    }

    public int getRadioType() {
        return radioType;
    }

    public void setRadioType(int radioType) {
        this.radioType = radioType;
    }

    public void removeClient(String ip){
        Logger.d(Logger.UTILS_LOG, "remove: " + ip);
        clients.remove(ip);
    }

    public void clearClients(){
        clients.clear();
    }
    public void clearMap(){ viewModel.clearMap(); }

    public static String byteArrayToBitString(byte b) {
        return String.format("%8s", Integer.toBinaryString(b)).replace(' ', '0');
    }

    public String getIp(String ssid){
        return viewModel.getIp(ssid);
    }
    public String getVersion(String ssid){
        return viewModel.getVersion(ssid);
    }


    public boolean needScanStationaryTransivers() {
        if(viewModel.getTransivers().getValue().size() == 0){
            return true;
        }
        for(Transiver t: viewModel.getTransivers().getValue()){
            if(t.isStationary() || !t.isTransport()){
                if(t.getIp() == null || viewModel.getIp(t.getSsid()) == null){
                    Logger.d(Logger.UTILS_LOG, "needScanStationaryTransivers: " + true);
                    return true;
                }
            }
        }
        Logger.d(Logger.UTILS_LOG, "needScanStationaryTransivers: " + false);
        return false;
    }

    @Override
    public void onTaskCompleted(Bundle result) {
        Logger.d(Logger.UTILS_LOG, "on task completed, result: " + result);
        String command = result.getString(PostInfo.COMMAND);
        String ip = result.getString(PostInfo.IP);
        String response = result.getString(PostInfo.RESPONSE);
        Logger.d(Logger.UTILS_LOG, "command: " + command);
        Logger.d(Logger.UTILS_LOG, "ip: " + ip);
        Logger.d(Logger.UTILS_LOG, "response: " + response);



        switch (command){
            case PostCommand.VERSION:
//                transiver.setVersion(response);
                // TODO: 29.04.2021 проверка версии
                if(response != null){
                    Logger.d(Logger.UTILS_LOG, "new post info: " + ip + ", version: " + response);
                    futures[futureCounter] = CompletableFuture.runAsync(new PostInfo((MainActivity)context, ip, PostCommand.TAKE_INFO_FULL, response), executorService);
                } else {
                    futures[futureCounter] = CompletableFuture.runAsync(new SshConnectionRunnable((MainActivity)context, ip, SshConnection.TAKE_CODE), executorService);
                }
                futureCounter += 1;
                break;

            case PostCommand.POST_COMMAND_ERROR:
                futures[futureCounter] = CompletableFuture.runAsync(new SshConnectionRunnable((MainActivity)context, ip, SshConnection.TAKE_CODE), executorService);
                futureCounter += 1;
                break;

        }
        if(futures != null){
            CompletableFuture.allOf(futures).thenRun(() -> {
                executorService.shutdown();
                return;
            });
        }
    }

    @Override
    public void onProgressUpdate(Integer downloaded) {
    }

    @Override
    public void setProgressBarVisible() {

    }

    @Override
    public void setProgressBarGone() {

    }

    @Override
    public void clearProgressBar() {

    }


    public static class MessageDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            String message = getArguments().getString("message");
            builder.setMessage(message);
            builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            builder.setCancelable(true);
            return builder.create();
        }
    }

    public void showMessage(String message){
        Bundle bundle = new Bundle();
        bundle.putString("message", message);
        Utils.MessageDialog dialog = new Utils.MessageDialog();
        dialog.setArguments(bundle);
        dialog.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().CONFIRMATION_DIALOG_TAG);
    }

    public InternetConn getInternetConnection() {
        return internetConnection;
    }


    public void startRvTimer(){
        if(thread != null){
            thread.interrupt();
        } thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if(newBleScanner.isScanning()) {
                        List<CustomScanResult> results = newBleScanner.getResults();
                        Logger.d(Logger.UTILS_LOG, "results: " + results.size());
                        viewModel.updateResults(results);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        });
        thread.start();
    }

    public CustomBluetooth getNewBleScanner() {
        return newBleScanner;
    }
}
