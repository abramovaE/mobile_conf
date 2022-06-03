package com.kotofeya.mobileconfigurator.clientsHandler;

import android.os.Bundle;
import android.os.Parcelable;

import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.InternetConn;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.SshConnectionRunnable;
import com.kotofeya.mobileconfigurator.activities.CustomViewModel;
import com.kotofeya.mobileconfigurator.activities.InterfaceUpdateListener;
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

    private static final String TAG = ClientsHandler.class.getSimpleName();
    private static ClientsHandler instance;
    private InterfaceUpdateListener scanClientsListener;
    private final InternetConn internetConnection;
    private int futureCounter;
    private ExecutorService executorService;
    private ExecutorService versionExecutorService;
    private CompletableFuture<Void>[] futures;
    private final CustomViewModel viewModel;
    private boolean isScanning = false;

    public void stopScanning(){
        if(executorService != null) {
            executorService.shutdown();
        }
        if(versionExecutorService != null) {
            versionExecutorService.shutdown();
        }
        isScanning = false;
        Logger.d(TAG, "stopScanning");
        scanClientsListener.clientsScanFinished();
    }

    public static ClientsHandler getInstance(CustomViewModel viewModel) {
        if(instance == null){
            instance = new ClientsHandler(viewModel);
        }
        return instance;
    }

    private ClientsHandler(CustomViewModel viewModel) {
        clients = new CopyOnWriteArrayList<>();
        internetConnection = new InternetConn();
        this.viewModel = viewModel;
    }

    private List<String> clients;

    public List<String> getClients() {
        return clients;
    }

    public InternetConn getInternetConnection() {
        return internetConnection;
    }

    public void updateClients(InterfaceUpdateListener interfaceUpdateListener){
        if(!isScanning) {
            this.scanClientsListener = interfaceUpdateListener;
            String deviceIp = internetConnection.getDeviceIp();
            if (deviceIp != null) {
                isScanning = true;
                WiFiLocalHotspot.getInstance().updateClientList(deviceIp, this);
            } else {
                clients = new ArrayList<>();
            }
        }
    }

    @Override
    public void scanFinished(List<String> clients) {
        Logger.d(TAG, "scanFinished()");
        isScanning = false;
        this.clients = clients;
        viewModel.setClients(clients);
        scanClientsListener.clientsScanFinished();
    }

    public void getTakeInfo(){
        if(!isScanning) {
            viewModel.setTakeInfoFinished(false);
            Logger.d(TAG, "get take info");
            this.futureCounter = 0;
            Logger.d(TAG, "clients: " + clients);
            if (clients.size() > 0) {
                executorService = Executors.newCachedThreadPool();
                versionExecutorService = Executors.newCachedThreadPool();
                futures = new CompletableFuture[clients.size()];
                CompletableFuture<Void>[] verFutures = new CompletableFuture[clients.size()];
                for (int i = 0; i < clients.size(); i++) {
                    String ip = clients.get(i);
                    verFutures[i] = runGetPostVersion(ip);
                }
                CompletableFuture.allOf(verFutures).thenRun(() -> {
                    Logger.d(TAG, "verFutures is complete");
                    versionExecutorService.shutdown();

                    CompletableFuture.allOf(futures).whenComplete((a, ex) -> {
                        executorService.shutdown();
                        Logger.d(TAG, "finishedGetTakeInfo()");
                        viewModel.setTakeInfoFinished(true);
                    });
                });
            }
        }
    }

    private CompletableFuture<Void> runGetPostVersion(String ip){
        Logger.d(TAG, "runGetPostVersion(String ip), ip: " + ip);
        return CompletableFuture.runAsync(new PostInfo(this, ip, PostCommand.VERSION), versionExecutorService);
    }
    private CompletableFuture<Void> runSShTakeInfo(String ip){
        Logger.d(TAG, "runSShTakeInfo(String ip), ip: " + ip);
        return CompletableFuture.runAsync(new SshConnectionRunnable(this, ip, SshConnection.TAKE_CODE), executorService);
    }
    private CompletableFuture<Void> runPostTakeInfo(String ip, String version){
        Logger.d(TAG, "runPostTakeInfo(String ip), ip: " + ip);
        return CompletableFuture.runAsync(new PostInfo(this, ip, PostCommand.TAKE_INFO_FULL, version), executorService);
    }

    @Override
    public void onTaskCompleted(Bundle result) {

        String command = result.getString(BundleKeys.COMMAND_KEY);
        String response = result.getString(BundleKeys.RESPONSE_KEY);
        String ip = result.getString(BundleKeys.IP_KEY);
        Parcelable parcelableResponse = result.getParcelable(BundleKeys.PARCELABLE_RESPONSE_KEY);


        Logger.d(TAG, "onTaskCompleted(Bundle result), command: " + command + ", ip: " + ip + ", futures: " + futures.length);



        if(command == null){
            command = "";
        }
        switch (command){
            case PostCommand.VERSION:
                if(response != null){
//                    Logger.d(TAG, "new post info: " + ip + ", version: " + response);
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
                Logger.d(TAG, "version: " + version  + ", ip: " + ip);
                viewModel.addTakeInfoFull(ip, version, (TakeInfoFull) parcelableResponse, true);
                break;
            case SshCommand.SSH_TAKE_COMMAND:
                viewModel.addTakeInfo(response, true);
                break;
            case SshCommand.SSH_COMMAND_ERROR:
                Logger.d(TAG, "response: " + response);
                if (response.contains("Connection refused") || response.contains("Auth fail")) {
                    removeClient(ip);
                }
                break;
        }

    }

    public void removeClient(String ip){
        Logger.d(TAG, "remove: " + ip);
        clients.remove(ip);
        viewModel.setClients(clients);
    }

    public void clearClients(){
        clients.clear();
    }
}
