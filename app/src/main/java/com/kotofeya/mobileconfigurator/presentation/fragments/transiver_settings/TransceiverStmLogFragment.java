package com.kotofeya.mobileconfigurator.presentation.fragments.transiver_settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.databinding.TransiverSettingsFragmentBinding;
import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;
import com.kotofeya.mobileconfigurator.network.PostInfoListener;
import com.kotofeya.mobileconfigurator.presentation.activities.MainActivity;
import com.kotofeya.mobileconfigurator.presentation.activities.MainActivityViewModel;
import com.kotofeya.mobileconfigurator.presentation.fragments.FragmentHandler;

import java.util.List;

public class TransceiverStmLogFragment extends Fragment
        implements PostCommand {

    private static final String LOG_IS_EMPTY = "Stm log is empty";

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
        viewModel.transceivers.observe(getViewLifecycleOwner(), this::updateUIButtons);
        viewModel.transceiverSettingsText().observe(getViewLifecycleOwner(), this::updateUI);

        fragmentHandler = ((MainActivity) requireActivity()).getFragmentHandler();
        Transceiver transceiver = viewModel.getTransceiverBySsid(ssid);

        binding.showSettingsBtn.setOnClickListener( v-> {
            String ip = transceiver.getIp();
            Thread thread = new Thread(new PostInfo(ip, PostCommand.STM_UPDATE_LOG, new PostInfoListener() {
                @Override
                public void postInfoSuccessful(String ip, String response) {
                    response = response.isEmpty() ? LOG_IS_EMPTY : response;
                    viewModel.setTransceiverSettingsText(response);
                }

                @Override
                public void postInfoFailed(String error) {

                }
            }));
            thread.start();
        });

        binding.setDefaultSettings.setOnClickListener(v -> {
            String ip = transceiver.getIp();
            Thread thread = new Thread(new PostInfo(ip, PostCommand.STM_UPDATE_LOG_CLEAR, new PostInfoListener() {
                @Override
                public void postInfoSuccessful(String ip, String response) {
                    fragmentHandler.showMessage("Stm log file was cleared");
                    viewModel.setTransceiverSettingsText(LOG_IS_EMPTY);
                }

                @Override
                public void postInfoFailed(String error) {

                }
            }));
            thread.start();
        });

        viewModel.setMainTxtLabel("Stm log: " + ssid);

        binding.showSettingsBtn.setEnabled(false);
        binding.setDefaultSettings.setEnabled(false);

        binding.addNewSettings.setVisibility(View.GONE);
        binding.showSettingsBtn.setText(getString(R.string.show_stm_log));
        binding.setDefaultSettings.setText(getString(R.string.clear_stm_log));
        return binding.getRoot();
    }

    public void updateButtonsState() {
        binding.showSettingsBtn.setEnabled(true);
        binding.setDefaultSettings.setEnabled(true);
    }

    private void updateUIButtons(List<Transceiver> transceivers) {
        Transceiver transceiver = viewModel.getTransceiverBySsid(ssid);
        String ip = transceiver.getIp();
        String version = transceiver.getVersion();
        if(version != null && !version.equals("ssh_conn")){
            if(ip != null){
                updateButtonsState();
            }
        }
    }


    private void updateUI(String text) {
        binding.settingsTv.setText(text);
    }

}