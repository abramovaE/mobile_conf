package com.kotofeya.mobileconfigurator.presentation.fragments.transiver_settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.network.PostCommand;

public class TransceiverStmLogFragment extends TransceiverSettingsFragment {

    private static final String LOG_IS_EMPTY = "Stm log is empty";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        viewModel.setMainTxtLabel("Stm log: " + ssid);

        binding.showSettingsBtn.setEnabled(false);
        binding.setDefaultSettings.setEnabled(false);

        binding.addNewSettings.setVisibility(View.GONE);
        binding.showSettingsBtn.setText(getString(R.string.show_stm_log));
        binding.setDefaultSettings.setText(getString(R.string.clear_stm_log));
        return binding.getRoot();
    }

    @Override
    public void updateButtonsState() {
        binding.showSettingsBtn.setEnabled(true);
        binding.setDefaultSettings.setEnabled(true);
    }

    @Override
    protected String getShowSettingsCommand() {
        return PostCommand.STM_UPDATE_LOG;
    }

    @Override
    protected String getDefaultSettingsCommand() {
        return PostCommand.STM_UPDATE_LOG_CLEAR;
    }

    @Override
    protected DialogFragment getDialogSettings() {
        return null;
    }

    @Override
    protected String getAddSettingsCommand() {
        return null;
    }

    @Override
    public void onTaskCompleted(Bundle result) {
        String command = result.getString(BundleKeys.COMMAND_KEY);
        String response = result.getString(BundleKeys.RESPONSE_KEY);
        if(command != null) {
            switch (command) {
                case PostCommand.STM_UPDATE_LOG:
                    response = response.isEmpty() ? LOG_IS_EMPTY : response;
                    viewModel.setTransceiverSettingsText(response);
                    break;
                case PostCommand.STM_UPDATE_LOG_CLEAR:
                    fragmentHandler.showMessage("Stm log file was cleared");
                    viewModel.setTransceiverSettingsText(LOG_IS_EMPTY);
                    break;
            }
        }
    }
}