package com.kotofeya.mobileconfigurator.domain.transceiver;

public class ClearTransceiversUseCase {
    TransceiverRepository transceiverRepository;

    public ClearTransceiversUseCase(TransceiverRepository transceiverRepository) {
        this.transceiverRepository = transceiverRepository;
    }

    public void clearTransceivers(){
        transceiverRepository.clearTransceivers();
    }
}
