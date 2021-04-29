package com.kotofeya.mobileconfigurator.activities;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.List;
import java.util.stream.Collectors;


public class CustomViewModel extends ViewModel {
    private MutableLiveData<List<Transiver>> wifiTransivers = new MutableLiveData<>();
    public MutableLiveData<List<Transiver>> getWifiransivers(){return wifiTransivers;}

    public void clearWifiTransivers(){
        List<Transiver> transiverList = wifiTransivers.getValue();
        transiverList.clear();
        wifiTransivers.postValue(transiverList);
    }

    public void addWifiTransiver(Transiver transiver){
        List<Transiver> transiverList = wifiTransivers.getValue();
        transiverList.add(transiver);
        wifiTransivers.postValue(transiverList);
    }


    public void updateWifiTransiver(String phoneIp, Transiver transiver){
        List<Transiver> transiverList = wifiTransivers.getValue();
        Transiver t = transiverList.stream().filter(it->it.getSsid().equals(transiver.getSsid())).collect(Collectors.toList()).get(0);



    }


//    public void addTransiver(Transiver transiver) {
//        if(transiver != null) {
//            List<Transiver> transiverList = transivers.getValue();
//            boolean isContains = transiverList.stream().anyMatch(trans -> trans.getSsid().equals(transiver.getSsid()));
//            if (!isContains) {
//                Logger.d(Logger.UTILS_LOG, "add transiver: ");
//                transiverList.add(transiver);
//            } else {
//                updateTransiver(transiver);
//            }
//        }
//    }



    public static class ModelFactory extends ViewModelProvider.NewInstanceFactory {
        public ModelFactory() {
            super();
        }
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass == CustomViewModel.class) {
                return (T) new CustomViewModel();
            }
            return null;
        }
    }







}