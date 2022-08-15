package com.kotofeya.mobileconfigurator.domain.tempfiles;


import androidx.lifecycle.LiveData;

public class GetOsUpdateVersionUseCase {
    private TempFilesRepository tempFilesRepository;

    public GetOsUpdateVersionUseCase(TempFilesRepository tempFilesRepository) {
        this.tempFilesRepository = tempFilesRepository;
    }

    public LiveData<String> getOsUpdateVersion(){
        return tempFilesRepository.getUpdateOsVersion();
    }
}
