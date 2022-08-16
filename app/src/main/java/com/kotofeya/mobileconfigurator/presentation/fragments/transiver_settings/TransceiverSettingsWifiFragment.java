package com.kotofeya.mobileconfigurator.presentation.fragments.transiver_settings;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.databinding.AddNewWifiSettingsDialogBinding;
import com.kotofeya.mobileconfigurator.presentation.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;

public class TransceiverSettingsWifiFragment extends TransceiverSettingsFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        viewModel.setMainTxtLabel("Wifi settings: " + ssid);

        binding.showSettingsBtn.setText(getString(R.string.show_settings_wifi));
        binding.setDefaultSettings.setText(getString(R.string.set_default_wifi_settings));
        binding.addNewSettings.setText(getString(R.string.add_new_wifi_settings));

        return binding.getRoot();
    }


    @Override
    public void updateButtonsState() {
        binding.showSettingsBtn.setEnabled(true);
        binding.setDefaultSettings.setEnabled(true);
        binding.addNewSettings.setEnabled(true);
    }

    @Override
    protected String getShowSettingsCommand() {
        return PostCommand.READ_WPA;
    }

    @Override
    protected String getDefaultSettingsCommand() {
        return PostCommand.WIFI_CLEAR;
    }

    @Override
    protected DialogFragment getDialogSettings() {

        AddNewWifiSettingsDialog dialog = new AddNewWifiSettingsDialog();
        dialog.setListener(this);
        return dialog;
    }

    @Override
    protected String getAddSettingsCommand() {
        return FragmentHandler.ADD_NEW_WIFI_SETTINGS_DIALOG;
    }

    @Override
    public void showErrorMessage(String errorMessage) {
        fragmentHandler.showMessage(errorMessage);
    }

    public static class AddNewWifiSettingsDialog extends DialogFragment implements PostCommand{
        private String ip;
        private AddNewWifiSettingsDialogBinding binding;
        private OnTaskCompleted listener;

        public void setListener(OnTaskCompleted listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            this.ip = requireArguments().getString(BundleKeys.IP_KEY);
            binding = AddNewWifiSettingsDialogBinding.inflate(getLayoutInflater());
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(getResources().getString(R.string.add_new_wifi_title));
            builder.setView(binding.getRoot());
            binding.addWifiSaveBtn.setOnClickListener(v1 -> {
                String ssidStr = binding.addWifiSsid.getText().toString();
                String passwdStr = binding.addWifiPassword.getText().toString();
                if (isValid(passwdStr)) {
                    Thread thread = new Thread(new PostInfo(listener, ip, wifi(ssidStr, passwdStr)));
                    thread.start();
                    dismiss();
                } else {
                    listener.showErrorMessage("The ssid or password is not valid");
                }
            });
            builder.setCancelable(true);
            return builder.create();
        }

        private boolean isValid(String password){
            int length = password.length();
            return length >= 8 && length <= 63;
        }
    }
}