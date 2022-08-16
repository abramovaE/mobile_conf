package com.kotofeya.mobileconfigurator.domain.transceiver;

import androidx.lifecycle.LiveData;

import java.util.List;

public interface TransceiverRepository {
    void addTransceiver(Transceiver transceiver);
    void editTransceiver(Transceiver transceiver);
    void deleteTransceiver(Transceiver transceiver);
    void clearTransceivers();
    Transceiver getTransceiverBySerial(String serial);
    Transceiver getTransceiverByIp(String ip);
    LiveData<List<Transceiver>> getTransceiverList();
    void updateTransceiversWithClients(List<String> clients);
}