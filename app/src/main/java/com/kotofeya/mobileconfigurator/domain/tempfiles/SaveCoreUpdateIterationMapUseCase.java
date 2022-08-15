package com.kotofeya.mobileconfigurator.domain.tempfiles;


import java.util.Map;

public class SaveCoreUpdateIterationMapUseCase {
    private final TempFilesRepository tempFilesRepository;

    public SaveCoreUpdateIterationMapUseCase(TempFilesRepository tempFilesRepository){
        this.tempFilesRepository = tempFilesRepository;
    }

    public void saveCoreUpdateIterationMapUseCase(Map<String, Integer> map){
        tempFilesRepository.saveCoreUpdateIterationsMap(map);
    }
}
