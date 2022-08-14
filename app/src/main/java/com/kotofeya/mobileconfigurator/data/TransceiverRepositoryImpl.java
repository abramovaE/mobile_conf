package com.kotofeya.mobileconfigurator.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;
import com.kotofeya.mobileconfigurator.domain.transceiver.TransceiverRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class TransceiverRepositoryImpl implements TransceiverRepository {

    private static final String TAG = TransceiverRepositoryImpl.class.getSimpleName();
    private static TransceiverRepositoryImpl instance;
    private TransceiverRepositoryImpl(){}
    public static TransceiverRepositoryImpl getInstance(){
        if(instance == null){
            instance = new TransceiverRepositoryImpl();
        }
        return instance;
    }

    private final List<Transceiver> transceiverList = new CopyOnWriteArrayList<>();
    private final MutableLiveData transceiverListLiveData =
            new MutableLiveData(new ArrayList<Transceiver>());

    @Override
    public void addTransceiver(Transceiver transceiver) {
        Logger.d(TAG, "addTransceiver()");
        transceiverList.add(transceiver);
        updateTransceiverListLiveData();
    }



    @Override
    public void editTransceiver(Transceiver transceiver) {
        Transceiver oldTransceiver = getTransceiverByIp(transceiver.getIp());
        if(oldTransceiver == null){
            oldTransceiver = getTransceiverBySerial(transceiver.getSsid());
        }
        if(transceiver != null) {
            transceiverList.remove(oldTransceiver);
        }
        addTransceiver(transceiver);
    }

    @Override
    public void deleteTransceiver(Transceiver transceiver) {
        Logger.d(TAG, "deleteTransceiver()");
        transceiverList.remove(transceiver);
        updateTransceiverListLiveData();
    }

    @Override
    public void clearTransceivers() {
        Logger.d(TAG, "clearTransceivers()");
        transceiverList.clear();
        updateTransceiverListLiveData();
    }

    @Override
    public Transceiver getTransceiverBySerial(String serial) {
        return transceiverList.stream()
                .filter(it -> it.getSsid().equals(serial))
                .findAny().orElse(null);
    }

    @Override
    public Transceiver getTransceiverByIp(String ip) {
        return transceiverList.stream()
                .filter(it->it.getIp().equals(ip))
                .findAny().orElse(null);
    }

    @Override
    public LiveData<List<Transceiver>> getTransceiverList() {
        Logger.d(TAG, "getTransceiverList()");
        return transceiverListLiveData;
    }

    @Override
    public void updateTransceiversWithClients(List<String> clients) {
        Logger.d(TAG, "updateTransceiversWithClients(): " + clients);
        List<Transceiver> newTransceivers = transceiverList
                .stream().filter(it -> clients.contains(it.getIp()))
                .collect(Collectors.toList());
        transceiverList.clear();
        transceiverList.addAll(newTransceivers);
        updateTransceiverListLiveData();
    }

    private Thread updatingTimer;

    @Override
    public void startTimer(Transceiver transceiver) {

    }

    @Override
    public void stopTimer(Transceiver transceiver) {

    }

    private void updateTransceiverListLiveData(){
        Logger.d(TAG, "updateTransceiverListLiveData()");
        transceiverListLiveData.postValue(transceiverList);
    }
}