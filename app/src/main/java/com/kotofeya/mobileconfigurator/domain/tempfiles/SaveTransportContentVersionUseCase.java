package com.kotofeya.mobileconfigurator.domain.tempfiles;

import java.util.List;

public class SaveTransportContentVersionUseCase {
    private TempFilesRepository tempFilesRepository;

    public SaveTransportContentVersionUseCase(TempFilesRepository tempFilesRepository) {
        this.tempFilesRepository = tempFilesRepository;
    }

    public void saveTransportContentVersion(List<String> transportContentVersion){
        tempFilesRepository.saveTransportContentVersions(transportContentVersion);
    }
}
