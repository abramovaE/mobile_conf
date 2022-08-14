package com.kotofeya.mobileconfigurator.domain.client;

import java.util.List;

public class AddClientsUseCase {
    ClientRepository clientRepository;

    public AddClientsUseCase(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public void addClients(List<String> clients){
        clientRepository.addClients(clients);
    }
}
