package com.kotofeya.mobileconfigurator.activities;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.data.TransceiverRepositoryImpl;
import com.kotofeya.mobileconfigurator.data.client.ClientsHandlerOkHttp;
import com.kotofeya.mobileconfigurator.domain.transceiver.ClearTransceiversUseCase;
import com.kotofeya.mobileconfigurator.domain.transceiver.DeleteTransceiverUseCase;
import com.kotofeya.mobileconfigurator.domain.transceiver.EditTransceiverUseCase;
import com.kotofeya.mobileconfigurator.domain.transceiver.GetTransceiverByIpUseCase;
import com.kotofeya.mobileconfigurator.domain.transceiver.GetTransceiverBySerialUseCase;
import com.kotofeya.mobileconfigurator.domain.transceiver.GetTransceiverListUseCase;
import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;
import com.kotofeya.mobileconfigurator.domain.transceiver.UpdateTransceiversWithClientsUseCase;

import java.util.List;
import java.util.stream.Collectors;

public class MainActivityViewModel extends ViewModel {

    private static final String TAG = MainActivityViewModel.class.getSimpleName();

    private final TransceiverRepositoryImpl transceiverRepositoryImpl =
            TransceiverRepositoryImpl.getInstance();
    private final GetTransceiverBySerialUseCase getTransceiverBySerialUseCase =
            new GetTransceiverBySerialUseCase(transceiverRepositoryImpl);
    private final GetTransceiverByIpUseCase getTransceiverByIpUseCase =
            new GetTransceiverByIpUseCase(transceiverRepositoryImpl);
    private final GetTransceiverListUseCase getTransceiverListUseCase =
            new GetTransceiverListUseCase(transceiverRepositoryImpl);
    private final EditTransceiverUseCase editTransceiverUseCase =
            new EditTransceiverUseCase(transceiverRepositoryImpl);
    private final DeleteTransceiverUseCase deleteTransceiverUseCase =
            new DeleteTransceiverUseCase(transceiverRepositoryImpl);
    private final ClearTransceiversUseCase clearTransceiversUseCase =
            new ClearTransceiversUseCase(transceiverRepositoryImpl);
    private final UpdateTransceiversWithClientsUseCase updateTransceiversWithClientsUseCase =
            new UpdateTransceiversWithClientsUseCase(transceiverRepositoryImpl);

    private final ClientsHandlerOkHttp clientsHandler = ClientsHandlerOkHttp.getInstance();

    public LiveData<List<Transceiver>> transceivers =
            getTransceiverListUseCase.getTransceiverList();
    public LiveData<List<String>> clients = clientsHandler.getClients();

    public LiveData<Boolean> isScanning = clientsHandler.isScanningLiveData;
    public LiveData<Boolean> isGetTakeInfoFinished = clientsHandler.isTakeInfoFinished;

    public LiveData<List<Transceiver>> getStationaryInformers() {
        return new MutableLiveData<>(
                transceivers.getValue().stream()
                        .filter(Transceiver::isStationary)
                        .collect(Collectors.toList()));
    }
    public LiveData<List<Transceiver>> getTranspInformers() {
        return new MutableLiveData<>(
                transceivers.getValue().stream()
                        .filter(Transceiver::isTransport)
                        .collect(Collectors.toList()));
    }

    public void updateTransceivers() {
        updateTransceiversWithClientsUseCase.updateTransceiversWithClients(clients.getValue());
    }

    public Transceiver getTransceiverByIp(String ip){
        Logger.d(TAG, "getTransceiverByIp(): " + ip);
        return getTransceiverByIpUseCase.getTransceiverByIp(ip);
    }

    public Transceiver getTransceiverBySsid(String ssid) {
        Logger.d(TAG, "getTransceiverBySerial(): " + ssid);
        return getTransceiverBySerialUseCase.getTransceiverBySerial(ssid);
    }

    public void removeTransceiver(Transceiver transceiver){
        Logger.d(TAG, "removeTransceiver()");
        deleteTransceiverUseCase.deleteTransceiver(transceiver);
    }

    public void clearTransceivers(){
        Logger.d(TAG, "clearTransceivers()");
        clearTransceiversUseCase.clearTransceivers();
    }


    private MutableLiveData<Boolean> _rescanPressed = new MutableLiveData<>();
    public LiveData<Boolean> isRescanPressed = _rescanPressed;
    public void rescanPressed(){_rescanPressed.postValue(true);}

    private MutableLiveData<Boolean> _isHotspotDialogShowing = new MutableLiveData<>();
    public LiveData<Boolean> isHotspotDialogShowing = _isHotspotDialogShowing;
    public void setIsHotspotDialogShowing(Boolean b){_isHotspotDialogShowing.postValue(b);}

    private MutableLiveData<String> _mainTxtLabel = new MutableLiveData<>("");
    public LiveData<String> mainTxtLabel(){return _mainTxtLabel;}
    public void setMainTxtLabel(String mainTxtLabel){
        _mainTxtLabel.postValue(mainTxtLabel);
    }

    private MutableLiveData<Integer> _mainBtnRescanVisibility = new MutableLiveData<Integer>();
    public LiveData<Integer> mainBtnRescanVisibility(){return  _mainBtnRescanVisibility;}
    public void setMainBtnRescanVisibility(int visibility){
        _mainBtnRescanVisibility.postValue(visibility);
    }

    private MutableLiveData<String> _time = new MutableLiveData<>("");
    public LiveData<String> time(){return _time;}
    public void setTime(String time) {_time.postValue(time);}

    public void setUpdatingTime(Transceiver transceiver, String time) {
        if(transceiver != null) {
            transceiver.setUpdatingTime(time);
            editTransceiverUseCase.editTransceiver(transceiver);
        }
    }

    private MutableLiveData<String> _transceiverSettingsText = new MutableLiveData<>("");
    public LiveData<String> transceiverSettingsText(){return _transceiverSettingsText;}
    public void setTransceiverSettingsText(String transceiverSettingsText){
        _transceiverSettingsText.postValue(transceiverSettingsText);
    }

    public void clearClients(){
        clientsHandler.clearClients();
    }

    public void updateConnectedClients(){
        clientsHandler.updateConnectedClients();
    }

    public void updateAndPollConnectedClients(){
        clientsHandler.updateAndPollConnectedClients();
    }

    public void stopClientsScanning(){
        clientsHandler.stopScanning();
    }

    public void pollConnectedClients(){
        clientsHandler.pollConnectedClients();
    }

    public void removeClient(String ip){
//        clientsHandler.removeClient(ip);
        setUpdatingAttr(ip);
    }

    public void setUpdatingAttr(String ip) {
        startUpdatingTimer(ip);
    }

    public void startUpdatingTimer(String ip){
        Transceiver transceiver = getTransceiverByIp(ip);
        if(transceiver != null) {
            stopUpdatingTimer(transceiver);
            Runnable runnable = new UpdatingTimer(this, transceiver);
            Thread updatingTimer = new Thread(runnable);
            transceiver.setUpdatingTimer(updatingTimer);
            updatingTimer.start();
        }
    }

    public void stopUpdatingTimer(Transceiver transceiver){
        Thread updatingTimer = transceiver.getUpdatingTimer();
        if(updatingTimer != null){
            updatingTimer.interrupt();
        }
        transceiver.setUpdatingTime(null);
    }
}