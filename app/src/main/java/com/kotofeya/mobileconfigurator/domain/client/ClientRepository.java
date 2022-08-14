package com.kotofeya.mobileconfigurator.domain.client;

import androidx.lifecycle.LiveData;

import java.util.List;

public interface ClientRepository {
    void deleteClient(String client);
    LiveData<List<String>> getClients();
    void clearClients();
    void addClients(List<String> clients);
}
