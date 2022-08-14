package com.kotofeya.mobileconfigurator.domain.tempfiles;

import java.io.File;

public class SaveOsUpdateFileUseCase {
    TempFilesRepository tempFilesRepository;

    public SaveOsUpdateFileUseCase(TempFilesRepository tempFilesRepository){
        this.tempFilesRepository = tempFilesRepository;
    }

    public void saveOsUpdateFile(File file){
        tempFilesRepository.saveUpdateOsFile(file);
    }
}
