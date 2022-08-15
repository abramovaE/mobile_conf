package com.kotofeya.mobileconfigurator.domain.tempfiles;


import java.io.File;

public class GetOsUpdateFileUseCase {
    private final TempFilesRepository tempFilesRepository;

    public GetOsUpdateFileUseCase(TempFilesRepository tempFilesRepository) {
        this.tempFilesRepository = tempFilesRepository;
    }

    public File getOsUpdateFile(){
        return tempFilesRepository.getUpdateOsFile();
    }




}
