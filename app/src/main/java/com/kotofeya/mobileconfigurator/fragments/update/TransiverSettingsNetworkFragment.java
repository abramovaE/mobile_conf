package com.kotofeya.mobileconfigurator.fragments.update;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.style.MaskFilterSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.activities.CustomViewModel;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.List;


public class TransiverSettingsNetworkFragment extends Fragment implements View.OnClickListener, PostCommand, OnTaskCompleted {


    private final Handler myHandler = new Handler();
    private String text;
    public Context context;
    public Utils utils;
    protected CustomViewModel viewModel;
    protected String ssid;

    private TextView settingsNetwork;
    private Button showSettingsNetworkBtn;
    private Button setDefaultNetworkSettingsBtn;
    private Button setEthernetSettingsBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.transiver_settings_network_fragment, container, false);

        this.ssid = getArguments().getString("ssid");

        TextView mainTxtLabel = ((MainActivity)context).findViewById(R.id.main_txt_label);
        ImageButton mainBtnRescan = ((MainActivity)context).findViewById(R.id.main_btn_rescan);
        mainTxtLabel.setText("Network settings: " + ssid);
        mainBtnRescan.setVisibility(View.GONE);

        settingsNetwork= view.findViewById(R.id.settings_network);
        showSettingsNetworkBtn = view.findViewById(R.id.show_settings_network);
        showSettingsNetworkBtn.setOnClickListener(this);
        setDefaultNetworkSettingsBtn = view.findViewById(R.id.set_default_network_settings);
        setDefaultNetworkSettingsBtn.setOnClickListener(this);
        setEthernetSettingsBtn = view.findViewById(R.id.add_new_network_settings);
        setEthernetSettingsBtn.setOnClickListener(this);
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
//                bundle.putString("key", content[which]);
//                bundle.putString("value", commonContent.get(content[which]));
                bundle.putString("ip", ip);
                AddNewEthernetSettingsDialog dialog = new AddNewEthernetSettingsDialog();
                dialog.setArguments(bundle);
                dialog.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().ADD_NEW_ETHERNET_SETTINGS_DIALOG);

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
                case PostCommand.READ_NETWORK:
                    if(response.isEmpty()){
                        response = "Empty response";
                    }
                    updateLogText(response);
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

    protected void updateUI() {

        settingsNetwork.setText(text);
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