package com.kotofeya.mobileconfigurator.domain.transceiver;

import androidx.lifecycle.LiveData;

import com.kotofeya.mobileconfigurator.Logger;

import java.util.List;

public class GetTransceiverListUseCase {
    private static final String TAG = GetTransceiverListUseCase.class.getSimpleName();
    TransceiverRepository transceiverRepository;

    public GetTransceiverListUseCase(TransceiverRepository transceiverRepository) {
        this.transceiverRepository = transceiverRepository;
    }

    public LiveData<List<Transceiver>> getTransceiverList(){
        Logger.d(TAG, "getTransceiverList()");
        return transceiverRepository.getTransceiverList();
    }

}
