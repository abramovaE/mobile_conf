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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.databinding.AddNewWifiSettingsDialogBinding;
import com.kotofeya.mobileconfigurator.databinding.TransiverSettingsFragmentBinding;
import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;
import com.kotofeya.mobileconfigurator.network.PostInfoListener;
import com.kotofeya.mobileconfigurator.presentation.activities.MainActivity;
import com.kotofeya.mobileconfigurator.presentation.activities.MainActivityViewModel;
import com.kotofeya.mobileconfigurator.presentation.fragments.FragmentHandler;

import java.util.List;

public class TransceiverSettingsWifiFragment extends Fragment
        implements PostCommand, PostInfoListener{

    public static final String TAG = TransceiverSettingsWifiFragment.class.getSimpleName();
    protected FragmentHandler fragmentHandler;
    protected TransiverSettingsFragmentBinding binding;
    protected MainActivityViewModel viewModel;
    protected String ssid;
    public static final String IP_KEY = "ip";


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

        binding.addNewSettings.setOnClickListener( v-> {
            String ip = transceiver.getIp();
            Bundle bundle = new Bundle();
            bundle.putString("ip", ip);
            DialogFragment dialog = getDialogSettings();
            dialog.setArguments(bundle);
            dialog.show(fragmentHandler.getFragmentManager(), getAddSettingsCommand());
        });

        viewModel.setMainTxtLabel("Wifi settings: " + ssid);
        binding.showSettingsBtn.setText(getString(R.string.show_settings_wifi));
        binding.setDefaultSettings.setText(getString(R.string.set_default_wifi_settings));
        binding.addNewSettings.setText(getString(R.string.add_new_wifi_settings));
        binding.setDefaultSettings.setOnClickListener(v -> {
            String ip = transceiver.getIp();
            Thread thread = new Thread(new PostInfo(ip, PostCommand.WIFI_CLEAR, new PostInfoListener() {
                @Override
                public void postInfoSuccessful(String ip, String response) {
                    fragmentHandler.showMessage((response.startsWith("Ok")) ?
                            "Настройки сброшены и примутся при перезапуске." : "Error");
                }
                @Override
                public void postInfoFailed(String error) {

                }
            }));
            thread.start();
        });
        binding.showSettingsBtn.setOnClickListener( v-> {
            String ip = transceiver.getIp();
            Thread thread = new Thread(new PostInfo(ip, PostCommand.READ_WPA, new PostInfoListener() {
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
        });
        return binding.getRoot();
    }


    public void updateButtonsState() {
        binding.showSettingsBtn.setEnabled(true);
        binding.setDefaultSettings.setEnabled(true);
        binding.addNewSettings.setEnabled(true);
    }

    protected DialogFragment getDialogSettings() {
        return new AddNewWifiSettingsDialog();
    }

    protected String getAddSettingsCommand() {
        return FragmentHandler.ADD_NEW_WIFI_SETTINGS_DIALOG;
    }


    @Override
    public void postInfoSuccessful(String ip, String response) {}

    @Override
    public void postInfoFailed(String error) {}

    public static class AddNewWifiSettingsDialog extends DialogFragment implements PostCommand{
        private String ip;
        private AddNewWifiSettingsDialogBinding binding;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            this.ip = requireArguments().getString(IP_KEY);
            binding = AddNewWifiSettingsDialogBinding.inflate(getLayoutInflater());
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(getResources().getString(R.string.add_new_wifi_title));
            builder.setView(binding.getRoot());
            binding.addWifiSaveBtn.setOnClickListener(v1 -> {
                String ssidStr = binding.addWifiSsid.getText().toString();
                String passwdStr = binding.addWifiPassword.getText().toString();
                if (isValid(passwdStr)) {
                    Thread thread = new Thread(new PostInfo(ip, wifi(ssidStr, passwdStr), new PostInfoListener() {
                        @Override
                        public void postInfoSuccessful(String ip, String response) {
                            ((MainActivity) requireActivity()).getFragmentHandler()
                                    .showMessage((response.startsWith("Ok")) ?
                                    "Новые параметры заданы и примутся при перезапуске." : "Error");
                        }

                        @Override
                        public void postInfoFailed(String error) {
                            ((MainActivity) requireActivity()).getFragmentHandler()
                                    .showMessage("Error");
                        }
                    }));
                    thread.start();
                    dismiss();
                } else {
                    ((MainActivity) requireActivity()).getFragmentHandler()
                            .showMessage("The ssid or password is not valid");
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