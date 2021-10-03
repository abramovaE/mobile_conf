package com.kotofeya.mobileconfigurator.fragments.config;


import android.os.Bundle;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.FragmentHandler;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.SshConnectionRunnable;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;
import com.kotofeya.mobileconfigurator.transivers.StatTransiver;


public class StationContentFragment extends ContentFragment {
    EditText floorTxt;
    Spinner zummerTypesSpn;
    Spinner zummerVolumeSpn;
    Spinner modemConfigSpn;
    String[] modemConfigs;
    StatTransiver statTransiver;

    String zummerTypeSend;
    String zummerVolumeSend;

    @Override
    protected void setFields() {
        statTransiver = (StatTransiver) viewModel.getTransiverBySsid(ssid);
        mainTxtLabel.setText(statTransiver.getSsid() + " (" + statTransiver.getStringType() + ")");
        floorTxt = getView().findViewById(R.id.content_txt_0);
        floorTxt.setText(statTransiver.getFloor() + "");
        floorTxt.setVisibility(View.VISIBLE);
        floorTxt.addTextChangedListener(textWatcher);
        floorTxt.setOnKeyListener(onKeyListener);
        floorTxt.setHint(getString(R.string.floor_hint));
        zummerTypesSpn = getView().findViewById(R.id.content_spn_0);
        String[] zummerTypes = getResources().getStringArray(R.array.zummer_types);
        ArrayAdapter<String> zummerTypesAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, zummerTypes);
        zummerTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        zummerTypesSpn.setAdapter(zummerTypesAdapter);
        zummerTypesSpn.setVisibility(View.VISIBLE);
        zummerTypesSpn.setOnItemSelectedListener(onItemSelectedListener);
        zummerVolumeSpn = getView().findViewById(R.id.content_spn_1);
        zummerVolumeSpn.setVisibility(View.VISIBLE);
        String[] zummerVolume = new String[11];
        zummerVolume[0] = getResources().getString(R.string.zummer_volume_hint);
        for(int i = 1; i < 11; i ++){
            zummerVolume[i] = i * 10 + "";
        }
        ArrayAdapter<String> zummerVolumeAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, zummerVolume);
        zummerVolumeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        zummerVolumeSpn.setAdapter(zummerVolumeAdapter);
        zummerVolumeSpn.setVisibility(View.VISIBLE);
        zummerVolumeSpn.setOnItemSelectedListener(onItemSelectedListener);
        modemConfigSpn = getView().findViewById(R.id.content_spn_2);
        modemConfigSpn.setVisibility(View.VISIBLE);
        modemConfigs = getResources().getStringArray(R.array.modem_types);
        ArrayAdapter<String> modemConfigAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, modemConfigs);
        modemConfigAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modemConfigSpn.setAdapter(modemConfigAdapter);
        modemConfigSpn.setVisibility(View.VISIBLE);
        modemConfigSpn.setOnItemSelectedListener(onItemSelectedListener);
        setModem();
        updateBtnCotentSendState();
        btnContntSend.setOnClickListener(this);
    }

    private void setModem(){
        if(statTransiver.getModem() != null){
            for(int i = 0; i < modemConfigs.length; i++){
                if(modemConfigs[i].equalsIgnoreCase(statTransiver.getModem())){
                    modemConfigSpn.setSelection(i);
                }
            }
        } else {
            String ip = utils.getIp(statTransiver.getSsid());
            String version = utils.getVersion(statTransiver.getSsid());
            try {
                if(version != null && version.equals("ssh_conn")) {
                    new SshConnectionRunnable(((MainActivity) getActivity()), ip, SshConnection.TAKE_CODE);
                }
            }
            catch (ClassCastException e){}
        }
    }
    protected void updateBtnCotentSendState(){
        String ip = utils.getIp(statTransiver.getSsid());
        String version = utils.getVersion(statTransiver.getSsid());
        Logger.d(Logger.STATION_CONTEN_LOG, "update btn content send state: ip - " + ip + ", version: " + version);
        if((!floorTxt.getText().toString().isEmpty() || zummerTypesSpn.getSelectedItemPosition() > 0
                || zummerVolumeSpn.getSelectedItemPosition() > 0 || modemConfigSpn.getSelectedItemPosition() > 0)
                && ip != null && version != null){
            btnContntSend.setEnabled(true);
        }
        else {
            btnContntSend.setEnabled(false);
        }
    }

    @Override
    public void updateFields() {
        setModem();
    }

    @Override
    public void onClick(View v) {
        String version = utils.getVersion(currentTransiver.getSsid());
        String floorSend = floorTxt.getText().toString();
        zummerTypeSend = zummerTypesSpn.getSelectedItem().toString();
        zummerVolumeSend = zummerVolumeSpn.getSelectedItem().toString();
        String modemConfigSend = modemConfigSpn.getTransitionName();
        if(version != null && version.equals("ssh_conn")){
            StringBuilder command = new StringBuilder();
            if(floorSend != null && !floorSend.isEmpty()){
                command.append(SshConnection.FLOOR_COMMAND);
                command.append(" ");
                command.append(floorSend);
            }
            if(zummerTypeSend != null && !zummerTypeSend.isEmpty()
                    && !zummerTypeSend.equals(getResources().getStringArray(R.array.zummer_types)[0])){
                if(!command.toString().isEmpty()){
                    command.append(";");
                }
                command.append(SshConnection.ZUMMER_TYPE_COMMAND);
                command.append(" ");
                if(zummerTypeSend.equalsIgnoreCase("room")){command.append(1);}
                else if(zummerTypeSend.equalsIgnoreCase("street")){command.append(2);}
            }

            // TODO: 16.08.2020  need
//        if(zummerVolumeSend != null && !zummerVolumeSend.isEmpty()){
//        if(!command.toString().isEmpty()){
//            command.append(";");
//        }
//            command.append(SshConnection.ZUMMER_VOLUME_COMMAND);
//            command.append(" ");
//            command.append(zummerVolumeSend);
//        }
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
            String ip = statTransiver.getIp();
            if(ip == null){
                ip = utils.getIp(statTransiver.getSsid());
            }
            Logger.d(Logger.STATION_CONTEN_LOG, "send command: " + command.toString());

            SshConnection connection = new SshConnection(((StationContentFragment) App.get().getFragmentHandler().getCurrentFragment()));
            connection.execute(ip, SshConnection.SEND_STATION_CONTENT_CODE, command.toString());
        } else if(version != null && !version.equals("ssh_conn")){
            int zummerType = 0;
            if(zummerTypeSend.equals(getResources().getStringArray(R.array.zummer_types)[1])){
                zummerType = 2;
            } else if(zummerTypeSend.equals(getResources().getStringArray(R.array.zummer_types)[2])){
                zummerType = 1;
            }
            String ip = utils.getIp(statTransiver.getSsid());

            Logger.d(Logger.STATION_CONTEN_LOG, "floor: " + floorSend);
            Logger.d(Logger.STATION_CONTEN_LOG, "zummerType: " + zummerType);
            Logger.d(Logger.STATION_CONTEN_LOG, "zummerVol: " + zummerVolumeSend);
            Logger.d(Logger.STATION_CONTEN_LOG, "modem: " + modemConfigSend);

            Thread thread = new Thread(new PostInfo(this, ip, floor(Integer.parseInt(floorSend))));
            thread.start();
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
                    App.get().getFragmentHandler().changeFragment(FragmentHandler.CONFIG_STATION_FRAGMENT, false);
                    break;
                case PostCommand.REBOOT + "_" + ContentFragment.REBOOT_STM:
                    if(response.startsWith("Ok")) {
                        utils.showMessage(getString(R.string.stm_rebooted));
                    } else {
                        utils.showMessage("reboot stm error ");
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
                    Thread thread = new Thread(new PostInfo(this, ip, sound(Integer.parseInt(zummerTypeSend))));
                    thread.start();
                    break;
                case SOUND:
                    showMessageAndChangeFragment(response, "sound type changed",
                            "set sound type error ", FragmentHandler.CONFIG_STATION_FRAGMENT);
                    thread = new Thread(new PostInfo(this, ip, volume(Integer.parseInt(zummerVolumeSend))));
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
