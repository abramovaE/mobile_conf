package com.kotofeya.mobileconfigurator.domain.transceiver;

public class GetTransceiverByIpUseCase {
    TransceiverRepository transceiverRepository;

    public GetTransceiverByIpUseCase(TransceiverRepository transceiverRepository) {
        this.transceiverRepository = transceiverRepository;
    }

    public Transceiver getTransceiverByIp(String ip){
        return transceiverRepository.getTransceiverByIp(ip);
    }
}
