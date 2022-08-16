package com.kotofeya.mobileconfigurator.presentation.fragments.transiver_settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;

public class TransceiverSettingsScUartFragment extends TransceiverSettingsFragment {



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding.rebootLl.setVisibility(View.GONE);
        viewModel.setMainTxtLabel("ScUart: " + ssid);

        String ip = viewModel.getTransceiverBySsid(ssid).getIp();
        Thread thread = new Thread(new PostInfo(this, ip, PostCommand.SCUART));
        thread.start();
        return binding.getRoot();
    }

    @Override
    public void updateButtonsState() { }

    @Override
    protected String getShowSettingsCommand() {
        return null;
    }

    @Override
    protected String getDefaultSettingsCommand() {
        return null;
    }

    @Override
    protected DialogFragment getDialogSettings() {
        return null;
    }

    @Override
    protected String getAddSettingsCommand() {
        return null;
    }

}