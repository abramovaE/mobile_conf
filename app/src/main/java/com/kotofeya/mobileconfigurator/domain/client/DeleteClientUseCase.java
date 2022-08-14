package com.kotofeya.mobileconfigurator.domain.client;

public class DeleteClientUseCase {
    ClientRepository clientRepository;

    public DeleteClientUseCase(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public void deleteClient(String client){
        clientRepository.deleteClient(client);
    }
}
