package com.kotofeya.mobileconfigurator.presentation.fragments.transiver_settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.databinding.TransiverSettingsFragmentBinding;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;
import com.kotofeya.mobileconfigurator.network.PostInfoListener;
import com.kotofeya.mobileconfigurator.presentation.activities.MainActivity;
import com.kotofeya.mobileconfigurator.presentation.activities.MainActivityViewModel;
import com.kotofeya.mobileconfigurator.presentation.fragments.FragmentHandler;

public class TransceiverSettingsScUartFragment extends Fragment
        implements PostCommand, PostInfoListener {

    private static final String TAG = TransceiverSettingsScUartFragment.class.getSimpleName();
    protected FragmentHandler fragmentHandler;
    protected TransiverSettingsFragmentBinding binding;
    protected MainActivityViewModel viewModel;
    protected String ssid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);


        this.ssid = getArguments() != null ? getArguments().getString("ssid") : null;

        binding = TransiverSettingsFragmentBinding.inflate(inflater, container, false);
        viewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);
        viewModel.setMainBtnRescanVisibility(View.GONE);
        viewModel.transceiverSettingsText().observe(getViewLifecycleOwner(), this::updateUI);

        fragmentHandler = ((MainActivity) requireActivity()).getFragmentHandler();

        binding.rebootLl.setVisibility(View.GONE);
        viewModel.setMainTxtLabel("ScUart: " + ssid);

        String ip = viewModel.getTransceiverBySsid(ssid).getIp();
        Thread thread = new Thread(new PostInfo(ip,
                PostCommand.SCUART,
                new PostInfoListener() {
            @Override
            public void postInfoSuccessful(String ip, String response) {
                response = response.isEmpty() ? "Empty response" : response;
                viewModel.setTransceiverSettingsText(response);
            }

            @Override
            public void postInfoFailed(String error) {
                Logger.d(TAG, "postInfoFailed(): " + error);
            }
        }));
        thread.start();
        return binding.getRoot();
    }

    @Override
    public void postInfoSuccessful(String ip, String response) {}

    @Override
    public void postInfoFailed(String error) {}

    private void updateUI(String text) {
        binding.settingsTv.setText(text);
    }
}