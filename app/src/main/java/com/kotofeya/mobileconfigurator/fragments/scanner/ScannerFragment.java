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
import com.kotofeya.mobileconfigurator.databinding.ScannerFragmentClBinding;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapter;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.List;

public abstract class ScannerFragment extends Fragment {

    private static final String TAG = ScannerFragment.class.getSimpleName();
    protected ScannerFragmentClBinding binding;
    protected FragmentHandler fragmentHandler;
    protected CustomViewModel viewModel;
    protected Utils utils;
    protected RvAdapter rvAdapter;
    protected ScannerFragmentVM scannerFragmentVM;

    public abstract void scan();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ScannerFragmentClBinding.inflate(inflater, container, false);
        viewModel = ViewModelProviders.of(requireActivity(),
                new CustomViewModel.ModelFactory()).get(CustomViewModel.class);
        viewModel.getTransivers().observe(getViewLifecycleOwner(), this::updateUI);
        utils = ((MainActivity) requireActivity()).getUtils();
        fragmentHandler = ((MainActivity) requireActivity()).getFragmentHandler();

        scannerFragmentVM = ViewModelProviders.of(requireActivity()).get(ScannerFragmentVM.class);
        return binding.getRoot();
    }

    @SuppressLint("NotifyDataSetChanged")
    protected void updateUI(List<Transiver> transceivers){
        Logger.d(TAG, "updateUI()");
        rvAdapter.setObjects(transceivers);
        rvAdapter.notifyDataSetChanged();
    }
}