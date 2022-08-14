package com.kotofeya.mobileconfigurator.domain.client;

import androidx.lifecycle.LiveData;

import java.util.List;

public class GetClientListUseCase {
    ClientRepository clientRepository;

    public GetClientListUseCase(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public LiveData<List<String>> getClientList(){
        return clientRepository.getClients();
    }
}
