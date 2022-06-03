package com.kotofeya.mobileconfigurator.fragments.scanner;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ScannerFragmentVM extends ViewModel {
    private MutableLiveData<String> time = new MutableLiveData<>("");
    public LiveData<String> getTime(){return time;}
    public void setTime(String time){this.time.postValue(time);}
}
