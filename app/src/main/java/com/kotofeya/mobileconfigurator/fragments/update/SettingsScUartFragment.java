package com.kotofeya.mobileconfigurator.fragments.update;

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

import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.InternetConn;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.ProgressBarInt;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.activities.MainActivityViewModel;
import com.kotofeya.mobileconfigurator.databinding.ScannerFragmentClBinding;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.fragments.scanner.ScannerFragment;
import com.kotofeya.mobileconfigurator.fragments.scanner.ScannerFragmentVM;
import com.kotofeya.mobileconfigurator.rv_adapter.AdapterListener;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapter;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterFactory;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;
import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SettingsScUartFragment extends Fragment
        implements
        AdapterListener {

    private static final String TAG = SettingsNetworkFragment.class.getSimpleName();

    protected ScannerFragmentClBinding binding;
    protected FragmentHandler fragmentHandler;
    protected MainActivityViewModel viewModel;
    protected RvAdapter rvAdapter;
    protected ScannerFragmentVM scannerFragmentVM;
    public static final String MESSAGE_TAKE_INFO = "Опрос подключенных трансиверов";

    public RvAdapterType getAdapterType() {
        return RvAdapterType.SETTINGS;
    }

    @Override
    public void adapterItemOnClick(Transceiver transceiver) {
        String ssid = transceiver.getSsid();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKeys.SSID_KEY, ssid);
        fragmentHandler.changeFragmentBundle(FragmentHandler.TRANSCEIVER_SETTINGS_SCUART_FRAGMENT, bundle);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = ScannerFragmentClBinding.inflate(inflater, container, false);
        viewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);
        fragmentHandler = ((MainActivity) requireActivity()).getFragmentHandler();
        scannerFragmentVM = ViewModelProviders.of(requireActivity()).get(ScannerFragmentVM.class);

        viewModel.transceivers.observe(getViewLifecycleOwner(), this::updateUI);
        viewModel.isGetTakeInfoFinished.observe(getViewLifecycleOwner(),
                this::updateScannerProgressBarTv);

        scannerFragmentVM.getProgressBarVisibility().observe(getViewLifecycleOwner(),
                this::updateProgressBarVisibility);
        scannerFragmentVM.setProgressTvVisibility(View.GONE);
        scannerFragmentVM.getProgressTvVisibility().observe(getViewLifecycleOwner(),
                this::updateProgressTvVisibility);
        scannerFragmentVM.getProgressTvText().observe(getViewLifecycleOwner(),
                this::updateProgressTvText);

        rvAdapter = RvAdapterFactory.getRvAdapter(getAdapterType(),
                new ArrayList<>(), this);
        binding.rvScanner.setAdapter(rvAdapter);

        viewModel.setMainTxtLabel("ScUart");
        viewModel.setMainBtnRescanVisibility(View.VISIBLE);
        binding.version1.setVisibility(View.GONE);
        binding.checkVersionBtn.setVisibility(View.GONE);

        scan();

        return binding.getRoot();
    }

    private void updateProgressTvText(String s) {
        binding.progressTv.setText(s);
    }
    private void updateProgressTvVisibility(int visibility) {
        binding.progressTv.setVisibility(visibility);
    }
    private void updateProgressBarVisibility(int visibility) {
        Logger.d(TAG, "updateProgressBarVisibility(): " + visibility);
        binding.scannerProgressBar.setVisibility(visibility);
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
