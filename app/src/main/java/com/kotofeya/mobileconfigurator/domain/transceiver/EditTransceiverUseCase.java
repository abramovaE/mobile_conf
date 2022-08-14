package com.kotofeya.mobileconfigurator.domain.transceiver;

public class EditTransceiverUseCase {
    TransceiverRepository transceiverRepository;

    public EditTransceiverUseCase(TransceiverRepository transceiverRepository) {
        this.transceiverRepository = transceiverRepository;
    }

    public void editTransceiver(Transceiver transceiver){
        transceiverRepository.editTransceiver(transceiver);
    }
}
