package com.kotofeya.mobileconfigurator.presentation.fragments.config;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.databinding.ScannerFragmentClBinding;
import com.kotofeya.mobileconfigurator.presentation.activities.MainActivity;
import com.kotofeya.mobileconfigurator.presentation.activities.MainActivityViewModel;
import com.kotofeya.mobileconfigurator.presentation.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.presentation.fragments.scanner.ScannerFragmentVM;
import com.kotofeya.mobileconfigurator.presentation.rv_adapter.AdapterListener;
import com.kotofeya.mobileconfigurator.presentation.rv_adapter.RvAdapter;
import com.kotofeya.mobileconfigurator.presentation.rv_adapter.RvAdapterFactory;
import com.kotofeya.mobileconfigurator.presentation.rv_adapter.RvAdapterType;
import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigStatFragment extends Fragment implements AdapterListener {

    private static final String TAG = ConfigStatFragment.class.getSimpleName();
    protected ScannerFragmentClBinding binding;
    protected FragmentHandler fragmentHandler;
    protected MainActivityViewModel viewModel;
    protected RvAdapter rvAdapter;
    protected ScannerFragmentVM scannerFragmentVM;
    public static final String MESSAGE_TAKE_INFO = "Опрос подключенных трансиверов";

    @Override
    public void adapterItemOnClick(Transceiver transceiver) {
        String ssid = transceiver.getSsid();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKeys.SSID_KEY, ssid);
        fragmentHandler.changeFragmentBundle(FragmentHandler.STATION_CONTENT_FRAGMENT, bundle);
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
        scan();

        viewModel.setMainTxtLabel(getString(R.string.config_stat_main_txt_label));
        viewModel.setMainBtnRescanVisibility(View.VISIBLE);

        rvAdapter = RvAdapterFactory.getRvAdapter(RvAdapterType.CONFIG_STATION,
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

    private void updateClients(List<String> clients) {
        viewModel.updateTransceivers();
    }

    @SuppressLint("NotifyDataSetChanged")
    protected void updateUI(List<Transceiver> transceivers){
        Logger.d(TAG, "updateUI()");
        transceivers = transceivers.stream()
                .filter(Transceiver::isStationary)
                .collect(Collectors.toList());
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