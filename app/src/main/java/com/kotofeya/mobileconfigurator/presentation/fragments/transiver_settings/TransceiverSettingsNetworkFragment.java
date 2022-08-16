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

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.presentation.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;

public class TransceiverSettingsNetworkFragment extends TransceiverSettingsFragment {



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        viewModel.setMainTxtLabel("Network settings: " + ssid);

        binding.showSettingsBtn.setText(getString(R.string.show_settings_network));
        binding.setDefaultSettings.setText(getString(R.string.set_default_network_settings));
        binding.addNewSettings.setText(getString(R.string.add_new_network_settings));

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
        return PostCommand.READ_NETWORK;
    }

    @Override
    protected String getDefaultSettingsCommand() {
        return PostCommand.NETWORK_CLEAR;
    }

    @Override
    protected DialogFragment getDialogSettings() {
        AddNewEthernetSettingsDialog dialog = new AddNewEthernetSettingsDialog();
        dialog.setListener(this);
        return dialog;
    }

    @Override
    protected String getAddSettingsCommand() {
        return FragmentHandler.ADD_NEW_ETHERNET_SETTINGS_DIALOG;
    }

    public static class AddNewEthernetSettingsDialog extends DialogFragment implements PostCommand{
        private String transIp;
        private OnTaskCompleted listener;

        public void setListener(OnTaskCompleted listener) {
            this.listener = listener;
        }

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
                Thread thread = new Thread(new PostInfo(listener,
                        transIp, staticEthernet(ipStr, gateStr, maskStr)));
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
}