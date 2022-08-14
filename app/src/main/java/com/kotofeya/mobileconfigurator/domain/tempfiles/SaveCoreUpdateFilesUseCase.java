package com.kotofeya.mobileconfigurator.domain.tempfiles;

import java.io.File;

public class SaveCoreUpdateFilesUseCase {
    TempFilesRepository tempFilesRepository;

    public SaveCoreUpdateFilesUseCase(TempFilesRepository tempFilesRepository){
        this.tempFilesRepository = tempFilesRepository;
    }

    public void saveUpdateCoreFiles(File[] files){
        tempFilesRepository.saveUpdateCoreFiles(files);
    }
}
