package com.kotofeya.mobileconfigurator.network.upload;

import java.io.File;

public interface UploadContentUpdateFileListener {
    void uploadContentFileSuccessful(File destinationFile, String serial, String ip);
    void uploadContentFileFailed(File file, String serial, String ip);
    void setProgress(int downloaded);
}
