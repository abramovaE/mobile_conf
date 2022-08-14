package com.kotofeya.mobileconfigurator.network.upload;

import java.io.File;

public interface UploadOsUpdateFileListener {
    void uploadFileSuccessful(File destinationFile, String serial, String ip);
    void uploadFileFailed(File file, String serial, String ip);
    void setProgress(int downloaded);
}
