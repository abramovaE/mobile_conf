package com.kotofeya.mobileconfigurator.fragments.update;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UpdateOsFragmentVM extends ViewModel {

    private MutableLiveData<String> _serverOsVersion = new MutableLiveData<>("");
    public LiveData<String> getServerOsVersion(){return _serverOsVersion;}
    public void setServerOsVersion(String version){this._serverOsVersion.postValue(version);}

    private MutableLiveData<String> _localOsVersion = new MutableLiveData<>("");
    public LiveData<String> getLocalOsVersion(){return _localOsVersion;}
    public void setLocalOsVersion(String version){this._localOsVersion.postValue(version);}
}
