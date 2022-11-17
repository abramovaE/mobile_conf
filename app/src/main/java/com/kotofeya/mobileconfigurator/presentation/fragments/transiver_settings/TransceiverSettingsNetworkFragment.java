package com.kotofeya.mobileconfigurator.presentation.fragments.transiver_settings;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.Logger;
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

public class TransceiverSettingsNetworkFragment extends Fragment
        implements PostCommand, PostInfoListener {

    public static final String TAG = TransceiverSettingsNetworkFragment.class.getSimpleName();
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
        binding.addNewSettings.setOnClickListener( v-> {
            String ip = transceiver.getIp();
            Bundle bundle = new Bundle();
            bundle.putString("ip", ip);
            DialogFragment dialog = getDialogSettings();
            dialog.setArguments(bundle);
            dialog.show(fragmentHandler.getFragmentManager(), getAddSettingsCommand());
        });


        ///
        viewModel.setMainTxtLabel("Network settings: " + ssid);

        binding.showSettingsBtn.setText(getString(R.string.show_settings_network));
        binding.setDefaultSettings.setText(getString(R.string.set_default_network_settings));
        binding.addNewSettings.setText(getString(R.string.add_new_network_settings));

        binding.setDefaultSettings.setOnClickListener(v -> {
            String ip = transceiver.getIp();
            Thread thread = new Thread(new PostInfo(ip, PostCommand.NETWORK_CLEAR, new PostInfoListener() {
                @Override
                public void postInfoSuccessful(String ip, String response) {
                    fragmentHandler.showMessage((response.startsWith("Ok")) ?
                            "Настройки сброшены и примутся при перезапуске." : "Error");
                }

                @Override
                public void postInfoFailed(String error) {
                    Logger.d(TAG, "postInfoFailed(), error: " + error);
                }
            }));
            thread.start();

        });
        binding.showSettingsBtn.setOnClickListener( v-> {
            String ip = transceiver.getIp();
            Thread thread = new Thread(new PostInfo(ip, PostCommand.READ_NETWORK, new PostInfoListener() {
                @Override
                public void postInfoSuccessful(String ip, String response) {
                    response = response.isEmpty() ? "Empty response" : response;
                    viewModel.setTransceiverSettingsText(response);
                }

                @Override
                public void postInfoFailed(String error) {
                    Logger.d(TAG, "postInfoFailed(), error: " + error);

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
        return new AddNewEthernetSettingsDialog();
    }
    protected String getAddSettingsCommand() {
        return FragmentHandler.ADD_NEW_ETHERNET_SETTINGS_DIALOG;
    }

    @Override
    public void postInfoSuccessful(String ip, String response) {}

    @Override
    public void postInfoFailed(String error) {}

    public static class AddNewEthernetSettingsDialog
            extends DialogFragment
            implements PostCommand{
        private String transIp;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            transIp = getArguments().getString("ip");
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(getResources().getString(R.string.set_ethernet_params_title));

            View v = LayoutInflater.from(getActivity()).inflate(R.layout.add_new_ethernet_settings_dialog, null);
            builder.setView(v);

            EditText ip = v.findViewById(R.id.add_ethernet_ip);
            EditText gate = v.findViewById(R.id.add_ethernet_gate);
            EditText mask = v.findViewById(R.id.add_ethernet_mask);

            InputFilter[] ipGateMaskFilters = getIpGateMaskFilter();
            ip.setFilters(ipGateMaskFilters);
            gate.setFilters(ipGateMaskFilters);
            mask.setFilters(ipGateMaskFilters);

            Button addNewEthernetParamsBtn = v.findViewById(R.id.add_ethernet_settings);
            addNewEthernetParamsBtn.setOnClickListener(v1 -> {
                String ipStr = ip.getText().toString();
                String gateStr = gate.getText().toString();
                String maskStr = mask.getText().toString();
                Thread thread = new Thread(new PostInfo(
                        transIp, staticEthernet(ipStr, gateStr, maskStr), new PostInfoListener() {
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
            });
            builder.setCancelable(true);
            return builder.create();
        }
    }

    private static InputFilter[] getIpGateMaskFilter(){
        InputFilter[] ipGateMaskFilters = new InputFilter[1];
        ipGateMaskFilters[0] = (source, start, end, dest, dstart, dend) -> {
            if (end > start) {
                String destTxt = dest.toString();
                String resultingTxt = destTxt.substring(0, dstart)
                        + source.subSequence(start, end)
                        + destTxt.substring(dend);
                if (!resultingTxt
                        .matches("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                    return "";
                } else {
                    String[] splits = resultingTxt.split("\\.");
                    for (int i = 0; i < splits.length; i++) {
                        if (Integer.parseInt(splits[i]) > 255) {
                            String resulting = "";
                            for (int j = 0; j < i; j++) {
                                resulting += splits[j];
                                if(splits.length < 5){
                                    resulting += ".";
                                }
                            }
                            Logger.d(Logger.MAIN_LOG, "return: " + resulting);
                            return resulting;
                        }
                    }
                }
            }
            Logger.d(Logger.MAIN_LOG, "return null");
            return null;
        };
        return ipGateMaskFilters;
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