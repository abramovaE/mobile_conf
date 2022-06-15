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
    private int futureCounter;
    private ExecutorService executorService;
    private ExecutorService versionExecutorService;
    private CompletableFuture<Void>[] futures;
    private final CustomViewModel viewModel;
    private boolean isScanning = false;

//    list of clients ip
    private List<String> clients;
    private List<Client> clientsList;


    public void stopScanning(){
        Logger.d(TAG, "stopScanning()");
        if(executorService != null) {
            executorService.shutdown();
        }
        if(versionExecutorService != null) {
            versionExecutorService.shutdown();
        }
        isScanning = false;
        viewModel.setClientsScanning(false);
    }

    public static ClientsHandler getInstance(CustomViewModel viewModel) {
        if(instance == null){
            instance = new ClientsHandler(viewModel);
        }
        return instance;
    }

    private ClientsHandler(CustomViewModel viewModel) {
        clients = new CopyOnWriteArrayList<>();
        this.viewModel = viewModel;
        clientsList = new ArrayList<>();
    }

    private void updateClients(boolean isNeedPoll){
        if(!isScanning) {
            clientsList.clear();
            isScanning = true;
            viewModel.setClientsScanning(true);
            String deviceIp = InternetConn.getDeviceIp();
            Logger.d(TAG, "devIp: " + deviceIp);
            if (deviceIp != null) {
                WiFiLocalHotspot.getInstance().updateClientList(deviceIp, this, isNeedPoll);
            } else {
                clients = new ArrayList<>();
                updateClientsFinished(clients);
            }
        }
    }

    private void updateClientsFinished(List<String> clients){
        isScanning = false;
        this.clients = clients;
        viewModel.setClients(clients);
        viewModel.setClientsScanning(false);
    }

    @Override
    public void pingClientsFinished(List<String> clients, boolean isNeedPoll) {
        Logger.d(TAG, "pingClientsFinished()");
        updateClientsFinished(clients);
        if(isNeedPoll) {
            pollConnectedClients();
        } else {

        }
    }

    public void pollConnectedClients(){
        Logger.d(TAG, "pollClients()");
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
                CompletableFuture.allOf(verFutures).whenComplete((a0, ex0) -> {
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
        return CompletableFuture.runAsync(
                new PostInfo(this, ip, PostCommand.VERSION),
                versionExecutorService);
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
        Logger.d(TAG, "onTaskCompleted(Bundle result)");
        String command = result.getString(BundleKeys.COMMAND_KEY);
        String response = result.getString(BundleKeys.RESPONSE_KEY);
        String ip = result.getString(BundleKeys.IP_KEY);
        String errorMessage = result.getString(BundleKeys.ERROR_MESSAGE);

        Parcelable parcelableResponse = result.getParcelable(BundleKeys.PARCELABLE_RESPONSE_KEY);
        Logger.d(TAG, "onTaskCompleted(Bundle result), command: " + command + ", ip: " + ip + ", futures: " + futures.length);
        if(command == null){
            command = "";
        }

//        if(errorMessage != null || !errorMessage.isEmpty()){
//            Client client = new Client(ip);
//            clientsList.add(client);
//        } else {
            switch (command) {
                case PostCommand.VERSION:
                    if (response != null) {
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
//                    Logger.d(TAG, "version: " + version + ", ip: " + ip);
//                    Client client = clientsList.stream().filter(it -> it.getIp().equals(ip)).findAny().orElse(null);
//                    if (client != null) {
//                        client.setVersion(version);
//                        Transiver transiver = createTakeInfoFullTransceiver(ip, version, (TakeInfoFull) parcelableResponse);
//                        client.setTransiver(transiver);
//                    }
//                    clientsList.add(client);
                viewModel.addTakeInfoFull(ip, version, (TakeInfoFull) parcelableResponse);
                    break;
                case SshCommand.SSH_TAKE_COMMAND:
//                    Client c = clientsList.stream().filter(it -> it.getIp().equals(ip)).findAny().orElse(null);
//                    if (c != null) {
//                        c.setVersion("");
//                        Transiver transiver = createTakeInfoTransceiver(ip, "", response);
//                        c.setTransiver(transiver);
//                    }
//                    clientsList.add(c);
                viewModel.addTakeInfo(response);
                    break;
                case SshCommand.SSH_COMMAND_ERROR:
                    Logger.d(TAG, "response: " + response);
//                if (response.contains("Connection refused") || response.contains("Auth fail")) {
                    removeClient(ip);
//                }
                    break;
//            }
        }

//        viewModel.setWifiClients(clientsList);
    }

    public void removeClient(String ip){
        Logger.d(TAG, "remove: " + ip);
        clients.remove(ip);
        viewModel.setClients(clients);
    }

    public void clearClients(){
        clients.clear();
    }

//
//    private Transiver createTakeInfoFullTransceiver(String ip, String version, TakeInfoFull takeInfoFull){
//        String ssid = takeInfoFull.getSerial() + "";
//        ssid = Transiver.formatSsid(ssid);
//        Transiver transiver = new Transiver(ip);
//        transiver.setTakeInfoFull(takeInfoFull);
//        transiver.setVersion(version);
//        transiver.setSsid(ssid);
//        return transiver;
//    }
//
//    public Transiver createTakeInfoTransceiver(String ipT, String version, String takeInfo){
//        String[] info = takeInfo.split("\n");
//        String ssid = info[1].trim();
//        String ip = info[2].trim();
//        String macWifi = info[3].trim();
//        String macBt = info[4].trim();
//        String boardVersion = info[5].trim();
//        String osVersion = info[6].trim();
//        String stmFirmware = info[7].trim();
//        String stmBootloader = info[8].trim();
//        String core = info[9].trim();
//        String modem = info[10].trim();
//        String incrementOfContent = info[11].trim();
//        String uptime = info[12].trim();
//        String cpuTemp = info[13].trim();
//        String load = info[14].trim();
//        String tType = info[17].trim();
//        Transiver t = new Transiver(ip);
//        t.setSsid(ssid);
//        t.setIp(ip);
//        t.setMacWifi(macWifi);
//        t.setMacBt(macBt);
//        t.setBoardVersion(boardVersion);
//        t.setOsVersion(osVersion);
//        t.setStmFirmware(stmFirmware);
//        t.setStmBootloader(stmBootloader);
//        t.setCore(core);
//        t.setModem(modem);
//        t.setIncrementOfContent(incrementOfContent);
//        t.setUptime(uptime);
//        t.setCpuTemp(cpuTemp);
//        t.setLoad(load);
//        t.setTType(tType);
//        return t;
//    }



    public void updateConnectedClients(){
        updateClients(false);
    }

    public void updateAndPollConnectedClients(){
        updateClients(true);
    }

}
