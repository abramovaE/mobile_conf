package com.kotofeya.mobileconfigurator.fragments.update.os;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.InternetConn;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.activities.MainActivityViewModel;
import com.kotofeya.mobileconfigurator.data.TempFilesRepositoryImpl;
import com.kotofeya.mobileconfigurator.databinding.ScannerFragmentClBinding;
import com.kotofeya.mobileconfigurator.domain.tempfiles.GetOsUpdateFileUseCase;
import com.kotofeya.mobileconfigurator.domain.tempfiles.GetOsUpdateVersionUseCase;
import com.kotofeya.mobileconfigurator.domain.tempfiles.SaveOsUpdateFileUseCase;
import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.fragments.scanner.ScannerFragmentVM;
import com.kotofeya.mobileconfigurator.network.download.DownloadFileListener;
import com.kotofeya.mobileconfigurator.network.download.DownloadFileUseCase;
import com.kotofeya.mobileconfigurator.network.request.PostOsVersionListener;
import com.kotofeya.mobileconfigurator.network.request.PostOsVersionUseCase;
import com.kotofeya.mobileconfigurator.network.upload.UploadOsUpdateFileListener;
import com.kotofeya.mobileconfigurator.network.upload.UploadOsUpdateFileUseCase;
import com.kotofeya.mobileconfigurator.rv_adapter.AdapterListener;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapter;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterFactory;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class UpdateOsFragment2 extends Fragment implements
        AdapterListener,
        DownloadFileListener,
        UploadOsUpdateFileListener,
        PostOsVersionListener {

    private static final String TAG = UpdateOsFragment2.class.getSimpleName();

    public static final String OS_VERSION_URL = "http://95.161.210.44/update/rootimg";
    public static final String OS_URL = "http://95.161.210.44/update/rootimg/root.img.bz2";

    private static final String DOWNLOADING = "Загрузка %s на трансивер %s";
    public static final String DOWNLOADING_FILE = "Загрузка: %s";
    public static final String DOWNLOADING_FILE_FAILED =
            "Возникла ошибка при скачивании файла: %s с сервера";

    private static final String UPLOADED_MESSAGE = "Загрузка: %s на устройство %s произошла успешно. " +
            "Трансивер перезагружается.";
    public String serverOsVersion = "";
    private static final String UPLOAD_ERROR_MESSAGE = "Ошибка загрузки файла %s на трансивер %s.";
    private static final String CONFIRM_OS_UPDATE = "Подтвердите обновление ос: %s";
    public static final String MESSAGE_TAKE_INFO = "Опрос подключенных трансиверов";

    private File tempUpdateOsFile;

    boolean isUploading = false;

    TempFilesRepositoryImpl tempFilesRepository = TempFilesRepositoryImpl.getInstance();

    GetOsUpdateFileUseCase getOsUpdateFileUseCase =
            new GetOsUpdateFileUseCase(tempFilesRepository);
    GetOsUpdateVersionUseCase getOsUpdateVersionUseCase =
            new GetOsUpdateVersionUseCase(tempFilesRepository);
    SaveOsUpdateFileUseCase saveOsUpdateFileUseCase =
            new SaveOsUpdateFileUseCase(tempFilesRepository);

    protected static final int MOBILE_SETTINGS_RESULT = 0;
    protected ScannerFragmentClBinding binding;
    protected FragmentHandler fragmentHandler;
    protected MainActivityViewModel viewModel;
    protected RvAdapter rvAdapter;
    protected ScannerFragmentVM scannerFragmentVM;
    protected UpdateOsFragmentVM updateOsFragmentVM;

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

        scannerFragmentVM = ViewModelProviders.of(requireActivity()).get(ScannerFragmentVM.class);
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

        updateOsFragmentVM = ViewModelProviders.of(requireActivity()).get(UpdateOsFragmentVM.class);
        updateOsFragmentVM.getServerOsVersion().observe(getViewLifecycleOwner(), this::updateServerOsVersion);
        updateOsFragmentVM._localOsVersion.observe(getViewLifecycleOwner(), this::updateLocalOsVersion);

        tempUpdateOsFile = getOsUpdateFileUseCase.getOsUpdateFile();

        hideProgressBar();
        loadServerVersion();
        scan();

        viewModel.setMainTxtLabel(getString(R.string.update_os_main_txt_label));
        viewModel.setMainBtnRescanVisibility(View.VISIBLE);
        binding.version1.setVisibility(View.VISIBLE);
        binding.version2.setVisibility(View.VISIBLE);
        binding.checkVersionBtn.setVisibility(View.VISIBLE);

        rvAdapter = RvAdapterFactory.getRvAdapter(getAdapterType(),
                new ArrayList<>(), getAdapterListener());
        binding.rvScanner.setAdapter(rvAdapter);

        binding.checkVersionBtn.setOnClickListener(v -> {
            Logger.d(TAG, "check updates button was pressed");
            boolean isInternetEnabled = InternetConn.hasInternetConnection();
            if(isInternetEnabled){
                createUpdateOsTempFile();
                downloadFile();
            } else {
                EnableMobileConfDialog dialog = new EnableMobileConfDialog();
                dialog.show(fragmentHandler.getFragmentManager(),
                        FragmentHandler.ENABLE_MOBILE_DIALOG_TAG);
            }
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

    private void updateServerOsVersion(String version) {
        String versionText= getString(R.string.release_os) + ": " + version;
        binding.version1.setText(versionText);
    }

    private void updateLocalOsVersion(String version) {
        Logger.d(TAG, "updateLocalOsVersion(): " + version);
        String versionText = getString(R.string.storage_os) + ": " + version;
        binding.version2.setText(versionText);
    }

    public void loadServerVersion() {
        Logger.d(TAG, new Throwable().getStackTrace()[0].getMethodName());
        boolean isInternetEnabled = InternetConn.hasInternetConnection();
        if(isInternetEnabled){
            new PostOsVersionUseCase(this, OS_VERSION_URL).newRequest();
        }
    }
    @Override
    public void postOsVersionSuccessful(String command, String version) {
        Logger.d(TAG, "postVersionSuccessful(), version: " + version);
        if(version.contains("ver")){
            version = version.substring(version.indexOf("ver"), version.indexOf("<a href"));
            serverOsVersion = version;
            updateOsFragmentVM.setServerOsVersion(version);
        }
    }

    @Override
    public void postOsVersionFailed(String command, String error) {
        Logger.d(TAG, "postVersionFailed(): " + error);
    }

    private void downloadFile(){
        File downloadingFile = tempUpdateOsFile;
        Logger.d(TAG, "download file: " + downloadingFile);
        showProgressBar(String.format(DOWNLOADING_FILE, downloadingFile.getName()));
        try {
            URL url = new URL(OS_URL);
            DownloadFileUseCase downloadFileUseCase = new DownloadFileUseCase(url,
                    this,
                    downloadingFile, 0);
            downloadFileUseCase.newRequest();
        } catch (MalformedURLException e) {
            Logger.d(TAG, "URL not valid: " + e.getCause());
            e.printStackTrace();
        }
    }
    @Override
    public void downloadFileSuccessful(File file, int index) {
        Logger.d(TAG, "downloadFileSuccessful(): " + file.getName());
        updateOsFragmentVM.setLocalOsVersion(updateOsFragmentVM.getServerOsVersion().getValue());
        saveOsUpdateFileUseCase.saveOsUpdateFile(file);
        hideProgressBar();
    }
    @Override
    public void downloadFileFailed(URL url, int index) {
        Logger.d(TAG, "downloadFileFailed(), url: " + url);
        fragmentHandler.showMessage(String.format(DOWNLOADING_FILE_FAILED, url));
        hideProgressBar();
    }

    private void hideProgressBar(){
        Logger.d(TAG, "hideProgressBar()");
        scannerFragmentVM.setProgressBarProgress(0);
        scannerFragmentVM.setProgressBarVisibility(View.GONE);
        scannerFragmentVM.setProgressTvVisibility(View.GONE);
    }

    private void showProgressBar(String text){
        Logger.d(TAG, "showProgressBar(), text: " + text);
        scannerFragmentVM.setProgressTvText(text);
        scannerFragmentVM.setProgressBarProgress(0);
        scannerFragmentVM.setProgressBarVisibility(View.VISIBLE);
        scannerFragmentVM.setProgressTvVisibility(View.VISIBLE);
    }


    public RvAdapterType getAdapterType() {
        return RvAdapterType.UPDATE_OS_TYPE;
    }


    private void createUpdateOsTempFile() {
        File outputDir = App.get().getApplicationContext().getExternalFilesDir(null);
        Logger.d(TAG, "tempUpdateOsFile: " + tempUpdateOsFile);
        if(tempUpdateOsFile != null && tempUpdateOsFile.exists()){
            Logger.d(TAG, "delete exist file");
            tempUpdateOsFile.delete();
        }
        Logger.d(TAG, " creating new temp os file");
        tempUpdateOsFile = new File(outputDir + "/root.img.bz2");
    }


    private void updateClients(List<String> clients) {
        viewModel.updateTransceivers();
    }

    @Override
    public void adapterItemOnClick(Transceiver transceiver) {
        Logger.d(TAG, "adapterItemOnClick(): " + transceiver.getSsid() + " " + transceiver.getOsVersion());
        String osVersion = transceiver.getOsVersion();
        String downloadedVersion = getOsUpdateVersionUseCase.getOsUpdateVersion().getValue();

        int index = osVersion.indexOf("v");
        osVersion = osVersion.substring((index + 1), osVersion.indexOf("-"));

        if(osVersion.startsWith("1.4")){
            fragmentHandler.showMessage("Пожалуйста, сначала обновите ядро");
        } else if(downloadedVersion.contains(osVersion)){
            fragmentHandler.showMessage("ОС не нуждается в обновлении");
        } else {
            File updateOsFile = getOsUpdateFileUseCase.getOsUpdateFile();
            if (updateOsFile.length() > 1000) {
                String ip = transceiver.getIp();
                showUpdateOsConfirmDialog(ip, transceiver.getSsid());
            }
        }
    }

    private void showUpdateOsConfirmDialog(String ip, String serial){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.confirmation_is_required);
        builder.setMessage(String.format(CONFIRM_OS_UPDATE, serial));
        builder.setPositiveButton(getString(R.string.upload_btn), (dialog, id) -> uploadFile(ip, serial));
        builder.setNegativeButton(getString(R.string.cancel_btn), (dialog, id) -> {});
        builder.setCancelable(true);
        builder.show();
    }

    public void uploadFile(String ip, String serial){
        if(!isUploading) {
            File uploadingFile = tempUpdateOsFile;
            Logger.d(TAG, "uploadFile(), serial: " + serial);
            showProgressBar(getProgressTvText(serial));
            UploadOsUpdateFileUseCase updateOsUpdateFileUseCase =
                    new UploadOsUpdateFileUseCase(
                    uploadingFile,
                    ip,
                    serial,
                    this);
            updateOsUpdateFileUseCase.execute();
            isUploading = true;
        }
    }
    @Override
    public void uploadFileSuccessful(File destinationFile, String serial, String ip) {
        isUploading = false;
        Logger.d(TAG, "uploadFileSuccessful(), file: " + destinationFile);
        hideProgressBar();
        String resultString = getCoreResultString(serial);
        fragmentHandler.showMessage(resultString);
    }
    @Override
    public void uploadFileFailed(File file, String serial, String ip) {
        isUploading = false;
        Logger.d(TAG, "uploadFileFailed()");
        fragmentHandler.showMessage(String.format(UPLOAD_ERROR_MESSAGE, file.getName(), serial));
        hideProgressBar();
    }

    private String getCoreResultString(String serial){
        Logger.d(TAG, "getCoreResultString(): " + serial);
        String fileName = tempUpdateOsFile.getName();
        return String.format(UPLOADED_MESSAGE, fileName, serial);
    }

    private String getProgressTvText(String serial){
        Logger.d(TAG, "getProgressTvText()");
        String fileName = tempUpdateOsFile.getName();
        return String.format(DOWNLOADING, fileName, serial);
    }

    public AdapterListener getAdapterListener(){ return this; }


    @Override
    public void setProgress(int downloaded) {
        scannerFragmentVM.setProgressBarProgress(downloaded);
    }

    public static class EnableMobileConfDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Logger.d(TAG, "show enable mobile config dialog");
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
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
            loadServerVersion();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    protected void updateUI(List<Transceiver> transceivers){
        Logger.d(TAG, "updateUI()");
        transceivers.sort(Comparator.comparing(Transceiver::getSsid));
        rvAdapter.setObjects(transceivers);
        rvAdapter.notifyDataSetChanged();
    }

    private void updateScannerProgressBarTv(Boolean aBoolean) {
        Logger.d(TAG, "updateScannerProgressBarTv(Boolean aBoolean): " + aBoolean);
        if(!aBoolean){
            scannerFragmentVM.setProgressTvText(MESSAGE_TAKE_INFO);
            scannerFragmentVM.setProgressTvVisibility(View.VISIBLE);
        } else {
            scannerFragmentVM.setProgressTvVisibility(View.GONE);
        }
    }

    public void scan(){
        Logger.d(TAG, "scan()");
        viewModel.pollConnectedClients();
    }
}