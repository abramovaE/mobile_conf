package com.kotofeya.mobileconfigurator.clientsHandler;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.InternetConn;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.SshConnectionRunnable;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.activities.CustomViewModel;
import com.kotofeya.mobileconfigurator.activities.InterfaceUpdateListener;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.hotspot.DeviceScanListener;
import com.kotofeya.mobileconfigurator.hotspot.WiFiLocalHotspot;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;
import com.kotofeya.mobileconfigurator.network.SshCommand;
import com.kotofeya.mobileconfigurator.network.post_response.TakeInfoFull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientsHandler implements DeviceScanListener, OnTaskCompleted {

    private static final String TAG = "ClientsHandler";
    private static ClientsHandler instance;
    private InterfaceUpdateListener interfaceUpdateListener;
    private InternetConn internetConnection;
    private int futureCounter;
    private ExecutorService executorService;
    private ExecutorService versionExecutorService;
    private CompletableFuture<Void>[] futures;
    private Context context;
    private CustomViewModel viewModel;


    public static synchronized ClientsHandler getInstance(Context context) {
        if(instance == null){
            instance = new ClientsHandler(context);
        }

        return instance;
    }
    private ClientsHandler(Context context) {
        clients = new CopyOnWriteArrayList<>();
        internetConnection = new InternetConn();
        this.context = context;
        this.viewModel = ViewModelProviders.of((MainActivity)context, new CustomViewModel.ModelFactory()).get(CustomViewModel.class);
    }

    private List<String> clients;

    public List<String> getClients() {
        return clients;
    }

    public InternetConn getInternetConnection() {
        return internetConnection;
    }

    public void updateClients(InterfaceUpdateListener interfaceUpdateListener){
        this.interfaceUpdateListener = interfaceUpdateListener;
        String deviceIp = internetConnection.getDeviceIp();
        if(deviceIp != null) {
            WiFiLocalHotspot.getInstance().updateClientList(deviceIp, this);
        } else {
            clients = new ArrayList<>();
        }
    }

    @Override
    public void scanFinished(List<String> clients) {
        this.clients = clients;
        viewModel.setClients(clients);
        interfaceUpdateListener.clientsScanFinished();
    }

    public void getTakeInfo(InterfaceUpdateListener interfaceUpdateListener){
        Logger.d(TAG, "get take info");
//        interfaceUpdateListener.startGetTakeInfo();
        this.futureCounter = 0;
        this.interfaceUpdateListener = interfaceUpdateListener;
//        interfaceUpdateListener.startGetTakeInfo();
        Logger.d(TAG, "clients: " + clients);
        if (clients.size() > 0) {
            executorService = Executors.newCachedThreadPool();
            versionExecutorService = Executors.newCachedThreadPool();
            futures = new CompletableFuture[clients.size()];
            CompletableFuture<Void>[] versFutures = new CompletableFuture[clients.size()];
//            interfaceUpdateListener.startGetTakeInfo();
            for (int i = 0; i < clients.size(); i++) {
                String ip = clients.get(i);
                versFutures[i] = runGetPostVersion(ip);
            }
            CompletableFuture.allOf(versFutures).thenRun(() -> {
                Logger.d(TAG, "versFutures is complete");
                versionExecutorService.shutdown();
                return;
            });
        }
    }

    private CompletableFuture runGetPostVersion(String ip){
        Logger.d(TAG, "runGetPostVersion(String ip), ip: " + ip);
        return CompletableFuture.runAsync(new PostInfo(this, ip, PostCommand.VERSION), versionExecutorService);
    }
    private CompletableFuture runSShTakeInfo(String ip){
        Logger.d(TAG, "runSShTakeInfo(String ip), ip: " + ip);
        return CompletableFuture.runAsync(new SshConnectionRunnable(this, ip, SshConnection.TAKE_CODE), executorService);
    }
    private CompletableFuture runPostTakeInfo(String ip, String version){
        Logger.d(TAG, "runPostTakeInfo(String ip), ip: " + ip);
        return CompletableFuture.runAsync(new PostInfo(this, ip, PostCommand.TAKE_INFO_FULL, version), executorService);
    }

    @Override
    public void onTaskCompleted(Bundle result) {
        String command = result.getString(BundleKeys.COMMAND_KEY);
        String response = result.getString(BundleKeys.RESPONSE_KEY);
        String ip = result.getString(BundleKeys.IP_KEY);
        Parcelable parcelableResponse = result.getParcelable(BundleKeys.PARCELABLE_RESPONSE_KEY);
        Logger.d(TAG, "onTaskCompleted(Bundle result), command: " + command);
        if(command == null){
            command = "";
        }
        switch (command){
            case PostCommand.VERSION:
                if(response != null){
                    Logger.d(TAG, "new post info: " + ip + ", version: " + response);
                    Logger.d(TAG, "new future take post: " + futureCounter);
                    futures[futureCounter++] = runPostTakeInfo(ip, response);
                } else {
                    Logger.d(TAG, "new future take ssh: " + futureCounter);
                    futures[futureCounter++] = runSShTakeInfo(ip);
                }
                break;
            case PostCommand.POST_COMMAND_ERROR:
                Logger.d(TAG, "new future take ssh: " + futureCounter);
                futures[futureCounter++] = runSShTakeInfo(ip);
                break;
            case PostCommand.TAKE_INFO_FULL:
                String version = result.getString(BundleKeys.VERSION_KEY);
                Logger.d(TAG, "version: " + version);
                viewModel.addTakeInfoFull(ip, version, (TakeInfoFull) parcelableResponse, true);
                break;
            case SshCommand.SSH_TAKE_COMMAND:
                viewModel.addTakeInfo(response, true);
                break;
            case SshCommand.SSH_COMMAND_ERROR:
                Logger.d(TAG, "response: " + response);
                if (response.contains("Connection refused") || response.contains("Auth fail")) {
                    removeClient(ip);
                } else {
                    showMessage("Error: " + response);
                }
                break;
        }
//        for (CompletableFuture<Void> f: futures){
//            Logger.d(TAG, "f is done: " + f.compl);
//            Logger.d(TAG, "f is exceptionally: " + f.isCompletedExceptionally());
//        }

        if(futures != null){
            CompletableFuture.allOf(futures).thenRun(() -> {
                executorService.shutdown();
                Logger.d(TAG, "finishedGetTakeInfo()");
                interfaceUpdateListener.finishedGetTakeInfo();
                return;
            });
        }
    }

    public void removeClient(String ip){
        Logger.d(TAG, "remove: " + ip);
        clients.remove(ip);
        viewModel.setClients(clients);
    }

    @Override
    public void onProgressUpdate(Integer downloaded) {

    }

    public void showMessage(String message){
        Bundle bundle = new Bundle();
        bundle.putString("message", message);
        Utils.MessageDialog dialog = new Utils.MessageDialog();
        dialog.setArguments(bundle);
        dialog.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().CONFIRMATION_DIALOG_TAG);
    }
}
