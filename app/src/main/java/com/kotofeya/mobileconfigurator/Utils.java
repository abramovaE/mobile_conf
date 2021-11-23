package com.kotofeya.mobileconfigurator;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.activities.CustomViewModel;
import com.kotofeya.mobileconfigurator.activities.InterfaceUpdateListener;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.clientsHandler.ClientsHandler;
import com.kotofeya.mobileconfigurator.hotspot.DeviceScanListener;
import com.kotofeya.mobileconfigurator.hotspot.WiFiLocalHotspot;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;
import com.kotofeya.mobileconfigurator.network.SshCommand;
import com.kotofeya.mobileconfigurator.network.post_response.TakeInfoFull;
import com.kotofeya.mobileconfigurator.newBleScanner.CustomBluetooth;
import com.kotofeya.mobileconfigurator.newBleScanner.CustomScanResult;
import com.kotofeya.mobileconfigurator.user.UserFactory;
import com.kotofeya.mobileconfigurator.user.UserType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Utils  {
    public static final int TRANSP_RADIO_TYPE = 0x80;
    public static final int STAT_RADIO_TYPE = 0x40;
    public static final int ALL_RADIO_TYPE = 0;

    public static final String TITLE_SCAN_CLIENTS = "";
    public static final String MESSAGE_SCAN_CLIENTS = "Поиск подключенных клиентов";

    public static final String TITLE_TAKE_INFO = "";
    public static final String MESSAGE_TAKE_INFO = "Опрос подключенных трансиверов";

    private int radioType;
//    private InternetConn internetConnection;


    private Context context;
    private CustomViewModel viewModel;
    private CustomBluetooth newBleScanner;
    private Thread thread;

    private InterfaceUpdateListener interfaceUpdateListener;


    public Utils(Context context, CustomBluetooth newBleScanner) {
        this.context = context;
        this.viewModel = ViewModelProviders.of((MainActivity)context, new CustomViewModel.ModelFactory()).get(CustomViewModel.class);
//        clients = new CopyOnWriteArrayList<>();
//        internetConnection = new InternetConn();
        this.newBleScanner = newBleScanner;
    }




    public void setRadioType(int radioType) {
        this.radioType = radioType;
    }

//    public void removeClient(String ip){
//        Logger.d(Logger.UTILS_LOG, "remove: " + ip);
//        ClientsHandler.getInstance(context).getClients().remove(ip);
//    }

    public void clearClients(){
        ClientsHandler.getInstance(context).getClients().clear();
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

    public void updateClients(InterfaceUpdateListener interfaceUpdateListener){
        ClientsHandler.getInstance(context).updateClients(interfaceUpdateListener);
    }
    public void getTakeInfo(InterfaceUpdateListener interfaceUpdateListener){
        Logger.d(Logger.UTILS_LOG, "getTakeInfo(InterfaceUpdateListener interfaceUpdateListener)");
        ClientsHandler.getInstance(context).getTakeInfo(interfaceUpdateListener);
    }

//        @Override
//    public void onTaskCompleted(Bundle result) {
////        Logger.d(Logger.UTILS_LOG, "onTaskCompleted(), futures: " + futures.length);
//        String command = result.getString(BundleKeys.COMMAND_KEY);
//        String ip = result.getString(BundleKeys.IP_KEY);
//        String response = result.getString(BundleKeys.RESPONSE_KEY);
//        Parcelable parcelableResponse = result.getParcelable(BundleKeys.PARCELABLE_RESPONSE_KEY);
//
//        if(command == null){
//            command = "";
//        }
//
//        Logger.d(Logger.UTILS_LOG, "on task completed, result: " + result);
//
//        switch (command){
////            case PostCommand.VERSION:
//////                transiver.setVersion(response);
////                // TODO: 29.04.2021 проверка версии
////                if(response != null){
////                    Logger.d(Logger.UTILS_LOG, "new post info: " + ip + ", version: " + response);
////                    futures[futureCounter] = runPostTakeInfo(ip, response);
////                } else {
////                    futures[futureCounter] = runSShTakeInfo(ip);
////                }
////                futureCounter += 1;
////                break;
//
////            case PostCommand.POST_COMMAND_ERROR:
////                futures[futureCounter] = runSShTakeInfo(ip);
////                futureCounter += 1;
////                break;
//
////            case PostCommand.TAKE_INFO_FULL:
////                String version = result.getString(BundleKeys.VERSION_KEY);
////                Logger.d(Logger.MAIN_LOG, "version: " + version);
////                viewModel.addTakeInfoFull(ip, version, (TakeInfoFull) parcelableResponse, true);
////                break;
//
////            case SshCommand.SSH_TAKE_COMMAND:
////                viewModel.addTakeInfo(response, true);
////                break;
////
////            case SshCommand.SSH_COMMAND_ERROR:
////                if (response.contains("Connection refused") || response.contains("Auth fail")) {
////                    removeClient(ip);
////                } else {
////                    showMessage("Error: " + response);
////                }
////                break;
//
//        }
////        if(futures != null){
////            CompletableFuture.allOf(futures).thenRun(() -> {
////                executorService.shutdown();
////                Logger.d(Logger.UTILS_LOG, "finishedGetTakeInfo()");
////                interfaceUpdateListener.finishedGetTakeInfo();
////                return;
////            });
////        }
//    }

//    @Override
//    public void onProgressUpdate(Integer downloaded) {
//    }



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
        return ClientsHandler.getInstance(context).getInternetConnection();
    }
    public void removeClient(String ip){
        Logger.d(Logger.UTILS_LOG, "remove: " + ip);
        ClientsHandler.getInstance(context).removeClient(ip);
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
                            Thread.sleep(2000);
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

    private String getTransportFileKey(String s, boolean isInternetEnabled){
        if(isInternetEnabled){
            return s.substring(0, s.indexOf("/"));
        } else {
            return s.substring(s.lastIndexOf("/") + 1).split("_")[0];
        }
    }

    public Map<String, String> getTransportContent(){
        Map<String, String> transportContent = new HashMap<>();
        boolean isInternetEnabled = getInternetConnection().hasInternetConnection();
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

    public AlertDialog.Builder getScanClientsDialog(){
        return new AlertDialog.Builder(context)
                .setTitle(TITLE_SCAN_CLIENTS)
                .setMessage(MESSAGE_SCAN_CLIENTS);
    }

    public AlertDialog.Builder getTakeInfoDialog(){
        Logger.d(Logger.UTILS_LOG, "getTakeInfoDialog()");
        return new AlertDialog.Builder(context)
                .setTitle(TITLE_TAKE_INFO)
                .setMessage(MESSAGE_TAKE_INFO);
    }
}
