package com.kotofeya.mobileconfigurator.fragments.update.content;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kotofeya.mobileconfigurator.data.TempFilesRepositoryImpl;
import com.kotofeya.mobileconfigurator.domain.tempfiles.GetOsUpdateVersionUseCase;
import com.kotofeya.mobileconfigurator.domain.tempfiles.SaveOsUpdateVersionUseCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateContentFragmentVM extends ViewModel {


    private MutableLiveData<List<String>> _transportContentVersionsLiveData =
            new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<Map<String, String>> _stationContentVersionsLiveData =
            new MutableLiveData<>(new HashMap<>());

    public LiveData<List<String>> transportContentVersionsLiveData =
            _transportContentVersionsLiveData;
    public LiveData<Map<String, String>> stationaryContentVersionsLiveData =
            _stationContentVersionsLiveData;

    public void setTransportContentVersions(List<String> list){
        _transportContentVersionsLiveData.postValue(list);
    }
    public void setStationContentVersions(Map<String, String> map){
        _stationContentVersionsLiveData.postValue(map);
    }


//    private TempFilesRepositoryImpl tempFilesRepository = TempFilesRepositoryImpl.getInstance();
//    private GetOsUpdateVersionUseCase getOsUpdateVersionUseCase = new GetOsUpdateVersionUseCase(tempFilesRepository);
//    private SaveOsUpdateVersionUseCase saveOsUpdateVersionUseCase = new SaveOsUpdateVersionUseCase(tempFilesRepository);
//
//    private MutableLiveData<String> _serverOsVersion = new MutableLiveData<>("");
//    public LiveData<String> getServerOsVersion(){return _serverOsVersion;}
//    public void setServerOsVersion(String version){this._serverOsVersion.postValue(version);}
//
//    public LiveData<String> _localOsVersion = getOsUpdateVersionUseCase.getOsUpdateVersion();
//    public void setLocalOsVersion(String version){
//        saveOsUpdateVersionUseCase.saveOsUpdateVersion(version);
//    }
}
