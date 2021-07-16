package com.kotofeya.mobileconfigurator.fragments.update;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Downloader;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.activities.CustomViewModel;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.List;

public class TransiverSettingsWifiFragment extends Fragment implements View.OnClickListener, PostCommand, OnTaskCompleted {

    private final Handler myHandler = new Handler();
    private String text;
    public Context context;
    public Utils utils;
    protected CustomViewModel viewModel;
    protected String ssid;

    private TextView settingsWifi;
    private Button showSettingsWifiBtn;
    private Button setDefaultWifiSettingsBtn;
    private Button addNewWifiSettingsBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.transiver_settings_wifi_fragment, container, false);

        this.ssid = getArguments().getString("ssid");

        TextView mainTxtLabel = ((MainActivity)context).findViewById(R.id.main_txt_label);
        ImageButton mainBtnRescan = ((MainActivity)context).findViewById(R.id.main_btn_rescan);
        mainTxtLabel.setText("Wifi settings: " + ssid);
        mainBtnRescan.setVisibility(View.GONE);

        settingsWifi = view.findViewById(R.id.settings_wifi);
        showSettingsWifiBtn = view.findViewById(R.id.show_settings_wifi);
        showSettingsWifiBtn.setOnClickListener(this);
        setDefaultWifiSettingsBtn = view.findViewById(R.id.set_default_wifi_settings);
        setDefaultWifiSettingsBtn.setOnClickListener(this);
        addNewWifiSettingsBtn = view.findViewById(R.id.add_new_wifi_settings);
        addNewWifiSettingsBtn.setOnClickListener(this);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Logger.d(Logger.CONTENT_LOG, "on view created");
        super.onViewCreated(view, savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity(), new CustomViewModel.ModelFactory()).get(CustomViewModel.class);
        viewModel.getTransivers().observe(getViewLifecycleOwner(), this::updateUI);
//        transiver = viewModel.getTransiverBySsid(ssid);



    }


    private void updateUI(List<Transiver> transivers){
        updateBtnsState();
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
//                bundle.putString("key", content[which]);
//                bundle.putString("value", commonContent.get(content[which]));
                bundle.putString("ip", ip);
                AddNewWifiSettingsDialog dialog = new AddNewWifiSettingsDialog();
                dialog.setArguments(bundle);
                dialog.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().ADD_NEW_WIFI_SETTINGS_DIALOG);
                break;
        }
    }

    @Override
    public void onTaskCompleted(Bundle result) {
        String command = result.getString(PostInfo.COMMAND);
        String ip = result.getString(PostInfo.IP);
        String response = result.getString(PostInfo.RESPONSE);
        Logger.d(Logger.TRANSIVER_STM_LOG_LOG, "on task completed, result: " + result);
        Logger.d(Logger.TRANSIVER_STM_LOG_LOG, "command: " + command);
        Logger.d(Logger.TRANSIVER_STM_LOG_LOG, "ip: " + ip);
        Logger.d(Logger.TRANSIVER_STM_LOG_LOG, "response: " + response);
        if(command != null) {
            switch (command) {
                case PostCommand.READ_WPA:
                    if(response.isEmpty()){
                        response = "Empty response";
                    }
                    updateLogText(response);
                    break;
                case PostCommand.WIFI_CLEAR:
                    if(response.startsWith("Ok")){
                        utils.showMessage("Настройки сброшены и примутся при перезапуске.");
                    } else {
                        utils.showMessage("Error");
                    }
                    break;
                case PostCommand.WIFI:
                    if(response.startsWith("Ok")){
                        utils.showMessage("Новые параметры заданы и примутся при перезапуске.");
                    } else{
                        utils.showMessage("Error");
                    }
                    break;
                case POST_COMMAND_ERROR:
                    utils.showMessage(response);
                    break;
            }
        }
    }

    protected void updateUI() {
        settingsWifi.setText(text);
    }

    final Runnable updateRunnable = new Runnable() {
        public void run() {
            updateUI();
        }
    };

    public void updateLogText(String text){
        this.text = text;
        myHandler.post(updateRunnable);
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        this.utils = ((MainActivity) context).getUtils();
        super.onAttach(context);
    }

    @Override
    public void onProgressUpdate(Integer downloaded) {
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.d(Logger.CONTENT_LOG, "contentFragment onStart");
        this.ssid = getArguments().getString("ssid");
//        transiver = viewModel.getTransiverBySsid(ssid);
//        stopScan();
    }

    public static class AddNewWifiSettingsDialog extends DialogFragment implements PostCommand{
        private String ip;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            this.ip = getArguments().getString("ip");
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