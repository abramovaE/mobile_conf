package com.kotofeya.mobileconfigurator.fragments.config;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.SshTakeInfoConnectionRunnable;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;

public class StationContentFragment extends ContentFragment {
    EditText floorTxt;
    Spinner zumTypesSpn;
    Spinner zumVolumeSpn;
    Spinner modemConfigSpn;
    String[] modemConfigs;
    Transceiver statTransiver;
    String zumTypeSend;
    String zumVolumeSend;

    @Override
    protected void setFields() {
        statTransiver = viewModel.getTransceiverBySsid(ssid);
        viewModel.setMainTxtLabel(statTransiver.getSsid());

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
        String[] zumVolume = new String[11];
        zumVolume[0] = getResources().getString(R.string.zummer_volume_hint);
        for(int i = 1; i < 11; i ++){
            zumVolume[i] = i * 10 + "";
        }
        ArrayAdapter<String> zumVolumeAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item, zumVolume);
        zumVolumeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        zumVolumeSpn.setAdapter(zumVolumeAdapter);
        zumVolumeSpn.setVisibility(View.VISIBLE);
        zumVolumeSpn.setOnItemSelectedListener(onItemSelectedListener);
        modemConfigSpn = binding.contentSpn2;
        modemConfigSpn.setVisibility(View.VISIBLE);
        modemConfigs = getResources().getStringArray(R.array.modem_types);
        ArrayAdapter<String> modemConfigAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item, modemConfigs);
        modemConfigAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modemConfigSpn.setAdapter(modemConfigAdapter);
        modemConfigSpn.setVisibility(View.VISIBLE);
        modemConfigSpn.setOnItemSelectedListener(onItemSelectedListener);
        setModem();
        updateBtnContentSendState();
        binding.contentBtnSend.setOnClickListener(this);
    }

    private void setModem(){
        if(statTransiver.getModem() != null){
            for(int i = 0; i < modemConfigs.length; i++){
                if(modemConfigs[i].equalsIgnoreCase(statTransiver.getModem())){
                    modemConfigSpn.setSelection(i);
                }
            }
        } else {
            Transceiver transceiver = viewModel.getTransceiverBySsid(statTransiver.getSsid());
            String ip = transceiver.getIp();
            String version = transceiver.getVersion();
            if(version != null && version.equals("ssh_conn")) {
                new SshTakeInfoConnectionRunnable(((MainActivity) requireActivity()), ip);
            }
        }
    }
    protected void updateBtnContentSendState(){
        Transceiver transceiver = viewModel.getTransceiverBySsid(statTransiver.getSsid());
        String ip = transceiver.getIp();
        String version = transceiver.getVersion();

        Logger.d(Logger.STATION_CONTEN_LOG, "update btn content send state: ip - " + ip + ", version: " + version);
        binding.contentBtnSend.setEnabled((!floorTxt.getText().toString().isEmpty()
                || zumTypesSpn.getSelectedItemPosition() > 0
                || zumVolumeSpn.getSelectedItemPosition() > 0
                || modemConfigSpn.getSelectedItemPosition() > 0)
                && ip != null && version != null);
    }

    @Override
    public void updateFields() {
        setModem();
    }

    @Override
    public void onClick(View v) {
        Transceiver transceiver = viewModel.getTransceiverBySsid(statTransiver.getSsid());
        String ip = transceiver.getIp();
        String version = transceiver.getVersion();
        String floorSend = floorTxt.getText().toString();
        zumTypeSend = zumTypesSpn.getSelectedItem().toString();
        zumVolumeSend = zumVolumeSpn.getSelectedItem().toString();
        String modemConfigSend = modemConfigSpn.getTransitionName();
        if(version != null && version.equals("ssh_conn")){
            StringBuilder command = new StringBuilder();
            if(!floorSend.isEmpty()){
                command.append(SshConnection.FLOOR_COMMAND);
                command.append(" ");
                command.append(floorSend);
            }
            if(zumTypeSend != null && !zumTypeSend.isEmpty()
                    && !zumTypeSend.equals(getResources().getStringArray(R.array.zummer_types)[0])){
                if(!command.toString().isEmpty()){
                    command.append(";");
                }
                command.append(SshConnection.ZUMMER_TYPE_COMMAND);
                command.append(" ");
                if(zumTypeSend.equalsIgnoreCase("room")){command.append(1);}
                else if(zumTypeSend.equalsIgnoreCase("street")){command.append(2);}
            }

            if(modemConfigSend != null && !modemConfigSend.isEmpty()
                    && !modemConfigSend.equals(getResources().getStringArray(R.array.modem_types)[0])){
                if(!command.toString().isEmpty()){
                    command.append(";");
                }
                if(statTransiver.getModem().equalsIgnoreCase("megafon") && modemConfigSend.equalsIgnoreCase("beeline")){
                    command.append(SshConnection.MODEM_CONFIG_MEGAF_BEELINE_COMMAND);
                }
                else if(statTransiver.getModem().equalsIgnoreCase("beeline") && modemConfigSend.equalsIgnoreCase("megafon")){
                    command.append(SshConnection.MODEM_CONFIG_BEELINE_MEGAF_COMMAND);
                }
            }
            Logger.d(Logger.STATION_CONTEN_LOG, "send command: " + command.toString());
            SshConnection connection = new SshConnection(ip, ((StationContentFragment) fragmentHandler.getCurrentFragment()));
            connection.execute(SshConnection.SEND_STATION_CONTENT_CODE, command.toString());
        } else if(version != null){
            int zumType = 0;
            if(zumTypeSend.equals(getResources().getStringArray(R.array.zummer_types)[1])){
                zumType = 2;
            } else if(zumTypeSend.equals(getResources().getStringArray(R.array.zummer_types)[2])){
                zumType = 1;
            }
            if(!floorSend.isEmpty()) {
                Thread thread = new Thread(new PostInfo(this, ip, floor(Integer.parseInt(floorSend))));
                thread.start();
            }
        }
    }

    @Override
    public void onTaskCompleted(Bundle result) {
        String command = result.getString(BundleKeys.COMMAND_KEY);
        String ip = result.getString(BundleKeys.IP_KEY);
        String response = result.getString(BundleKeys.RESPONSE_KEY);
        if(command != null) {
            switch (command) {
                case PostCommand.REBOOT + "_" + ContentFragment.REBOOT_RASP:
                    fragmentHandler.changeFragment(FragmentHandler.CONFIG_STATION_FRAGMENT, false);
                    break;
                case PostCommand.REBOOT + "_" + ContentFragment.REBOOT_STM:
                    if(response.startsWith("Ok")) {
                        fragmentHandler.showMessage(getString(R.string.stm_rebooted));
                    } else {
                        fragmentHandler.showMessage("reboot stm error ");
                    }
                    break;
                case PostCommand.REBOOT + "_" + ContentFragment.REBOOT_ALL:
                    showMessageAndChangeFragment(response, getString(R.string.all_rebooted),
                            "reboot all error ",  FragmentHandler.CONFIG_STATION_FRAGMENT);
                    break;
                case PostCommand.ERASE_CONTENT:
                    showMessageAndChangeFragment(response, getString(R.string.rasp_was_cleared),
                            "clear rasp error ", FragmentHandler.CONFIG_STATION_FRAGMENT);
                    break;
                case FLOOR:
                    showMessageAndChangeFragment(response, "floor changed",
                            "set floor error ", FragmentHandler.CONFIG_STATION_FRAGMENT);
                    int zumType = 0;
                    if(zumTypeSend.equals(getResources().getStringArray(R.array.zummer_types)[1])){
                        zumType = 2;
                    } else if(zumTypeSend.equals(getResources().getStringArray(R.array.zummer_types)[2])){
                        zumType = 1;
                    }
                    Thread thread = new Thread(new PostInfo(this, ip, sound(zumType)));
                    thread.start();
                    break;
                case SOUND:
                    showMessageAndChangeFragment(response, "sound type changed",
                            "set sound type error ", FragmentHandler.CONFIG_STATION_FRAGMENT);
                    thread = new Thread(new PostInfo(this, ip, volume(Integer.parseInt(zumVolumeSend))));
                    thread.start();
                    break;
                case VOLUME:
                    showMessageAndChangeFragment(response, "volume changed",
                            "set volume  error ", FragmentHandler.CONFIG_STATION_FRAGMENT);
                    break;
                default:
                    super.onTaskCompleted(result);
            }
        } else {
            super.onTaskCompleted(result);
        }
    }
}