package com.kotofeya.mobileconfigurator.domain.transceiver;

import java.util.List;

public class UpdateTransceiversWithClientsUseCase {
    TransceiverRepository transceiverRepository;

    public UpdateTransceiversWithClientsUseCase(TransceiverRepository transceiverRepository) {
        this.transceiverRepository = transceiverRepository;
    }

    public void updateTransceiversWithClients(List<String> clients){
        transceiverRepository.updateTransceiversWithClients(clients);
    }
}
