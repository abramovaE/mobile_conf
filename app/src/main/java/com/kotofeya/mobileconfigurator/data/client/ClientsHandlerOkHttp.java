package com.kotofeya.mobileconfigurator.data.client;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.network.request.SshTakeInfoListener;
import com.kotofeya.mobileconfigurator.network.request.SshTakeInfoUseCase;
import com.kotofeya.mobileconfigurator.data.TransceiverRepositoryImpl;
import com.kotofeya.mobileconfigurator.domain.client.AddClientsUseCase;
import com.kotofeya.mobileconfigurator.domain.client.ClearClientsUseCase;
import com.kotofeya.mobileconfigurator.domain.client.DeleteClientUseCase;
import com.kotofeya.mobileconfigurator.domain.client.GetClientListUseCase;
import com.kotofeya.mobileconfigurator.domain.hotspot.DeviceScanListener;
import com.kotofeya.mobileconfigurator.domain.hotspot.WiFiLocalHotspot;
import com.kotofeya.mobileconfigurator.domain.transceiver.EditTransceiverUseCase;
import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.ResponseParser;
import com.kotofeya.mobileconfigurator.network.request.PostTakeInfoListener;
import com.kotofeya.mobileconfigurator.network.request.PostTakeInfoUseCase;
import com.kotofeya.mobileconfigurator.network.request.PostVersionListener;
import com.kotofeya.mobileconfigurator.network.request.PostVersionUseCase;
import com.kotofeya.mobileconfigurator.network.response.TakeInfoFull;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ClientsHandlerOkHttp implements DeviceScanListener,
        PostVersionListener,
        PostTakeInfoListener,
        SshTakeInfoListener {

    CountDownLatch latch;

    private static final String TAG = ClientsHandlerOkHttp.class.getSimpleName();
    private static ClientsHandlerOkHttp instance;

    private final ClientRepositoryImpl clientRepositoryImpl = ClientRepositoryImpl.getInstance();
    private final GetClientListUseCase getClientListUseCase =
            new GetClientListUseCase(clientRepositoryImpl);
    private final AddClientsUseCase addClientsUseCase =
            new AddClientsUseCase(clientRepositoryImpl);
    private final ClearClientsUseCase clearClientsUseCase =
            new ClearClientsUseCase(clientRepositoryImpl);
    private final DeleteClientUseCase deleteClientUseCase =
            new DeleteClientUseCase(clientRepositoryImpl);

    private final TransceiverRepositoryImpl transceiverRepositoryImpl
            = TransceiverRepositoryImpl.getInstance();
    private final EditTransceiverUseCase editTransceiverUseCase
            = new EditTransceiverUseCase(transceiverRepositoryImpl);

    public MutableLiveData<Boolean> isScanningLiveData = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> isTakeInfoFinished = new MutableLiveData<>(true);

    public static ClientsHandlerOkHttp getInstance() {
        if(instance == null){
            instance = new ClientsHandlerOkHttp();
        }
        return instance;
    }

    public void stopScanning(){
        Logger.d(TAG, "stopScanning()");
        isScanningLiveData.postValue(false);
    }

    private void updateClients(boolean isNeedPoll){
        Logger.d(TAG, "updateClients(), isScanning: " + isScanningLiveData.getValue());
        if(!isScanningLiveData.getValue()) {
            isScanningLiveData.postValue(true);
            WiFiLocalHotspot.getInstance().updateClientList(this, isNeedPoll);
        }
    }

    private void updateClientsFinished(List<String> clients){
        Logger.d(TAG, "updateClientsFinished()");
        isScanningLiveData.postValue(false);
        addClientsUseCase.addClients(clients);
    }

    @Override
    public void pingClientsFinished(List<String> clients, boolean isNeedPoll) {
        Logger.d(TAG, "pingClientsFinished()");
        updateClientsFinished(clients);
        if(isNeedPoll) {
            pollConnectedClients();
        }
    }

    public void pollConnectedClients(){
        List<String> clients = getClientListUseCase.getClientList().getValue();
        latch = new CountDownLatch(clients.size() * 2);
        isTakeInfoFinished.postValue(false);
        Logger.d(TAG, "pollConnectedClients(), clients: " + clients);
        if(!isScanningLiveData.getValue()) {
            Logger.d(TAG, "get take info");
            Logger.d(TAG, "clients: " + clients);

            if (clients.size() > 0) {
                for (int i = 0; i < clients.size(); i++) {
                    String ip = clients.get(i);
                    new PostVersionUseCase(this, ip).newRequest();
                }
            } else {
                isTakeInfoFinished.postValue(true);
            }
        }
    }


    private void runSShTakeInfo(String ip){
        Logger.d(TAG, "runSShTakeInfo(), ip: " + ip);
        new Thread(new SshTakeInfoUseCase(this, ip)).start();
    }

    public void removeClient(String ip){
        Logger.d(TAG, "removeClient(): " + ip);
        deleteClientUseCase.deleteClient(ip);
    }

    public void clearClients(){
        Logger.d(TAG, "clearClients()");
        clearClientsUseCase.clearClients();
    }

    public void updateConnectedClients(){
        updateClients(false);
    }

    public void updateAndPollConnectedClients(){
        updateClients(true);
    }

    public LiveData<List<String>> getClients(){
        return getClientListUseCase.getClientList();
    }

    private void addTransceiver(String ip,
                                String version,
                                TakeInfoFull takeInfoFull){
        Logger.d(TAG, "addTakeInfoFull(): " + takeInfoFull.getSerial()
                + ", version: " + version
                + ", ip: " + ip);
        Transceiver transceiver = new Transceiver(ip, version, takeInfoFull);
        editTransceiverUseCase.editTransceiver(transceiver);
    }

    private void addTransceiver(String takeInfo){
        Logger.d(TAG, "addTakeInfo(): " + takeInfo);
        Transceiver transceiver = new Transceiver(takeInfo);
        editTransceiverUseCase.editTransceiver(transceiver);
    }

    @Override
    public void postVersionSuccessful(String command, String ip, String response) {
        Logger.d(TAG, "postVersionSuccessful(), ip: " + ip + ", response: " + response);
        String version = ResponseParser.parseVersion(response);
        decrementLatch();
        new PostTakeInfoUseCase(this, ip, PostCommand.TAKE_INFO_FULL, version).newRequest();
    }

    @Override
    public void postVersionFailed(String command, String ip, String error) {
        Logger.d(TAG, "postVersionFailed(), ip: " + ip + ", error: " + error);
        decrementLatch();
        runSShTakeInfo(ip);
    }

    @Override
    public void postTakeInfoSuccessful(String command, String ip, String content, String version) {
        Logger.d(TAG, "postTakeInfoSuccessful(), ip: " + ip + " " + version);
        decrementLatch();
        double gsonVersion = getVersion(version);
        TakeInfoFull takeInfoFull = ResponseParser.parseTakeInfoFull(content, gsonVersion);
        addTransceiver(ip, version, takeInfoFull);
    }

    @Override
    public void postTakeInfoFailed(String command, String ip, String error) {
        Logger.d(TAG, "postTakeInfoFailed(), ip: " + ip + ", error: " + error);
//        removeClient(ip);
        decrementLatch();
    }

    private double getVersion(String version){
        Logger.d(TAG, "getVersion: " + version);
        if(version.startsWith("0")){
            return Double.parseDouble(version.replaceFirst("0.", ""));
        }
        return 0.0;
    }

    private void decrementLatch(){
        if(latch != null) {
            Logger.d(TAG, "decrementLatch(), latch: " + latch.getCount());
            latch.countDown();
            if (latch.getCount() == 0) {
                isTakeInfoFinished.postValue(true);
                latch = null;
            }
        }
    }

    @Override
    public void sshTakeInfoSuccessful(String response) {
        Logger.d(TAG, "sshTakeInfoSuccessful(): " + response);
        decrementLatch();
        addTransceiver(response);
    }

    @Override
    public void sshTakeInfoFailed(String ip, String error) {
        Logger.d(TAG, "sshTakeInfoFailed(): " + ip + " " + error);
        decrementLatch();
        removeClient(ip);
    }
}