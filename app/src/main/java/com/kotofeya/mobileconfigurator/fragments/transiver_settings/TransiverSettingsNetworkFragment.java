package com.kotofeya.mobileconfigurator.fragments.transiver_settings;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.List;


public class TransiverSettingsNetworkFragment extends TransiverSettingsFragment {

    private TextView settingsNetwork;
    private Button showSettingsNetworkBtn;
    private Button setDefaultNetworkSettingsBtn;
    private Button setEthernetSettingsBtn;

    @Override
    public View getView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.transiver_settings_network_fragment, container, false);
    }

    @Override
    public void updateUIBtns(List<Transiver> transivers) {
        updateBtnsState();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mainTxtLabel.setText("Network settings: " + ssid);
        settingsNetwork= view.findViewById(R.id.settings_network);
        showSettingsNetworkBtn = view.findViewById(R.id.show_settings_network);
        showSettingsNetworkBtn.setOnClickListener(this);
        setDefaultNetworkSettingsBtn = view.findViewById(R.id.set_default_network_settings);
        setDefaultNetworkSettingsBtn.setOnClickListener(this);
        setEthernetSettingsBtn = view.findViewById(R.id.add_new_network_settings);
        setEthernetSettingsBtn.setOnClickListener(this);
        return view;
    }

    private void updateBtnsState() {
        if(utils.getVersion(ssid) != null && !utils.getVersion(ssid).equals("ssh_conn")){
            if(utils.getIp(ssid) != null){
                showSettingsNetworkBtn.setEnabled(true);
                setDefaultNetworkSettingsBtn.setEnabled(true);
                setEthernetSettingsBtn.setEnabled(true);
            }
        }
    }

    @Override
    public void onClick(View v) {
        String ip = utils.getIp(ssid);
        Thread thread;
        switch (v.getId()){
            case R.id.show_settings_network:
                thread = new Thread(new PostInfo((TransiverSettingsNetworkFragment) App.get().getFragmentHandler().getCurrentFragment(), ip,
                        PostCommand.READ_NETWORK));
                thread.start();
                break;
            case R.id.set_default_network_settings:
                thread = new Thread(new PostInfo((TransiverSettingsNetworkFragment) App.get().getFragmentHandler().getCurrentFragment(), ip,
                        PostCommand.NETWORK_CLEAR));
                thread.start();
                break;

            case R.id.add_new_network_settings:
                Bundle bundle = new Bundle();
                bundle.putString("ip", ip);
                AddNewEthernetSettingsDialog dialog = new AddNewEthernetSettingsDialog();
                dialog.setArguments(bundle);
                dialog.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().ADD_NEW_ETHERNET_SETTINGS_DIALOG);
                break;
        }
    }

    @Override
    public void onTaskCompleted(Bundle result) {
        String command = result.getString(BundleKeys.COMMAND_KEY);
        String response = result.getString(BundleKeys.RESPONSE_KEY);
        if(command != null) {
            switch (command) {
                case PostCommand.READ_NETWORK:
                    if(response.isEmpty()){
                        response = "Empty response";
                    }
                    updateText(response);
                    break;
                case PostCommand.NETWORK_CLEAR:
                    if(response.startsWith("Ok")){
                        utils.showMessage("Настройки сброшены и примутся при перезапуске.");
                    } else {
                        utils.showMessage("Error");
                    }
                    break;
                case PostCommand.STATIC:
                    if(response.startsWith("Ok")){
                        utils.showMessage("Настройки изменены и примутся при перезапуске.");
                    } else {
                        utils.showMessage("Error");
                    }
                    break;
                case POST_COMMAND_ERROR:
                    utils.showMessage(response);
                    break;
            }
        }
    }

    public void updateUI() {
        settingsNetwork.setText(text);
    }

    public static class AddNewEthernetSettingsDialog extends DialogFragment implements PostCommand{
        private String transIp;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            transIp = getArguments().getString("ip");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getResources().getString(R.string.set_ethernet_params_title));

            View v = LayoutInflater.from(getActivity()).inflate(R.layout.add_new_ethernet_settings_dialog, null);
            builder.setView(v);

            EditText ip = v.findViewById(R.id.add_ethernet_ip);
            EditText gate = v.findViewById(R.id.add_ethernet_gate);
            EditText mask = v.findViewById(R.id.add_ethernet_mask);

            InputFilter[] ipGateMaskFilters = new InputFilter[1];
            ipGateMaskFilters[0] = new InputFilter() {
                @Override
                public CharSequence filter(CharSequence source, int start, int end,
                                           android.text.Spanned dest, int dstart, int dend) {
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
                                if (Integer.valueOf(splits[i]) > 255) {
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
                }

            };
            ip.setFilters(ipGateMaskFilters);
            gate.setFilters(ipGateMaskFilters);
            mask.setFilters(ipGateMaskFilters);

            Button addNewEthernetParamsBtn = v.findViewById(R.id.add_ethernet_settings);
            addNewEthernetParamsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String ipStr = ip.getText().toString();
                    String gateStr = gate.getText().toString();
                    String maskStr = mask.getText().toString();
                    Thread thread = new Thread(new PostInfo((TransiverSettingsNetworkFragment) App.get().getFragmentHandler().getCurrentFragment(), transIp,
                            staticEthernet(new String[]{ipStr, gateStr, maskStr})));
                    thread.start();
                    dismiss();
                }
            });
            builder.setCancelable(true);
            return builder.create();
        }
    }
}