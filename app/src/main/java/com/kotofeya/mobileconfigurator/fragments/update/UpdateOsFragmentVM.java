package com.kotofeya.mobileconfigurator.fragments.update;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UpdateOsFragmentVM extends ViewModel {
    private MutableLiveData<String> _osVersion = new MutableLiveData<>("");
    public LiveData<String> getOsVersion(){return _osVersion;}
    public void setOsVersion(String version){this._osVersion.postValue(version);}
}
