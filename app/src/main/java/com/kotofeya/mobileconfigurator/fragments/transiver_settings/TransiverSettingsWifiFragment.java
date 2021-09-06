package com.kotofeya.mobileconfigurator.fragments.transiver_settings;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
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
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.List;

public class TransiverSettingsWifiFragment extends TransiverSettingsFragment {


    private TextView settingsWifi;
    private Button showSettingsWifiBtn;
    private Button setDefaultWifiSettingsBtn;
    private Button addNewWifiSettingsBtn;


    @Override
    public View getView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.transiver_settings_wifi_fragment, container, false);
    }

    @Override
    public void updateUIBtns(List<Transiver> transivers) {
        updateBtnsState();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mainTxtLabel.setText("Wifi settings: " + ssid);
        settingsWifi = view.findViewById(R.id.settings_wifi);
        showSettingsWifiBtn = view.findViewById(R.id.show_settings_wifi);
        showSettingsWifiBtn.setOnClickListener(this);
        setDefaultWifiSettingsBtn = view.findViewById(R.id.set_default_wifi_settings);
        setDefaultWifiSettingsBtn.setOnClickListener(this);
        addNewWifiSettingsBtn = view.findViewById(R.id.add_new_wifi_settings);
        addNewWifiSettingsBtn.setOnClickListener(this);
        return view;
    }


    private void updateBtnsState() {
        if(utils.getVersion(ssid) != null && !utils.getVersion(ssid).equals("ssh_conn")){
            if(utils.getIp(ssid) != null){

                showSettingsWifiBtn.setEnabled(true);
                setDefaultWifiSettingsBtn.setEnabled(true);
                addNewWifiSettingsBtn.setEnabled(true);
            }
        }
    }

    @Override
    public void onClick(View v) {
        String ip = utils.getIp(ssid);
        Thread thread;
        switch (v.getId()){
            case R.id.show_settings_wifi:
                thread = new Thread(new PostInfo((TransiverSettingsWifiFragment) App.get().getFragmentHandler().getCurrentFragment(), ip,
                        PostCommand.READ_WPA));
                thread.start();
                break;

            case R.id.set_default_wifi_settings:
                thread = new Thread(new PostInfo((TransiverSettingsWifiFragment) App.get().getFragmentHandler().getCurrentFragment(), ip,
                        PostCommand.WIFI_CLEAR));
                thread.start();
                break;

            case R.id.add_new_wifi_settings:
                Bundle bundle = new Bundle();
                bundle.putString(BundleKeys.IP_KEY, ip);
                AddNewWifiSettingsDialog dialog = new AddNewWifiSettingsDialog();
                dialog.setArguments(bundle);
                dialog.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().ADD_NEW_WIFI_SETTINGS_DIALOG);
                break;
        }
    }

    @Override
    public void onTaskCompleted(Bundle result) {
        String command = result.getString(BundleKeys.COMMAND_KEY);
        String response = result.getString(BundleKeys.RESPONSE_KEY);
        if(command != null) {
            switch (command) {
                case PostCommand.READ_WPA:
                    if(response.isEmpty()){
                        response = "Empty response";
                    }
                    updateText(response);
                    break;
                case PostCommand.WIFI_CLEAR:
                    utils.showMessage((response.startsWith("Ok"))? "Настройки сброшены и примутся при перезапуске." : "Error");
                    break;
                case PostCommand.WIFI:
                    utils.showMessage((response.startsWith("Ok"))? "Новые параметры заданы и примутся при перезапуске." : "Error");
                    break;
                case POST_COMMAND_ERROR:
                    utils.showMessage(response);
                    break;
            }
        }
    }

    public void updateUI() {
        settingsWifi.setText(text);
    }

    public static class AddNewWifiSettingsDialog extends DialogFragment implements PostCommand{
        private String ip;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            this.ip = getArguments().getString(BundleKeys.IP_KEY);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getResources().getString(R.string.add_new_wifi_title));

            View v = LayoutInflater.from(getActivity()).inflate(R.layout.add_new_wifi_settings_dialog, null);
            builder.setView(v);

            EditText ssid = v.findViewById(R.id.add_wifi_ssid);
            EditText password = v.findViewById(R.id.add_wifi_password);
            Button addNewWifiSettingsBtn = v.findViewById(R.id.add_wifi_save_btn);
            addNewWifiSettingsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String ssidStr = ssid.getText().toString();
                    String passwdStr = password.getText().toString();
                    String valid = isValid(ssidStr, passwdStr);
                    if (valid.equals("valid")) {
                        Thread thread = new Thread(new PostInfo((TransiverSettingsWifiFragment) App.get().getFragmentHandler().getCurrentFragment(), ip,
                                wifi(new String[]{ssidStr, passwdStr})));
                        thread.start();
                        dismiss();
                    } else {
                        showErrorDialog().show();
                    }
                }
            });

            builder.setCancelable(true);
            return builder.create();
        }

        private String isValid(String ssid, String passw){
            if(passw.length() < 8){
                return "The password length must be greater than 8";
            }
            if(passw.length() > 63){
                return "The password length must be less than 63";
            }
            return "valid";
        }

        private Dialog showErrorDialog(){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("The ssid or password is not valid");
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });
            builder.setCancelable(true);
            return builder.create();
        }
    }
}