package com.kotofeya.mobileconfigurator.domain.transceiver;

public class GetTransceiverBySerialUseCase {
    TransceiverRepository transceiverRepository;

    public GetTransceiverBySerialUseCase(TransceiverRepository transceiverRepository) {
        this.transceiverRepository = transceiverRepository;
    }

    public Transceiver getTransceiverBySerial(String serial){
        return transceiverRepository.getTransceiverBySerial(serial);
    }

}
