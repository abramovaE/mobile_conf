package com.kotofeya.mobileconfigurator.network.download;

import androidx.annotation.NonNull;

import com.kotofeya.mobileconfigurator.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DownloadFileUseCase {

    private static final String TAG = DownloadFileUseCase.class.getSimpleName();
    private final URL url;
    private final DownloadFileListener downloadFileListener;
    private final File file;
    private final int index;

    public DownloadFileUseCase(URL url,
                               DownloadFileListener downloadFileListener,
                               File destinationFile, int index) {
        this.url = url;
        this.downloadFileListener = downloadFileListener;
        this.file = destinationFile;
        this.index = index;
    }

    public void newRequest() {
        Logger.d(TAG, "newRequest()");
        OkHttpClient client = new OkHttpClient();
        Request get = new Request.Builder()
                    .url(url)
                    .build();
        client.newCall(get).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                writeToFile(Objects.requireNonNull(response.body()), file);
                downloadFileListener.downloadFileSuccessful(file, index);
                response.close();
            }
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                downloadFileListener.downloadFileFailed(url, index);
            }
        });
    }


    private void writeToFile(ResponseBody body, File file) throws IOException {
        Logger.d(TAG, "writeToFile(): " + file.getName() + " " + body.contentLength());
        long fullFileLength = body.contentLength();
        try(InputStream input = body.byteStream();
                OutputStream output = new FileOutputStream(file)) {
            byte[] data = new byte[4096];
            int count;
            int download = 0;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
                download += count;
                int downloaded = (int) ((10000L * download / fullFileLength) /100);
                downloadFileListener.setProgress(downloaded);
            }
        }
        body.close();
    }
}
