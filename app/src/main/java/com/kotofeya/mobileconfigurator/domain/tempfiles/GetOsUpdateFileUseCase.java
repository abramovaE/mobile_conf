package com.kotofeya.mobileconfigurator.domain.tempfiles;


import java.io.File;

public class GetOsUpdateFileUseCase {
    private TempFilesRepository tempFilesRepository;

    public GetOsUpdateFileUseCase(TempFilesRepository tempFilesRepository) {
        this.tempFilesRepository = tempFilesRepository;
    }

    public File getOsUpdateFile(){
        return tempFilesRepository.getUpdateOsFile();
    }
}
