package com.kotofeya.mobileconfigurator.domain.client;

public class ClearClientsUseCase {
    ClientRepository clientRepository;

    public ClearClientsUseCase(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public void clearClients(){
        clientRepository.clearClients();
    }
}
