package com.kotofeya.mobileconfigurator.fragments.scanner;

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
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.activities.CustomViewModel;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.clientsHandler.ClientsHandler;
import com.kotofeya.mobileconfigurator.databinding.ScannerFragmentClBinding;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.rv_adapter.AdapterListener;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapter;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterFactory;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.ArrayList;
import java.util.List;

public abstract class ScannerFragment extends Fragment {

    private static final String TAG = ScannerFragment.class.getSimpleName();
    protected ScannerFragmentClBinding binding;
    protected FragmentHandler fragmentHandler;
    protected CustomViewModel viewModel;
    protected RvAdapter rvAdapter;
    protected ScannerFragmentVM scannerFragmentVM;
    protected ClientsHandler clientsHandler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ScannerFragmentClBinding.inflate(inflater, container, false);


        viewModel = ViewModelProviders.of(requireActivity(),
                new CustomViewModel.ModelFactory()).get(CustomViewModel.class);


        fragmentHandler = ((MainActivity) requireActivity()).getFragmentHandler();

        scannerFragmentVM = ViewModelProviders.of(requireActivity()).get(ScannerFragmentVM.class);
        clientsHandler = ClientsHandler.getInstance(viewModel);

        viewModel.getTransivers().observe(getViewLifecycleOwner(), this::updateUI);
        viewModel.getIsGetTakeInfoFinished().observe(getViewLifecycleOwner(), this::updateScannerProgressBarTv);
        viewModel.isClientsScanning().observe(getViewLifecycleOwner(), this::updateClientsScanning);

        viewModel.set_progressTvVisibility(false);
        rvAdapter = RvAdapterFactory.getRvAdapter(getAdapterType(),
                new ArrayList<>(), getAdapterListener());
        binding.rvScanner.setAdapter(rvAdapter);
        setMainTextLabelText();


        return binding.getRoot();
    }

    public abstract RvAdapterType getAdapterType();
    public abstract AdapterListener getAdapterListener();

    public abstract void setMainTextLabelText();

    @SuppressLint("NotifyDataSetChanged")
    protected void updateUI(List<Transiver> transceivers){
        Logger.d(TAG, "updateUI()");
        rvAdapter.setObjects(transceivers);
        rvAdapter.notifyDataSetChanged();
    }

    private void updateScannerProgressBarTv(Boolean aBoolean) {
        Logger.d(TAG, "updateScannerProgressBarTv(Boolean aBoolean): " + aBoolean);
        if(!aBoolean){
            viewModel.set_progressTvText(Utils.MESSAGE_TAKE_INFO);
            viewModel.set_progressTvVisibility(true);
        } else {
            viewModel.set_progressTvVisibility(false);
        }
    }

    public void scan(){
        Logger.d(TAG, "scan()");
        clientsHandler.pollConnectedClients();
    }

    private void updateClientsScanning(Boolean aBoolean) {
        Logger.d(TAG, "updateClientsScanning(): " + aBoolean);
//        if(!aBoolean){
//            scan();
//        }
    }
}