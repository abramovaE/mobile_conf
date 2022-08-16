package com.kotofeya.mobileconfigurator.fragments.update.os;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kotofeya.mobileconfigurator.data.TempFilesRepositoryImpl;
import com.kotofeya.mobileconfigurator.domain.tempfiles.GetOsUpdateVersionUseCase;
import com.kotofeya.mobileconfigurator.domain.tempfiles.SaveOsUpdateVersionUseCase;

public class UpdateOsFragmentVM extends ViewModel {

    private final TempFilesRepositoryImpl tempFilesRepository =
            TempFilesRepositoryImpl.getInstance();
    private final GetOsUpdateVersionUseCase getOsUpdateVersionUseCase =
            new GetOsUpdateVersionUseCase(tempFilesRepository);
    private final SaveOsUpdateVersionUseCase saveOsUpdateVersionUseCase =
            new SaveOsUpdateVersionUseCase(tempFilesRepository);

    private final MutableLiveData<String> _serverOsVersion = new MutableLiveData<>("");
    public LiveData<String> getServerOsVersion(){return _serverOsVersion;}
    public void setServerOsVersion(String version){this._serverOsVersion.postValue(version);}

    public LiveData<String> _localOsVersion = getOsUpdateVersionUseCase.getOsUpdateVersion();
    public void setLocalOsVersion(String version){
        saveOsUpdateVersionUseCase.saveOsUpdateVersion(version);
    }
}