package com.kotofeya.mobileconfigurator.domain.tempfiles;


import java.util.Map;

public class SaveCoreUpdateIterationMapUseCase {
    TempFilesRepository tempFilesRepository;

    public SaveCoreUpdateIterationMapUseCase(TempFilesRepository tempFilesRepository){
        this.tempFilesRepository = tempFilesRepository;
    }

    public void saveCoreUpdateIterationMapUseCase(Map<String, Integer> map){
        tempFilesRepository.saveCoreUpdateIterationsMap(map);
    }
}
