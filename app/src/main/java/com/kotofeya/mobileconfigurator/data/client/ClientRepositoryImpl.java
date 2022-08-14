package com.kotofeya.mobileconfigurator.data.client;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.domain.client.ClientRepository;

import java.util.ArrayList;
import java.util.List;

public class ClientRepositoryImpl implements ClientRepository {

    private static final String TAG = ClientRepositoryImpl.class.getSimpleName();
    private static ClientRepositoryImpl instance;

    public static ClientRepositoryImpl getInstance() {
        if(instance == null){
            instance = new ClientRepositoryImpl();
        }
        return instance;
    }
    private ClientRepositoryImpl(){}

    private final List<String> clientList = new ArrayList<>();
    private final MutableLiveData<List<String>> clientsListLiveData = new MutableLiveData<>(new ArrayList<>());

    @Override
    public void deleteClient(String client) {
        Logger.d(TAG, "deleteClient()");
        clientList.remove(client);
        updateClientsLiveData();
    }

    @Override
    public LiveData<List<String>> getClients() {
        return clientsListLiveData;
    }

    @Override
    public void clearClients() {
        clientList.clear();
        updateClientsLiveData();
    }

    @Override
    public void addClients(List<String> clients) {
        clientList.addAll(clients);
        updateClientsLiveData();
    }

    private void updateClientsLiveData(){
        clientsListLiveData.postValue(clientList);
    }
}
