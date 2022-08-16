package com.kotofeya.mobileconfigurator.domain.tempfiles;


import java.util.Map;

public class GetTransportContentUseCase {

    private TempFilesRepository tempFilesRepository;

    public GetTransportContentUseCase(TempFilesRepository tempFilesRepository) {
        this.tempFilesRepository = tempFilesRepository;
    }

    public Map<String , String> getTransportContent(){
        return tempFilesRepository.getTransportContent();
    }
}
