package com.kotofeya.mobileconfigurator.domain.tempfiles;

public class SetIterationUseCase {
    private final TempFilesRepository tempFilesRepository;

    public SetIterationUseCase(TempFilesRepository tempFilesRepository) {
        this.tempFilesRepository = tempFilesRepository;
    }

    public void setIteration(String serial, int iteration){
        tempFilesRepository.setIteration(serial, iteration);

    }
}