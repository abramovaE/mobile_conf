package com.kotofeya.mobileconfigurator.domain.tempfiles;


public class GetOsUpdateVersionUseCase {
    private TempFilesRepository tempFilesRepository;

    public GetOsUpdateVersionUseCase(TempFilesRepository tempFilesRepository) {
        this.tempFilesRepository = tempFilesRepository;
    }

    public String getOsUpdateVersion(){
        return tempFilesRepository.getUpdateOsVersion();
    }
}
