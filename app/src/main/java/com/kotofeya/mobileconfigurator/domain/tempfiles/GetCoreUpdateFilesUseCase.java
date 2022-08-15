package com.kotofeya.mobileconfigurator.domain.tempfiles;


import java.io.File;

public class GetCoreUpdateFilesUseCase {
    private final TempFilesRepository tempFilesRepository;

    public GetCoreUpdateFilesUseCase(TempFilesRepository tempFilesRepository) {
        this.tempFilesRepository = tempFilesRepository;
    }

    public File[] getCoreUpdateFiles(){
        return tempFilesRepository.getUpdateCoreFiles();
    }
}
