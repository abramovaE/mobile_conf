package com.kotofeya.mobileconfigurator.presentation.fragments.scanner;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.databinding.ScannerFragmentClBinding;
import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;
import com.kotofeya.mobileconfigurator.presentation.activities.MainActivity;
import com.kotofeya.mobileconfigurator.presentation.activities.MainActivityViewModel;
import com.kotofeya.mobileconfigurator.presentation.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.presentation.rv_adapter.RvAdapter;
import com.kotofeya.mobileconfigurator.presentation.rv_adapter.RvAdapterFactory;
import com.kotofeya.mobileconfigurator.presentation.rv_adapter.RvAdapterType;

import java.util.ArrayList;
import java.util.List;


public class BasicScannerFragment extends Fragment {

    private static final String TAG = BasicScannerFragment.class.getSimpleName();
    protected ScannerFragmentClBinding binding;
    protected FragmentHandler fragmentHandler;
    protected MainActivityViewModel viewModel;
    protected RvAdapter rvAdapter;
    protected ScannerFragmentVM scannerFragmentVM;
    public static final String MESSAGE_TAKE_INFO = "Опрос подключенных трансиверов";

    @Override
    public void onStart() {
        super.onStart();
        viewModel.setMainBtnRescanVisibility(View.VISIBLE);
        viewModel.clearTransceivers();
        scan();
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
        viewModel.isGetTakeInfoFinished.observe(getViewLifecycleOwner(), this::updateScannerProgressBarTv);

        scannerFragmentVM.getProgressBarVisibility()
                .observe(getViewLifecycleOwner(), this::updateProgressBarVisibility);
        scannerFragmentVM.setProgressTvVisibility(View.GONE);

        rvAdapter = RvAdapterFactory.getRvAdapter(RvAdapterType.BASIC_SCANNER_TYPE,
                new ArrayList<>(), null);
        binding.rvScanner.setAdapter(rvAdapter);
        viewModel.setMainTxtLabel(getString(R.string.basic_scan_main_txt_label));
        return binding.getRoot();
    }


    private void updateProgressBarVisibility(int visibility) {
        Logger.d(TAG, "updateProgressBarVisibility(): " + visibility);
        binding.scannerProgressBar.setVisibility(visibility);
    }


    @SuppressLint("NotifyDataSetChanged")
    protected void updateUI(List<Transceiver> transceivers){
        Logger.d(TAG, "updateUI()");
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