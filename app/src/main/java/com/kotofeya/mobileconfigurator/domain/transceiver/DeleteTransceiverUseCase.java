package com.kotofeya.mobileconfigurator.domain.transceiver;

public class DeleteTransceiverUseCase {

    TransceiverRepository transceiverRepository;

    public DeleteTransceiverUseCase(TransceiverRepository transceiverRepository) {
        this.transceiverRepository = transceiverRepository;
    }

    public void deleteTransceiver(Transceiver transceiver){
        transceiverRepository.deleteTransceiver(transceiver);
    }
}
