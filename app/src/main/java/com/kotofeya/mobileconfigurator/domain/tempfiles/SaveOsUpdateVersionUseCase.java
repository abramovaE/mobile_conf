package com.kotofeya.mobileconfigurator.domain.tempfiles;


public class SaveOsUpdateVersionUseCase {
    TempFilesRepository tempFilesRepository;

    public SaveOsUpdateVersionUseCase(TempFilesRepository tempFilesRepository){
        this.tempFilesRepository = tempFilesRepository;
    }

    public void saveOsUpdateVersion(String version){
        tempFilesRepository.setUpdateOsVersion(version);
    }
}
