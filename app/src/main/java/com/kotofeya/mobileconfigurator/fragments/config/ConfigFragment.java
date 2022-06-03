package com.kotofeya.mobileconfigurator.fragments.config;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.databinding.ScannerFragmentClBinding;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.rv_adapter.AdapterListener;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapter;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.activities.CustomViewModel;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterFactory;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.ArrayList;
import java.util.List;


public abstract class ConfigFragment extends Fragment implements OnTaskCompleted, AdapterListener {

    public Utils utils;
    protected CustomViewModel viewModel;
    RvAdapter rvAdapter;

    public abstract void setMainTextLabel();
    public abstract void scan();

    protected ScannerFragmentClBinding binding;
    protected FragmentHandler fragmentHandler;

    @Override
    public void onStart() {
        super.onStart();
        setMainTextLabel();
        viewModel.setMainBtnRescanVisibility(View.GONE);
    }

    protected abstract RvAdapterType getAdapterType();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ScannerFragmentClBinding.inflate(getLayoutInflater(), container, false);
        viewModel = ViewModelProviders.of(requireActivity(), new CustomViewModel.ModelFactory()).get(CustomViewModel.class);
        fragmentHandler = ((MainActivity) requireActivity()).getFragmentHandler();

        binding.progressTv.setVisibility(View.GONE);

        utils = ((MainActivity) requireActivity()).getUtils();
        utils.getNewBleScanner().stopScan();
        rvAdapter = RvAdapterFactory.getRvAdapter(getAdapterType(),
                new ArrayList<>(), this);
        binding.rvScanner.setAdapter(rvAdapter);

        scan();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getIsGetTakeInfoFinished().observe(getViewLifecycleOwner(), this::updateScannerProgressBarTv);
    }

    private void updateScannerProgressBarTv(Boolean aBoolean) {
        if(!aBoolean){
            binding.progressTv.setText(Utils.MESSAGE_TAKE_INFO);
            binding.progressTv.setVisibility(View.VISIBLE);
        } else {
            binding.progressTv.setVisibility(View.GONE);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    protected void updateUI(List<Transiver> transceiverList){
        rvAdapter.setObjects(transceiverList);
        rvAdapter.notifyDataSetChanged();
    }
}