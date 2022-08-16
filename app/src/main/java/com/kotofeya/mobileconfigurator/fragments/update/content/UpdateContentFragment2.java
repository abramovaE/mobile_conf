package com.kotofeya.mobileconfigurator.fragments.update.content;

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

import com.kotofeya.mobileconfigurator.InternetConn;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.activities.MainActivityViewModel;
import com.kotofeya.mobileconfigurator.data.TempFilesRepositoryImpl;
import com.kotofeya.mobileconfigurator.databinding.ScannerFragmentClBinding;
import com.kotofeya.mobileconfigurator.domain.tempfiles.GetTransportContentUseCase;
import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.fragments.scanner.ScannerFragmentVM;
import com.kotofeya.mobileconfigurator.network.DownloadFileUtils;
import com.kotofeya.mobileconfigurator.network.download.DownloadFileListener;
import com.kotofeya.mobileconfigurator.network.download.DownloadFileUseCase;
import com.kotofeya.mobileconfigurator.network.request.PostStatContentVersionListener;
import com.kotofeya.mobileconfigurator.network.request.PostStatContentVersionUseCase;
import com.kotofeya.mobileconfigurator.network.request.PostTranspContentVersionListener;
import com.kotofeya.mobileconfigurator.network.request.PostTranspContentVersionUseCase;
import com.kotofeya.mobileconfigurator.network.upload.UploadContentUpdateFileListener;
import com.kotofeya.mobileconfigurator.network.upload.UploadContentUpdateFileUseCase;
import com.kotofeya.mobileconfigurator.rv_adapter.AdapterListener;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapter;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterFactory;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UpdateContentFragment2 extends Fragment
        implements PostStatContentVersionListener,
        PostTranspContentVersionListener,
        DownloadFileListener,
        UploadContentUpdateFileListener,
        AdapterListener {

    public static final String TAG = UpdateContentFragment2.class.getSimpleName();

    public static final String CHOOSE_THE_CITY_FOR_LOAD = "Выберите город для загрузки";
    public static final String MESSAGE_TAKE_INFO = "Опрос подключенных трансиверов";
    public static final String TRANSPORT_CONTENT_VERSION_URL = "http://95.161.210.44/update/content/transp";
    public static final String STATION_CONTENT_VERSION_URL = "http://95.161.210.44/update/content/station";
    private static final String CONFIRM_CONTENT_DOWNLOAD = "Подтвердите загрузку: %s";
    public static final String DOWNLOADING_FILE = "Загрузка: %s";
    public static final String DOWNLOADING_FILE_FAILED =
            "Возникла ошибка при скачивании файла: %s с сервера";
    public static final String CHOOSE_THE_CITY_FOR_UPLOAD = "Choose the city for upload";

    protected static final int MOBILE_SETTINGS_RESULT = 0;
    public static final int DOWNLOAD_TO_STORAGE = 0;
    public static final int DOWNLOAD_FOR_UPLOAD = 1;
    public static final String DOWNLOAD_CONTENT_UPDATE_FILES_TO_STORAGE = "Загрузить файлы для обновления в память телефона";
    public static final String SAVED_FILES = "Сохраненные файлы: \n";
    public static final String TRANSP_KEY_PREFIX = "transp ";

    protected ScannerFragmentClBinding binding;
    protected FragmentHandler fragmentHandler;
    protected MainActivityViewModel viewModel;
    private UpdateContentFragmentVM updateContentFragmentVM;
    protected RvAdapter rvAdapter;
    protected ScannerFragmentVM scannerFragmentVM;

    private final TempFilesRepositoryImpl tempFilesRepository = TempFilesRepositoryImpl.getInstance();

    public void loadVersion() {
        boolean isInternetEnabled = InternetConn.hasInternetConnection();
        Logger.d(TAG, "loadVersion(), isInternetEnabled: " + isInternetEnabled);
        if(isInternetEnabled) {
            new PostTranspContentVersionUseCase(this, TRANSPORT_CONTENT_VERSION_URL).newRequest();
            new PostStatContentVersionUseCase(this, STATION_CONTENT_VERSION_URL).newRequest();
        }
    }

    @Override
    public void postStatContentVersionSuccessful(String command, Map<String, String> versions) {
        Logger.d(TAG, "postStatContentVersionSuccessful(), version: " + versions);
        updateContentFragmentVM.setStationContentVersions(versions);
    }
    @Override
    public void postStatContentVersionFailed(String command, String error) {
        Logger.d(TAG, "postStatContentVersionFailed(), error: " + error);
    }
    @Override
    public void postTranspContentVersionSuccessful(String command, List<String> versions) {
        Logger.d(TAG, "postTranspContentVersionSuccessful(), version: " + versions);
        updateContentFragmentVM.setTransportContentVersions(versions);
    }
    @Override
    public void postTranspContentVersionFailed(String command, String error) {
        Logger.d(TAG, "postTranspContentVersionFailed(), error: " + error);
    }

    public RvAdapterType getAdapterType() {
        Logger.d(TAG, "getAdapterType()");
        return RvAdapterType.UPDATE_CONTENT_TYPE;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = ScannerFragmentClBinding.inflate(inflater, container, false);
        viewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);
        fragmentHandler = ((MainActivity) requireActivity()).getFragmentHandler();

        viewModel.clients.observe(getViewLifecycleOwner(), this::updateClients);
        viewModel.transceivers.observe(getViewLifecycleOwner(), this::updateUI);
        viewModel.isGetTakeInfoFinished.observe(getViewLifecycleOwner(), this::updateScannerProgressBarTv);

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

        updateContentFragmentVM = ViewModelProviders.of(requireActivity()).get(UpdateContentFragmentVM.class);

        hideProgressBar();
        loadVersion();
        scan();
        updateFilesTv();

        viewModel.setMainTxtLabel(getString(R.string.update_content_main_txt_label));

        binding.checkVersionBtn.setVisibility(View.VISIBLE);
        viewModel.setMainBtnRescanVisibility(View.VISIBLE);
        binding.version1.setVisibility(View.GONE);
        binding.updateContentLabel.setVisibility(View.VISIBLE);

        binding.checkVersionBtn.setText(DOWNLOAD_CONTENT_UPDATE_FILES_TO_STORAGE);
        binding.checkVersionBtn.setOnClickListener(v -> {
            boolean isInternetEnabled = InternetConn.hasInternetConnection();
            if(isInternetEnabled){
                Map<String, String> transportContent = getTransportContent();
                showDownloadContentToStorageDialog(transportContent);
            } else {
                EnableMobileConfDialog dialog = new EnableMobileConfDialog();
                dialog.show(fragmentHandler.getFragmentManager(),
                        FragmentHandler.ENABLE_MOBILE_DIALOG_TAG);
            }
        });

        rvAdapter = RvAdapterFactory.getRvAdapter(getAdapterType(),
                new ArrayList<>(), getAdapterListener());
        binding.rvScanner.setAdapter(rvAdapter);
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

    private void updateClients(List<String> clients) {
        viewModel.updateTransceivers();
    }

    private void updateFilesTv(){
        Logger.d(TAG, "updateFilesTv()");
        StringBuilder sb = new StringBuilder();
        sb.append(SAVED_FILES);
        new LinkedList<>(tempFilesRepository.getUpdateContentFilePaths())
                .forEach(it -> sb.append(it.substring(it.lastIndexOf("/") + 1, it.indexOf("_"))).append("\n"));
        scannerFragmentVM.setDownloadedFilesTvText(sb.toString());
        scannerFragmentVM.setDownloadedFilesTvVisibility(View.VISIBLE);
    }



    public Map<String, String> getTransportContent(){
        GetTransportContentUseCase getTransportContentUseCase =
                new GetTransportContentUseCase(tempFilesRepository);
        return getTransportContentUseCase.getTransportContent();

//        Map<String, String> transportContent = new HashMap<>();
//        boolean isInternetEnabled = InternetConn.hasInternetConnection();
//        Collection<String> collection = (isInternetEnabled) ?
//                updateContentFragmentVM.transportContentVersionsLiveData.getValue() :
//                tempFilesRepository.getUpdateContentFilePaths();
//        for (String s : collection) {
//            String key = getTransportFileKey(s, isInternetEnabled);
//            transportContent = addToTransportContent(transportContent, key, s);
//        }
//        return transportContent;
    }

    @Override
    public void adapterItemOnClick(Transceiver transceiver) {
        Logger.d(TAG, "adapterItemOnClick(): " + transceiver.getSsid());
        boolean isTransport = transceiver.isTransport();
        boolean isStationary = transceiver.isStationary();

        if (isTransport) {
            showUpdateContentConfDialog(transceiver.getIp(), getTransportContent());
        } else if (isStationary) {
            String key = transceiver.getSsid();
            Map<String, String> tempUpdateStationaryContentFiles =
                    updateContentFragmentVM.stationaryContentVersionsLiveData.getValue();
            if (tempUpdateStationaryContentFiles != null &&
                    tempUpdateStationaryContentFiles.containsKey(key)) {
                showUploadContentConfDialog(key,
                        key + "/data.tar.bz2",
                        transceiver.getIp(),
                        STATION_CONTENT_VERSION_URL);
            }
        }
    }

    public AdapterListener getAdapterListener(){ return this; }

    private void showDownloadContentToStorageDialog(Map<String, String> contentMap){
        Logger.d(TAG, "contentMap: " + contentMap);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(CHOOSE_THE_CITY_FOR_LOAD);
        String[] content = contentMap.keySet().toArray(new String[contentMap.size()]);
        builder.setItems(content,
                (dialog, which) -> showDownloadContentConfirmationDialog(TRANSP_KEY_PREFIX + content[which],
                        contentMap.get(content[which])));
        builder.setCancelable(true);
        builder.create().show();
    }
    private void showUpdateContentConfDialog(String ip, Map<String, String> contentMap){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(CHOOSE_THE_CITY_FOR_UPLOAD);
        String[] content = contentMap.keySet().toArray(new String[contentMap.size()]);
        builder.setItems(content,
                (dialog, which) -> showUploadContentConfDialog(TRANSP_KEY_PREFIX + content[which],
                        contentMap.get(content[which]),
                        ip,
                        TRANSPORT_CONTENT_VERSION_URL));
        builder.setCancelable(true);
        builder.create().show();
    }

    private void showDownloadContentConfirmationDialog(String key, String value){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.confirmation_is_required);
        builder.setMessage(String.format(CONFIRM_CONTENT_DOWNLOAD, key));
        builder.setPositiveButton(getString(R.string.upload_btn), (dialog, id) -> downloadFile(value));
        builder.setNegativeButton(getString(R.string.cancel_btn), (dialog, id) -> {});
        builder.setCancelable(true);
        builder.show();
    }

    private void showUploadContentConfDialog(String key,
                                             String value,
                                             String ip,
                                             String urlString){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.confirmation_is_required);
        builder.setMessage(String.format(CONFIRM_CONTENT_DOWNLOAD, key));
        builder.setPositiveButton(R.string.upload_btn, (dialog, id) -> {
            if(value.length() > 20 && value.contains("_")){
                File file = new File(value);
                showProgressBar(String.format(DOWNLOADING_FILE, file.getName()));
                new UploadContentUpdateFileUseCase(file, ip, "",
                        this).execute();
            } else {
                try {
                    URL url = new URL(urlString + "/" + value);
                    Logger.d(TAG, "url: " + url);
                    String tempFileName = value.replace("/", "_");
                    File file = DownloadFileUtils.createTempUpdateFile(tempFileName);
                    showProgressBar(String.format(DOWNLOADING_FILE, file.getName()));

                    Logger.d(TAG, "tempFilePath: " + file.getAbsolutePath());

                    new DownloadFileUseCase(url, new DownloadFileListener() {
                        @Override
                        public void downloadFileSuccessful(File destinationFile, int index) {
//                            hideProgressBar();
                            Logger.d(TAG, "downloadFileSuccessful(): " + destinationFile);
                            showProgressBar(String.format(DOWNLOADING_FILE, destinationFile.getName()));
                            new UploadContentUpdateFileUseCase(destinationFile,
                                    ip,
                                    "",
                                    UpdateContentFragment2.this)
                                    .execute();
                        }

                        @Override
                        public void downloadFileFailed(URL url, int index) {
                            hideProgressBar();
                            Logger.d(TAG, "downloadFileFailed(): " + url);
                        }

                        @Override
                        public void setProgress(int downloaded) {
                            scannerFragmentVM.setProgressBarProgress(downloaded);
                        }
                    }, file,
                            UpdateContentFragment2.DOWNLOAD_FOR_UPLOAD).newRequest();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel_btn, (dialog, id) -> {});
        builder.setCancelable(true);
        builder.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void uploaded(String ip){
        Logger.d(TAG, "uploaded: " + ip);
        viewModel.setUpdatingAttr(ip);
        rvAdapter.notifyDataSetChanged();
        fragmentHandler.showMessage(getString(R.string.uploaded));
    }

    @Override
    public void downloadFileSuccessful(File destinationFile, int index) {
        Logger.d(TAG, "downloadFileSuccessful(): " + destinationFile);
        if(index == DOWNLOAD_TO_STORAGE){
            tempFilesRepository.saveUpdateContentFilePaths(destinationFile.getPath());
            binding.downloadCoreUpdateFilesTv.setVisibility(View.VISIBLE);
            updateFilesTv();
            hideProgressBar();
        }
    }

    @Override
    public void downloadFileFailed(URL url, int index) {
        Logger.d(TAG, "downloadFileFailed(): " + url);
        fragmentHandler.showMessage(String.format(DOWNLOADING_FILE_FAILED, url));
        hideProgressBar();
    }

    @Override
    public void uploadContentFileSuccessful(File destinationFile, String serial, String ip) {
        Logger.d(TAG, "uploadContentFileSuccessful: " + destinationFile);
        hideProgressBar();
        uploaded(ip);
    }
    @Override
    public void uploadContentFileFailed(File file, String serial, String ip) {
        Logger.d(TAG, "uploadContentFileFailed(): " + file);
        hideProgressBar();
    }

    @Override
    public void setProgress(int downloaded) {
        scannerFragmentVM.setProgressBarProgress(downloaded);
    }

    private void downloadFile(String value){
        String tempFileName = value.replace("/", "_");
        File file = DownloadFileUtils.createTempUpdateFile(tempFileName);
        showProgressBar(String.format(DOWNLOADING_FILE, file.getName()));
        try {
            URL url = new URL(TRANSPORT_CONTENT_VERSION_URL + "/" + value);
            Logger.d(TAG, "url: " + url);
            Logger.d(TAG, "tempFilePath: " + file.getAbsolutePath());
            new DownloadFileUseCase(url, this, file, DOWNLOAD_TO_STORAGE).newRequest();
        } catch (MalformedURLException e) {
            Logger.d(TAG, "URL not valid: " + e.getCause());
            e.printStackTrace();
        }
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
            loadVersion();
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