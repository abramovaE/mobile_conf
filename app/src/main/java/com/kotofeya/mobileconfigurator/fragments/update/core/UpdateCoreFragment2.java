package com.kotofeya.mobileconfigurator.fragments.update.core;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.activities.MainActivityViewModel;
import com.kotofeya.mobileconfigurator.data.TempFilesRepositoryImpl;
import com.kotofeya.mobileconfigurator.databinding.ScannerFragmentClBinding;
import com.kotofeya.mobileconfigurator.domain.tempfiles.GetCoreUpdateFilesUseCase;
import com.kotofeya.mobileconfigurator.domain.tempfiles.GetCoreUpdateIterationMapUseCase;
import com.kotofeya.mobileconfigurator.domain.tempfiles.SaveCoreUpdateFilesUseCase;
import com.kotofeya.mobileconfigurator.domain.tempfiles.SetIterationUseCase;
import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.fragments.scanner.ScannerFragmentVM;
import com.kotofeya.mobileconfigurator.network.download.DownloadFileListener;
import com.kotofeya.mobileconfigurator.network.download.DownloadFileUseCase;
import com.kotofeya.mobileconfigurator.network.upload.UploadCoreUpdateFileListener;
import com.kotofeya.mobileconfigurator.network.upload.UploadCoreUpdateFileUseCase;
import com.kotofeya.mobileconfigurator.rv_adapter.AdapterListener;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapter;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterFactory;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class UpdateCoreFragment2 extends Fragment
        implements
        AdapterListener,
        DownloadFileListener,
        UploadCoreUpdateFileListener,
        DialogListener {
    private static final String TAG = UpdateCoreFragment2.class.getSimpleName();

    public static final String TAKE_INFO_MESSAGE = "Опрос подключенных трансиверов";
    private static final String WAIT_MESSAGE = "Загрузка: %s на устройство %s произошла успешно. " +
            "Трансивер перезагружается. Обновите список трансиверов примерно через %d мин.";
    private static final String UPLOAD_ERROR_MESSAGE = "Ошибка загрузки файла %s на трансивер %s. " +
            "Пожалуйста, подождите и обновите список трансиверов, чтобы продолжить обновление ядра";
    private static final String NO_NEED_TO_UPDATE = "Ядро не нуждается в обновлении";

    private static final String DOWNLOAD_FILES =
            "Пожалуйста, загрузите с сервера файлы для обновления.";
    public static final String DOWNLOADING_FILE = "Загрузка: %s";
    public static final String DOWNLOADING_FILE_FAILED =
            "Возникла ошибка при скачивании файла: %s с сервера";

    private static final String CONFIRM_CORE_UPDATE = "Подтвердите обновление ядра: %s, шаг: %d";
    private static final String UPDATE = "Обновить";
    private static final String CANCEL = "Отмена";
    private static final String DOWNLOADING = "Загрузка %s на трансивер %s";

    private static final String MAIN_TEXT_LABEL = "Update the core";

    protected ScannerFragmentClBinding binding;
    protected FragmentHandler fragmentHandler;
    protected MainActivityViewModel viewModel;
    protected ScannerFragmentVM scannerFragmentVM;
    protected RvAdapter rvAdapter;
    private CountDownLatch downloadFilesFromServerLatcher;

    public File[] tempUpdateCoreFiles;

    private boolean isUploading = false;
    private boolean isDialogShowing = false;

    TempFilesRepositoryImpl tempFilesRepository = TempFilesRepositoryImpl.getInstance();
    private final GetCoreUpdateIterationMapUseCase getCoreUpdateIterationMap =
        new GetCoreUpdateIterationMapUseCase(tempFilesRepository);
    private final SetIterationUseCase setIteration = new SetIterationUseCase(tempFilesRepository);
    private final SaveCoreUpdateFilesUseCase saveUpdateCoreFiles =
            new SaveCoreUpdateFilesUseCase(tempFilesRepository);
    private final GetCoreUpdateFilesUseCase getCoreUpdateFilesUseCase =
            new GetCoreUpdateFilesUseCase(tempFilesRepository);

    private static final String CORE_URLS_DIR = "http://95.161.210.44/update/1.4-1.5/";

    private static final String[] CORE_URLS_FILE_NAMES = {
            "root_prepare_1.4-1.5.img.bz2",
            "boot-old.img.bz2",
            "boot-new.img.bz2",
            "root-1.5.6-release.img.bz2"
    };

    public RvAdapterType getAdapterType() {
        return RvAdapterType.SETTINGS_UPDATE_CORE;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = ScannerFragmentClBinding.inflate(inflater, container, false);
        viewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);
        fragmentHandler = ((MainActivity) requireActivity()).getFragmentHandler();

        viewModel.clients.observe(getViewLifecycleOwner(), this::updateClients);
        viewModel.transceivers.observe(getViewLifecycleOwner(), this::updateUI);
        viewModel.isGetTakeInfoFinished.observe(getViewLifecycleOwner(),
                this::updateScannerProgressBarTv);

        scannerFragmentVM = ViewModelProviders.of(this).get(ScannerFragmentVM.class);
        scannerFragmentVM.getProgressBarVisibility().observe(getViewLifecycleOwner(),
                this::updateProgressBarVisibility);
        scannerFragmentVM.getProgressBarProgress().observe(getViewLifecycleOwner(),
                this::updateProgressBarProgress);
        scannerFragmentVM.getProgressTvVisibility().observe(getViewLifecycleOwner(),
                this::updateProgressTvVisibility);
        scannerFragmentVM.getProgressTvText().observe(getViewLifecycleOwner(),
                this::updateProgressTvText);
        scannerFragmentVM.getDownloadedFilesTvText().observe(getViewLifecycleOwner(),
                this::updateDownloadedFilesTvText);
        scannerFragmentVM.getDownloadedFilesTvVisibility().observe(getViewLifecycleOwner(),
                this::updateDownloadedFilesTvVisibility);

        tempUpdateCoreFiles = getCoreUpdateFilesUseCase.getCoreUpdateFiles();

        hideProgressBar();
        updateFilesTv();
        scan();

        viewModel.setMainTxtLabel(MAIN_TEXT_LABEL);
        viewModel.setMainBtnRescanVisibility(View.VISIBLE);
        binding.version1.setVisibility(View.GONE);
        binding.checkVersionBtn.setVisibility(View.GONE);
        binding.downloadCoreUpdateFilesBtn.setVisibility(View.VISIBLE);

        rvAdapter = RvAdapterFactory.getRvAdapter(getAdapterType(),
                new ArrayList<>(), getAdapterListener());
        binding.rvScanner.setAdapter(rvAdapter);

        binding.downloadCoreUpdateFilesBtn.setOnClickListener(v -> {
            createUpdateCoreTempFiles();
            downloadFilesFromServerLatcher = new CountDownLatch(tempUpdateCoreFiles.length);
            downloadFile(0);
        });
        return binding.getRoot();
    }

    private void updateProgressBarVisibility(Integer integer) {
        binding.scannerProgressBar.setVisibility(integer);
    }
    private void updateProgressBarProgress(Integer integer) {
        binding.scannerProgressBar.setProgress(integer);
    }
    private void updateProgressTvVisibility(Integer integer) {
        binding.progressTv.setVisibility(integer);
    }
    private void updateProgressTvText(String s) {
        binding.progressTv.setText(s);
    }
    private void updateDownloadedFilesTvText(String s) {
        binding.downloadCoreUpdateFilesTv.setText(s);
    }
    private void updateDownloadedFilesTvVisibility(Integer integer) {
        binding.downloadCoreUpdateFilesTv.setVisibility(integer);
    }

    private void createUpdateCoreTempFiles() {
        File outputDir = getContext().getExternalFilesDir(null);
        Logger.d(TAG, " creating new temp core files, outputDir: " + outputDir);
        tempUpdateCoreFiles = new File[4];
        tempUpdateCoreFiles[0] = (new File(outputDir + "/" + CORE_URLS_FILE_NAMES[0]));
        tempUpdateCoreFiles[1] = (new File(outputDir + "/" + CORE_URLS_FILE_NAMES[1]));
        tempUpdateCoreFiles[2] = (new File(outputDir + "/" + CORE_URLS_FILE_NAMES[2]));
        tempUpdateCoreFiles[3] = (new File(outputDir + "/" + CORE_URLS_FILE_NAMES[3]));
        Logger.d(TAG, "new temp core files created: " + Arrays.asList(tempUpdateCoreFiles));
    }

    private void updateClients(List<String> clients) {
        viewModel.updateTransceivers();
    }

    private void updateFilesTv(){
        StringBuilder sb = new StringBuilder();
        (Arrays.asList(tempUpdateCoreFiles))
                .forEach(it -> sb.append(it.getName()).append("\n"));
        scannerFragmentVM.setDownloadedFilesTvText(sb.toString());
        scannerFragmentVM.setDownloadedFilesTvVisibility(View.VISIBLE);
    }

    private int getTimerMinutes(int iteration){
        switch (iteration){
            case 0: return 3;
            case 1: return 2;
            case 2:
            case 3:
                return 5;
        }
        return 0;
    }

    protected void updateUI(List<Transceiver> transceivers){
        Logger.d(TAG, "updateUI(): " + transceivers.stream().count());
        transceivers.sort(Comparator.comparing(Transceiver::getSsid));
        rvAdapter.setObjects(transceivers);
        rvAdapter.notifyDataSetChanged();
    }

    @Override
    public void adapterItemOnClick(Transceiver transceiver) {
        String ip = viewModel.getTransceiverBySsid(transceiver.getSsid()).getIp();
        if(ip != null) {
            Logger.d(TAG, "Update core was pressed");
            String versionString = transceiver.getVersionString();
            if(versionString.contains(Transceiver.VERSION_NEW)){
                fragmentHandler.showMessage(NO_NEED_TO_UPDATE);
            } else {
                if (isTempFilesExists()) {
                    viewModel.stopUpdatingTimer(transceiver);
                    Map<String, Integer> coreUpdateSsidIteration = getCoreUpdateIterationMap.getCoreUpdateIterationMap();
                    Integer coreUpdateIteration = coreUpdateSsidIteration.get(transceiver.getSsid());
                    if(coreUpdateIteration == null){
                        coreUpdateIteration = 0;
                    }
                    if (coreUpdateIteration == 1
                            && !transceiver.getOsVersion().contains(Transceiver.VERSION_PRE)) {
                            Logger.d(TAG, "iteration 1 was failed, start updating again");
                            coreUpdateIteration = 0;
                    }
                    showConfirmDialog(ip, this, coreUpdateIteration, transceiver);
                } else {
                    fragmentHandler.showMessage(DOWNLOAD_FILES);
                }
            }
        }
    }

    public boolean isTempFilesExists(){
        return Arrays.stream(tempUpdateCoreFiles).allMatch(File::exists);
    }

    private void showConfirmDialog(String ip, DialogListener listener, int iteration, Transceiver t){
        Logger.d(TAG, "showConfirmDialog(), isDialogShowing: "
                + isDialogShowing + ", isUploading: " + isUploading) ;
        String ssid = t.getSsid();
        if(!isDialogShowing && !isUploading) {
            isDialogShowing = true;
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(R.string.confirmation_is_required);
            builder.setMessage(String.format(Locale.getDefault(), CONFIRM_CORE_UPDATE, ssid, (iteration + 1)));
            builder.setPositiveButton(UPDATE, (dialog, id) -> {
                isDialogShowing = false;
                uploadFile(ip, t.getSsid(), iteration);
                setIteration.setIteration(ssid, iteration);

                listener.uploadFile(ip, ssid, iteration);
            });
            builder.setNegativeButton(CANCEL, (dialog, id) -> isDialogShowing = false);
            builder.setCancelable(true);
            builder.show();
        }
    }

    private String getProgressTvText(String serial, int coreUpdateIteration){
        Logger.d(TAG, "getProgressTvText(): " + serial + " " + coreUpdateIteration);
        File[] tempFiles = getCoreUpdateFilesUseCase.getCoreUpdateFiles();
        String fileName = tempFiles[coreUpdateIteration].getName();
        if(coreUpdateIteration == 2){
            fileName = fileName + " " + tempFiles[3].getName();
        }
        return String.format(DOWNLOADING, fileName, serial);
    }


    protected static final int MOBILE_SETTINGS_RESULT = 0;

    public AdapterListener getAdapterListener(){ return this; }

    private void downloadFile(int index){
        File downloadingFile = tempUpdateCoreFiles[index];
        Logger.d(TAG, "download file: " + downloadingFile);
        showProgressBar(String.format(DOWNLOADING_FILE, downloadingFile.getName()));
        try {
            URL url = new URL(CORE_URLS_DIR + CORE_URLS_FILE_NAMES[index]);
            DownloadFileUseCase downloadFileUseCase = new DownloadFileUseCase(url,
                    this,
                    downloadingFile, index);
            downloadFileUseCase.newRequest();
        } catch (MalformedURLException e) {
            Logger.d(TAG, "URL not valid: " + e.getCause());
            e.printStackTrace();
        }
    }
    @Override
    public void downloadFileSuccessful(File file, int index) {
        Logger.d(TAG, "downloadFileSuccessful()");
        hideProgressBar();
        decrLatch();
        int nextIndex = ++index;
        if(nextIndex < tempUpdateCoreFiles.length) {
            downloadFile(nextIndex);
        }
    }
    @Override
    public void downloadFileFailed(URL url, int index) {
        Logger.d(TAG, "downloadFileFailed(), url: " + url);
        decrLatch();
        fragmentHandler.showMessage(String.format(DOWNLOADING_FILE_FAILED, url));
    }

    public void uploadFile(String ip, String serial, int index){
        if(!isUploading) {
            File uploadingFile = tempUpdateCoreFiles[index];
            Logger.d(TAG, "uploadFile(), serial: " + serial + ", iteration: " + index);
            showProgressBar(getProgressTvText(serial, index));
            UploadCoreUpdateFileUseCase updateCoreUpdateFileUseCase = new UploadCoreUpdateFileUseCase(
                    uploadingFile,
                    ip,
                    index,
                    serial,
                    this);
            updateCoreUpdateFileUseCase.execute();
            isUploading = true;
        }
    }
    @Override
    public void uploadFileSuccessful(File destinationFile, int index, String serial, String ip) {
        isUploading = false;
        Logger.d(TAG, "uploadFileSuccessful(), index: " + index + ", file: " + destinationFile);
        hideProgressBar();
        String resultString = getCoreResultString(serial, index);
        int nextIndex = ++index;
        if(nextIndex >= tempUpdateCoreFiles.length){
            nextIndex = 0;
        }
        setIteration.setIteration(serial, nextIndex);
        viewModel.startUpdatingTimer(ip);
        fragmentHandler.showMessage(resultString);

    }
    @Override
    public void uploadFileFailed(File file, int index, String serial, String ip) {
        isUploading = false;
        Logger.d(TAG, "uploadFileFailed()");
        fragmentHandler.showMessage(String.format(UPLOAD_ERROR_MESSAGE, file.getName(), serial));
        hideProgressBar();
    }


    private String getCoreResultString(String serial, int iteration){
        Logger.d(TAG, "getCoreResultString(): " + serial + " " + iteration);
        String fileName = tempUpdateCoreFiles[iteration].getName();
        int minutesCount = getTimerMinutes(iteration);
        if(iteration == 2){
            fileName = fileName + ", " +
                    tempUpdateCoreFiles[3].getName();
        }
        return String.format(Locale.getDefault(), WAIT_MESSAGE, fileName, serial, minutesCount);
    }



    @Override
    public void setProgress(int downloaded) {
        scannerFragmentVM.setProgressBarProgress(downloaded);
    }


    public static class EnableMobileConfDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Logger.d(TAG, "show enable mobile config dialog");
            androidx.appcompat.app.AlertDialog.Builder builder =
                    new androidx.appcompat.app.AlertDialog.Builder(requireActivity());
            builder.setTitle(R.string.mobile_internet_title);
            builder.setMessage(R.string.mobile_internet_message);
            builder.setPositiveButton(getString(R.string.ok), (dialog, id) -> {
                Logger.d(TAG, "ok btn was pressed, show settings");
                startActivityForResult(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS), MOBILE_SETTINGS_RESULT);
            });
            builder.setNegativeButton(getString(R.string.cancel), (dialog, id) ->
                    Logger.d(TAG, "cancel btn was pressed, keep working without mobile internet"));
            builder.setCancelable(true);
            return builder.create();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MOBILE_SETTINGS_RESULT) {

        }
    }

    private void updateScannerProgressBarTv(Boolean aBoolean) {
        Logger.d(TAG, "updateScannerProgressBarTv(): " + aBoolean);
        if(!aBoolean){
            binding.progressTv.setText(TAKE_INFO_MESSAGE);
            binding.progressTv.setVisibility(View.VISIBLE);
        } else {
            binding.progressTv.setVisibility(View.GONE);
        }
    }

    public void scan(){
        Logger.d(TAG, "scan()");
        viewModel.pollConnectedClients();
    }

    private void showProgressBar(String text){
        Logger.d(TAG, "showProgressBar(), text: " + text);
        scannerFragmentVM.setProgressTvText(text);
        scannerFragmentVM.setProgressBarProgress(0);
        scannerFragmentVM.setProgressBarVisibility(View.VISIBLE);
        scannerFragmentVM.setProgressTvVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        Logger.d(TAG, "hideProgressBar()");
        scannerFragmentVM.setProgressBarProgress(0);
        scannerFragmentVM.setProgressBarVisibility(View.GONE);
        scannerFragmentVM.setProgressTvVisibility(View.GONE);
    }

    private void decrLatch(){
        downloadFilesFromServerLatcher.countDown();
        if(downloadFilesFromServerLatcher != null && downloadFilesFromServerLatcher.getCount() == 0){
            hideProgressBar();
            updateFilesTv();
            saveUpdateCoreFiles.saveUpdateCoreFiles(tempUpdateCoreFiles);
            downloadFilesFromServerLatcher = null;
        }
    }
}