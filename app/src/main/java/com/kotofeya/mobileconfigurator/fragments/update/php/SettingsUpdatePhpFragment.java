package com.kotofeya.mobileconfigurator.fragments.update.php;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.activities.MainActivityViewModel;
import com.kotofeya.mobileconfigurator.databinding.ScannerFragmentClBinding;
import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.fragments.scanner.ScannerFragmentVM;
import com.kotofeya.mobileconfigurator.network.request.SettingsUpdatePhpListener;
import com.kotofeya.mobileconfigurator.network.request.SettingsUpdatePhpUseCase;
import com.kotofeya.mobileconfigurator.rv_adapter.AdapterListener;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapter;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterFactory;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SettingsUpdatePhpFragment extends Fragment implements
        SettingsUpdatePhpListener,
        AdapterListener {

    private static final String TAG = SettingsUpdatePhpFragment.class.getSimpleName();

    public static final String SSH_CONN = "ssh_conn";
    public static final String CONFIRM_THE_UPDATE_OF_PHP_VERSION = "Подтвердите обновление PHP";
    public static final String SSH_CONNECTION_FAILED = "Не удается установить ssh-подключение";
    public static final String UPDATING_PHP_VERSION_FAILED = "Ошибка обновления PHP";
    public static final String PHP_VERSION_UPDATED_SUCCESSFULLY = "Обновление PHP произошло успешно";
    public static final String MESSAGE_TAKE_INFO = "Опрос подключенных трансиверов";

    protected ScannerFragmentClBinding binding;
    protected FragmentHandler fragmentHandler;
    protected MainActivityViewModel viewModel;
    protected RvAdapter rvAdapter;
    protected ScannerFragmentVM scannerFragmentVM;

    public RvAdapterType getAdapterType() {
        return RvAdapterType.SETTINGS_UPDATE_PHP;
    }

    @Override
    public void adapterItemOnClick(Transceiver transceiver) {
        showUpdatePhpDialog(transceiver.getIp());
    }

    private void showUpdatePhpDialog(String ip){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        builder.setTitle(R.string.confirmation_is_required);
        builder.setMessage(CONFIRM_THE_UPDATE_OF_PHP_VERSION);

        builder.setPositiveButton(R.string.update_btn, (dialog, id) -> {
            Thread thread = new Thread(
                    new SettingsUpdatePhpUseCase(this, ip));
            thread.start();
        });
        builder.setNegativeButton(R.string.cancel_btn, (dialog, id) -> {});
        builder.setCancelable(true);
        builder.show();
    }

    @Override
    public void updateSuccessful(String ip, String response) {
        Logger.d(TAG, "updateSuccessful()");
        if(response.startsWith("Ok")){
            fragmentHandler.showMessage(PHP_VERSION_UPDATED_SUCCESSFULLY);
        } else {
            fragmentHandler.showMessage(UPDATING_PHP_VERSION_FAILED + " " + response);
        }
    }
    @Override
    public void updateFailed(String ip, String errorMessage) {
        Logger.d(TAG, "updateFailed()");
        fragmentHandler.showMessage(UPDATING_PHP_VERSION_FAILED  + " " + errorMessage);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);

        binding = ScannerFragmentClBinding.inflate(inflater, container, false);
        viewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);
        fragmentHandler = ((MainActivity) requireActivity()).getFragmentHandler();

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

        hideProgressBar();
        scan();
        viewModel.setMainTxtLabel("Update PHP");
        viewModel.setMainBtnRescanVisibility(View.VISIBLE);
        binding.version1.setVisibility(View.GONE);
        binding.checkVersionBtn.setVisibility(View.GONE);
        rvAdapter = RvAdapterFactory.getRvAdapter(getAdapterType(),
                new ArrayList<>(), this);
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