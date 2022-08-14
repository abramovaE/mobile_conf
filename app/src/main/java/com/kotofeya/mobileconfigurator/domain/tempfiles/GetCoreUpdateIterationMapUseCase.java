package com.kotofeya.mobileconfigurator.domain.tempfiles;

import java.util.Map;

public class GetCoreUpdateIterationMapUseCase {

    private TempFilesRepository tempFilesRepository;

    public GetCoreUpdateIterationMapUseCase(TempFilesRepository tempFilesRepository) {
        this.tempFilesRepository = tempFilesRepository;
    }

    public Map<String, Integer> getCoreUpdateIterationMap(){
        return tempFilesRepository.getCoreUpdateIterationMap();
    }
}
