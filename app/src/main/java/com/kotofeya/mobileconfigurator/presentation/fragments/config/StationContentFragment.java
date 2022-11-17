package com.kotofeya.mobileconfigurator.presentation.fragments.config;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.databinding.ContentFragmentBinding;
import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;
import com.kotofeya.mobileconfigurator.network.PostInfoListener;
import com.kotofeya.mobileconfigurator.network.request.SshTakeInfoListener;
import com.kotofeya.mobileconfigurator.network.request.SshTakeInfoUseCase;
import com.kotofeya.mobileconfigurator.network.request.SendSshCommandListener;
import com.kotofeya.mobileconfigurator.network.request.SendSshCommandUseCase;
import com.kotofeya.mobileconfigurator.presentation.activities.MainActivity;
import com.kotofeya.mobileconfigurator.presentation.activities.MainActivityViewModel;
import com.kotofeya.mobileconfigurator.presentation.fragments.FragmentHandler;

import java.util.List;

public class StationContentFragment  extends Fragment
        implements
        PostCommand,
        View.OnClickListener {

    public static final String FLOOR_COMMAND =
            "/usr/local/bin/call --cmd FLOOR";
    public static final String ZUMMER_TYPE_COMMAND =
            "/usr/local/bin/call --cmd SNDTYPE";
    public static final String MODEM_CONFIG_MEGAF_BEELINE_COMMAND =
            "sudo sed -I ’s/megafon-m2m/beeline-m2m/g’ /etc/init.d/S99stp-tools";
    public static final String MODEM_CONFIG_BEELINE_MEGAF_COMMAND =
            "sudo sed -I ’s/beeline-m2m/megafon-m2m/g’ /etc/init.d/S99stp-tools";

    EditText floorTxt;
    Spinner zumTypesSpn;
    Spinner zumVolumeSpn;
    Spinner modemConfigSpn;
    String[] modemConfigs;
    Transceiver statTransceiver;

    public static final String TAG = StationContentFragment.class.getSimpleName();

    private final Handler myHandler = new Handler();

    protected ContentFragmentBinding binding;

    protected MainActivityViewModel viewModel;
    protected String ssid;
    protected View.OnKeyListener onKeyListener;
    protected AdapterView.OnItemSelectedListener onItemSelectedListener;
    protected TextWatcher textWatcher;

    protected Transceiver currentTransceiver;

    protected FragmentHandler fragmentHandler;

    protected void setFields() {
        statTransceiver = viewModel.getTransceiverBySsid(ssid);
        viewModel.setMainTxtLabel(statTransceiver.getSsid());

        floorTxt = binding.contentTxt0;
        floorTxt.setVisibility(View.VISIBLE);
        floorTxt.addTextChangedListener(textWatcher);
        floorTxt.setOnKeyListener(onKeyListener);
        floorTxt.setHint(getString(R.string.floor_hint));

        zumTypesSpn = binding.contentSpn0;
        String[] zumTypes = getResources().getStringArray(R.array.zummer_types);
        ArrayAdapter<String> zumTypesAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item, zumTypes);
        zumTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        zumTypesSpn.setAdapter(zumTypesAdapter);
        zumTypesSpn.setVisibility(View.VISIBLE);
        zumTypesSpn.setOnItemSelectedListener(onItemSelectedListener);
        zumVolumeSpn = binding.contentSpn1;
        zumVolumeSpn.setVisibility(View.VISIBLE);

        String[] zumVolume = getZumVolume();

        ArrayAdapter<String> zumVolumeAdapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item, zumVolume);
        zumVolumeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        zumVolumeSpn.setAdapter(zumVolumeAdapter);
        zumVolumeSpn.setVisibility(View.VISIBLE);
        zumVolumeSpn.setOnItemSelectedListener(onItemSelectedListener);
        modemConfigSpn = binding.contentSpn2;
        modemConfigSpn.setVisibility(View.VISIBLE);
        modemConfigs = getResources().getStringArray(R.array.modem_types);

        ArrayAdapter<String> modemConfigAdapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item, modemConfigs);
        modemConfigAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modemConfigSpn.setAdapter(modemConfigAdapter);
        modemConfigSpn.setVisibility(View.VISIBLE);
        modemConfigSpn.setOnItemSelectedListener(onItemSelectedListener);
        setModem();
        updateBtnContentSendState();
        binding.contentBtnSend.setOnClickListener(this);
    }

    private String[] getZumVolume(){
        String[] zumVolume = new String[11];
        zumVolume[0] = getResources().getString(R.string.zummer_volume_hint);
        for(int i = 1; i < 11; i ++){
            zumVolume[i] = i * 10 + "";
        }
        return zumVolume;
    }

    private void setModem(){
        if(statTransceiver.getModem() != null){
            for(int i = 0; i < modemConfigs.length; i++){
                if(modemConfigs[i].equalsIgnoreCase(statTransceiver.getModem())){
                    modemConfigSpn.setSelection(i);
                }
            }
        } else {
            Transceiver transceiver = viewModel.getTransceiverBySsid(statTransceiver.getSsid());
            String ip = transceiver.getIp();
            String version = transceiver.getVersion();
            if(version != null && version.equals("ssh_conn")) {
                new Thread(new SshTakeInfoUseCase(new SshTakeInfoListener() {
                    @Override
                    public void sshTakeInfoSuccessful(String response) {
                        Logger.d(TAG, "sshTakeInfoSuccessful(): " + response);
                    }

                    @Override
                    public void sshTakeInfoFailed(String ip, String error) {
                        Logger.d(TAG, "sshTakeInfoFailed(): " + error);
                    }
                }, ip)).start();
            }
        }
    }
    protected void updateBtnContentSendState(){
        Transceiver transceiver = viewModel.getTransceiverBySsid(statTransceiver.getSsid());
        String ip = transceiver.getIp();
        String version = transceiver.getVersion();

        Logger.d(Logger.STATION_CONTENT_LOG, "update btn content send state: ip - "
                + ip + ", version: " + version);
        binding.contentBtnSend.setEnabled((!floorTxt.getText().toString().isEmpty()
                || zumTypesSpn.getSelectedItemPosition() > 0
                || zumVolumeSpn.getSelectedItemPosition() > 0
                || modemConfigSpn.getSelectedItemPosition() > 0)
                && ip != null && version != null);
    }

    public void updateFields() {
        setModem();
    }

    private void appendNewFloor(StringBuilder command){
        String floorSend = floorTxt.getText().toString();
        if(!floorSend.isEmpty()){
            command.append(FLOOR_COMMAND);
            command.append(" ");
            command.append(floorSend);
        }
    }

    private void appendNewZumType(StringBuilder command){
        String zumTypeSend = zumTypesSpn.getSelectedItem().toString();
        if(zumTypeSend.isEmpty()
                && !zumTypeSend.equals(getResources().getStringArray(R.array.zummer_types)[0])){
            if(!command.toString().isEmpty()){
                command.append(";");
            }
            command.append(ZUMMER_TYPE_COMMAND);
            command.append(" ");
            if(zumTypeSend.equalsIgnoreCase("room")){command.append(1);}
            else if(zumTypeSend.equalsIgnoreCase("street")){command.append(2);}
        }
    }

    private void appendModem(StringBuilder command){
        String modemConfigSend = modemConfigSpn.getTransitionName();
        if(!modemConfigSend.isEmpty()
                && !modemConfigSend.equals(getResources().getStringArray(R.array.modem_types)[0])){
            if(!command.toString().isEmpty()){
                command.append(";");
            }
            if(statTransceiver.getModem().equalsIgnoreCase("megafon")
                    && modemConfigSend.equalsIgnoreCase("beeline")){
                command.append(MODEM_CONFIG_MEGAF_BEELINE_COMMAND);
            }
            else if(statTransceiver.getModem().equalsIgnoreCase("beeline")
                    && modemConfigSend.equalsIgnoreCase("megafon")){
                command.append(MODEM_CONFIG_BEELINE_MEGAF_COMMAND);
            }
        }
    }

    private String getSshCommand(){
        StringBuilder command = new StringBuilder();
        appendNewFloor(command);
        appendNewZumType(command);
        appendModem(command);
        return command.toString();
    }

    @Override
    public void onClick(View v) {
        Transceiver transceiver = viewModel.getTransceiverBySsid(statTransceiver.getSsid());
        String ip = transceiver.getIp();
        String version = transceiver.getVersion();
        String floorSend = floorTxt.getText().toString();

//        zumTypeSend = zumTypesSpn.getSelectedItem().toString();
//        zumVolumeSend = zumVolumeSpn.getSelectedItem().toString();

        if(version != null){
            if(version.equals("ssh_conn")){
                setSettingsBySsh(ip);
            } else {
                if(!floorSend.isEmpty()) {
                    setFloor(ip);
                }
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.d(TAG, "onViewCreated()");
        currentTransceiver = viewModel.getTransceiverBySsid(ssid);
        binding.spoilerBtn.setOnClickListener(v -> {
            if (v.getId() == R.id.spoilerBtn) {
                TextView contentLabel = view.findViewById(R.id.content_label);
                LinearLayout rebootLl = view.findViewById(R.id.reboot_ll);
                contentLabel.setVisibility(contentLabel.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                rebootLl.setVisibility(rebootLl.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                binding.contentBtnClear.setVisibility(binding.contentBtnClear.getVisibility() == View.VISIBLE ?
                        View.GONE : View.VISIBLE);
            }
        });
        viewModel.transceivers.observe(getViewLifecycleOwner(), this::updateInformers);
        setFields();


        setContentBtnRaspListener();
        setContentBtnStmListener();
        setContentBtnClearListener();
        setContentBtnAllListener();
    }

    private void setSettingsBySsh(String ip){
        String command = getSshCommand();

        new Thread(new SendSshCommandUseCase(ip, new SendSshCommandListener() {
            @Override
            public void sendSshCommandSuccessful(String response) {
                Logger.d(TAG, "updateStationaryContentSuccessful(), response: " + response);
                if(response.contains("Tested")){
                    fragmentHandler.showMessage(getString(R.string.content_updated));
                    (requireActivity()).onBackPressed();
                }
                StationContentFragment.this.refreshButtons();
            }
            @Override
            public void sendSshCommandFailed(String error) {
                Logger.d(TAG, "updateStationaryContentFailed(), error: " + error);
                if(!error.contains("Connection refused")) {
                    fragmentHandler.showMessage("Error");
                }
                StationContentFragment.this.refreshButtons();
            }
        }, command)).start();
    }

    private void setFloor(String ip){
        String floorSend = floorTxt.getText().toString();
        Thread thread = new Thread(new PostInfo(ip, floor(Integer.parseInt(floorSend)), new PostInfoListener() {
            @Override
            public void postInfoSuccessful(String ip, String response) {
                fragmentHandler.showMessage("Floor changed");
                fragmentHandler.changeFragment(FragmentHandler.CONFIG_STATION_FRAGMENT, false);
                changeSoundType(ip);
            }
            @Override
            public void postInfoFailed(String error) {
                Logger.d(TAG, "postInfoFailed(): " + error);
                fragmentHandler.showMessage("Floor change error: " + error);
            }
        }));
        thread.start();
    }

    private void changeSoundType(String ip){
        int zumType = 0;
        String zumTypeSend = zumTypesSpn.getSelectedItem().toString();
        if(zumTypeSend.equals(getResources().getStringArray(R.array.zummer_types)[1])){
            zumType = 2;
        } else if(zumTypeSend.equals(getResources().getStringArray(R.array.zummer_types)[2])){
            zumType = 1;
        }


        Thread thread = new Thread(new PostInfo(ip, sound(zumType), new PostInfoListener() {
            @Override
            public void postInfoSuccessful(String ip, String response) {
                Logger.d(TAG, "postInfoSuccessful(): " + response);
                fragmentHandler.showMessage("Sound type changed");
                fragmentHandler.changeFragment(FragmentHandler.CONFIG_STATION_FRAGMENT, false);
                setZummerVolume(ip);
            }
            @Override
            public void postInfoFailed(String error) {
                Logger.d(TAG, "postInfoFailed(): " + error);
                fragmentHandler.showMessage("Sound type change error: " + error);
            }
        }));
        thread.start();
    }
    private void setZummerVolume(String ip){
        try {
            String zumVolumeSend = zumVolumeSpn.getSelectedItem().toString();
            int zummerVolumeSend = Integer.parseInt(zumVolumeSend);
            Logger.d(TAG, "zummerVolumeSend: " + zummerVolumeSend);
            Thread thread = new Thread(new PostInfo(ip, volume(zummerVolumeSend),
                    new PostInfoListener() {
                        @Override
                        public void postInfoSuccessful(String ip, String response) {
                            Logger.d(TAG, "postInfoSuccessful(): " + response);
                            fragmentHandler.showMessage("Volume changed");
                            fragmentHandler.changeFragment(FragmentHandler.CONFIG_STATION_FRAGMENT,
                                    false);
                        }
                        @Override
                        public void postInfoFailed(String error) {
                            Logger.d(TAG, "postInfoFailed(): " + error);
                            fragmentHandler.showMessage("Volume change error: " + error);
                        }
                    }));
            thread.start();
        } catch (Exception e){
            Logger.d(TAG, "exc: " + e.getMessage());
        }
    }

    private void setContentBtnRaspListener(){
        binding.contentBtnRasp.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(R.string.confirmation_is_required);
            builder.setMessage(getString(R.string.confirm_reboot_of) + " " + PostCommand.REBOOT_RASP);
            builder.setPositiveButton(R.string.ok, (dialog, which) -> {
                String ip = currentTransceiver.getIp();
                String version = currentTransceiver.getVersion();
                if(version != null) {
                    if (!version.equals("ssh_conn")) {
                        rebootRaspByPost(ip);
                    } else {
                        rebootRaspBySsh(ip);
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> {});
            builder.setCancelable(true);
            builder.create().show();
        });
    }

    private void rebootRaspByPost(String ip){
        Thread thread = new Thread(new PostInfo(ip,
                PostCommand.REBOOT + "_" + PostCommand.REBOOT_RASP, new PostInfoListener() {
            @Override
            public void postInfoSuccessful(String ip, String response) {
                fragmentHandler.changeFragment(FragmentHandler.CONFIG_STATION_FRAGMENT, false);
            }
            @Override
            public void postInfoFailed(String error) {}
        }));
        thread.start();
    }
    private void rebootRaspBySsh(String ip){
        new Thread(new SendSshCommandUseCase(ip, new SendSshCommandListener()  {
            @Override
            public void sendSshCommandSuccessful(String response) {
                Logger.d(TAG, "rebootRaspSuccessful()");
                (requireActivity()).onBackPressed();
            }
            @Override
            public void sendSshCommandFailed(String message) {
                Logger.d(TAG, "rebootRaspFailed()");
            }
        }, PostCommand.REBOOT_COMMAND)).start();
    }

    private void rebootStmBySsh(String ip){
        new Thread(new SendSshCommandUseCase(ip, new SendSshCommandListener(){
            @Override
            public void sendSshCommandSuccessful(String response) {
                Logger.d(TAG, "rebootStmSuccessful()");
                if (response.contains("Tested")) {
                    (requireActivity()).runOnUiThread(() -> {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(requireActivity());
                        builder1.setMessage(getString(R.string.stm_rebooted));
                        builder1.setPositiveButton(R.string.ok, (dialog, id) -> {
                        });
                        builder1.setCancelable(true);
                        builder1.show();
                    });
                }
            }

            @Override
            public void sendSshCommandFailed(String error) {

            }
        }, PostCommand.REBOOT_STM_COMMAND)).start();
    }
    private void rebootStmByPost(String ip){
        Thread thread = new Thread(new PostInfo(ip,
                PostCommand.REBOOT + "_" + PostCommand.REBOOT_STM, new PostInfoListener() {
            @Override
            public void postInfoSuccessful(String ip, String response) {
                fragmentHandler.showMessage((response.startsWith("Ok")) ?
                        getString(R.string.stm_rebooted) :
                        "reboot stm error ");

            }
            @Override
            public void postInfoFailed(String error) {
            }
        }));
        thread.start();
    }

    private void clearRaspBySsh(String ip){
        Thread thread = new Thread(new PostInfo(ip,
                PostCommand.ERASE_CONTENT, new PostInfoListener() {
            @Override
            public void postInfoSuccessful(String ip, String response) {
                fragmentHandler.showMessage(getString(R.string.rasp_was_cleared));
                fragmentHandler.changeFragment(FragmentHandler.CONFIG_STATION_FRAGMENT, false);
            }
            @Override
            public void postInfoFailed(String error) {
                fragmentHandler.showMessage("Clear rasp error : " + error);
            }
        }));
        thread.start();
    }
    private void clearRaspByPost(String ip){
        new Thread(new SendSshCommandUseCase(ip, new SendSshCommandListener() {
            @Override
            public void sendSshCommandSuccessful(String result) {
                (requireActivity()).runOnUiThread(() -> {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(requireActivity());
                    builder1.setMessage(getString(R.string.rasp_was_cleared));
                    builder1.setPositiveButton(R.string.ok, (dialog, id) -> {});
                    builder1.setCancelable(true);
                    builder1.show();
                });
            }

            @Override
            public void sendSshCommandFailed(String error) {
                Logger.d(TAG, "clearRaspFailed()");
            }
        }, PostCommand.CLEAR_RASP_COMMAND)).start();
    }

    private void rebootAllBySsh(String ip){
        Thread thread = new Thread(new PostInfo(ip,
                PostCommand.REBOOT + "_" + PostCommand.REBOOT_ALL, new PostInfoListener() {
            @Override
            public void postInfoSuccessful(String ip, String response) {
                fragmentHandler.showMessage(getString(R.string.all_rebooted));
                fragmentHandler.changeFragment(FragmentHandler.CONFIG_STATION_FRAGMENT,
                        false);
            }
            @Override
            public void postInfoFailed(String error) {
                fragmentHandler.showMessage("Reboot all error: " + error);
            }
        }));
        thread.start();
    }


    private void setContentBtnStmListener(){
        binding.contentBtnStm.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(R.string.confirmation_is_required);
            builder.setMessage(getString(R.string.confirm_reboot_of) + " " + PostCommand.REBOOT_STM);
            builder.setPositiveButton(R.string.ok, (dialog, which) -> {
                String ip = currentTransceiver.getIp();
                String version = currentTransceiver.getVersion();

                if (version != null) {
                    if (!version.equals("ssh_conn")) {
                        rebootStmByPost(ip);
                    } else {
                        rebootStmBySsh(ip);
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> {});
            builder.setCancelable(true);
            builder.create().show();
        });
    }
    private void setContentBtnClearListener(){
        binding.contentBtnClear.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(R.string.confirmation_is_required);
            builder.setMessage(R.string.ask_clear_the_transiver);

            builder.setPositiveButton(R.string.ok, (dialog, which) -> {
                String ip = currentTransceiver.getIp();
                String version = currentTransceiver.getVersion();
                if(version != null) {
                    if (!version.equals("ssh_conn")) {
                        clearRaspBySsh(ip);
                    } else {
                        clearRaspByPost(ip);
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> {});
            builder.setCancelable(true);
            builder.create().show();
        });
    }
    private void setContentBtnAllListener(){
        binding.contentBtnAll.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(R.string.confirmation_is_required);
            builder.setMessage(getString(R.string.confirm_reboot_of) + " " + PostCommand.REBOOT_ALL);
            builder.setPositiveButton(R.string.ok, (dialog, which) -> {
                String ip = currentTransceiver.getIp();
                String version = currentTransceiver.getVersion();
                if(version != null) {
                    if (!version.equals("ssh_conn")) {
                        rebootAllBySsh(ip);
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> {});
            builder.setCancelable(true);
            builder.create().show();
        });
    }



    private void updateUI() {
        Logger.d(TAG, "update ui, ssid " + ssid + " " + currentTransceiver.getSsid());
        currentTransceiver = viewModel.getTransceiverBySsid(currentTransceiver.getSsid());
        if(!currentTransceiver.getVersion().equals(Transceiver.VERSION_UNDEFINED)) {
            binding.contentBtnRasp.setEnabled(true);
            binding.contentBtnStm.setEnabled(true);
            binding.contentBtnClear.setEnabled(true);
            binding.contentBtnSend.setEnabled(true);
            if(!currentTransceiver.getVersion().equals("ssh_conn")){
                binding.contentBtnAll.setVisibility(View.VISIBLE);
                binding.contentBtnAll.setEnabled(true);
            }
        }
        updateFields();
    }

    final Runnable updateRunnable = this::updateUI;

    @Override
    public void onAttach(@NonNull Context context) {

        super.onAttach(context);

        onKeyListener = (v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                InputMethodManager in = (InputMethodManager) App.get().getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(v.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                return true;
            }
            return false;
        };

        onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateBtnContentSendState();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateBtnContentSendState();
            }
        };

        textWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                updateBtnContentSendState();
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = ContentFragmentBinding.inflate(inflater, container, false);
        fragmentHandler = ((MainActivity) requireActivity()).getFragmentHandler();
        this.ssid = getArguments().getString(BundleKeys.SSID_KEY);
        viewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);
        viewModel.isScanning.observe(getViewLifecycleOwner(), this::updateClientsScanFinished);
        return binding.getRoot();
    }

    private void updateClientsScanFinished(Boolean aBoolean) {
        if(!aBoolean){
            viewModel.pollConnectedClients();
        }
    }



    private void updateInformers(List<Transceiver> transceivers){
        updateFields();
        Logger.d(TAG, "update informers, current ssid: " + ssid);
        Logger.d(TAG, "update informers, transceivers: " + transceivers);
        Transceiver transceiver = viewModel.getTransceiverBySsid(ssid);
        if(transceiver != null){
            Logger.d(TAG, "transceiver: " + transceiver);
            currentTransceiver = transceiver;
            if(currentTransceiver.getSsid() != null){
                updateBtnContentSendState();
                updateUI();
            }
        }
    }



    private void basicScan(){
        Logger.d(TAG, "wifi scan");
        viewModel.pollConnectedClients();
    }


    @Override
    public void onStart() {
        super.onStart();
        Logger.d(TAG, "contentFragment onStart");
        String ssid = getArguments().getString(BundleKeys.SSID_KEY);
        currentTransceiver = viewModel.getTransceiverBySsid(ssid);
        if(!refreshButtons()){
            basicScan();
        }
        viewModel.setMainBtnRescanVisibility(View.GONE);
    }

    public boolean refreshButtons(){
        Transceiver transceiver = viewModel.getTransceiverBySsid(currentTransceiver.getSsid());
        Logger.d(TAG, "refresh buttons, currentTransceiverIp: " + transceiver.getIp());
        if(currentTransceiver.getIp() != null || transceiver.getIp() != null){
            myHandler.post(updateRunnable);
            return true;
        }
        return false;
    }

}