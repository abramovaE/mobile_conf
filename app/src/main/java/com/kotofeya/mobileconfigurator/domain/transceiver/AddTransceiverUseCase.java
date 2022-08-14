package com.kotofeya.mobileconfigurator.domain.transceiver;

public class AddTransceiverUseCase {
    TransceiverRepository transceiverRepository;

    public AddTransceiverUseCase(TransceiverRepository transceiverRepository) {
        this.transceiverRepository = transceiverRepository;
    }

    public void addTransceiver(Transceiver transceiver){
        transceiverRepository.addTransceiver(transceiver);
    }
}
