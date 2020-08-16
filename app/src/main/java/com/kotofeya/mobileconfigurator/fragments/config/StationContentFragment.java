package com.kotofeya.mobileconfigurator.fragments.config;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.transivers.StatTransiver;
import com.kotofeya.mobileconfigurator.transivers.TransportTransiver;

public class StationContentFragment extends ContentFragment implements View.OnClickListener {

    EditText floorTxt;
    Spinner zummerTypesSpn;
    Spinner zummerVolumeSpn;
    Spinner modemConfigSpn;

    StatTransiver statTransiver;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        String ssid = getArguments().getString("ssid");
        statTransiver = (StatTransiver) utils.getBySsid(ssid);


        mainTxtLabel.setText(statTransiver.getSsid() + " (" + statTransiver.getType() + ")");

        floorTxt = view.findViewById(R.id.content_txt_0);
        floorTxt.setText(statTransiver.getFloor() + "");
        floorTxt.setVisibility(View.VISIBLE);
        floorTxt.addTextChangedListener(textWatcher);
        floorTxt.setOnKeyListener(onKeyListener);

        zummerTypesSpn = view.findViewById(R.id.content_spn_0);
        String[] zummerTypes = getResources().getStringArray(R.array.zummer_types);
        ArrayAdapter<String> zummerTypesAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, zummerTypes);
        zummerTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        zummerTypesSpn.setAdapter(zummerTypesAdapter);
        zummerTypesSpn.setVisibility(View.VISIBLE);
        zummerTypesSpn.setOnItemSelectedListener(onItemSelectedListener);

        zummerVolumeSpn = view.findViewById(R.id.content_spn_1);
        zummerVolumeSpn.setVisibility(View.VISIBLE);
        String[] zummerVolume = new String[11];
        zummerVolume[0] = "";
        for(int i = 1; i < 11; i++){
            zummerVolume[i] = i + "";
;        }
        ArrayAdapter<String> zummerVolumeAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, zummerVolume);
        zummerVolumeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        zummerVolumeSpn.setAdapter(zummerVolumeAdapter);
        zummerVolumeSpn.setVisibility(View.VISIBLE);
        zummerVolumeSpn.setOnItemSelectedListener(onItemSelectedListener);

        modemConfigSpn = view.findViewById(R.id.content_spn_2);
        modemConfigSpn.setVisibility(View.VISIBLE);
        String[] modemConfigs = getResources().getStringArray(R.array.modem_types);
        ArrayAdapter<String> modemConfigAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, modemConfigs);
        modemConfigAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modemConfigSpn.setAdapter(modemConfigAdapter);
        modemConfigSpn.setVisibility(View.VISIBLE);
        modemConfigSpn.setOnItemSelectedListener(onItemSelectedListener);

        updateBtnCotentSendState();
        btnContntSend.setOnClickListener(this);
        return view;
    }


    protected void updateBtnCotentSendState(){
        if((!floorTxt.getText().toString().isEmpty() || zummerTypesSpn.getSelectedItemPosition()> 0
                || zummerVolumeSpn.getSelectedItemPosition() > 0 || modemConfigSpn.getSelectedItemPosition() > 0)
                && statTransiver.getIp() != null){
            btnContntSend.setEnabled(true);
        }
        else {
            btnContntSend.setEnabled(false);
        }
    }

    @Override
    public void stopScan() {
        utils.getBluetooth().stopScan(true);
    }



    @Override
    public void onProgressUpdate(Integer downloaded) {

    }

    @Override
    public void onClick(View v) {

        String floorSend = floorTxt.getText().toString();
        String zummerTypeSend = zummerTypesSpn.getSelectedItem().toString();
        String zummerVolumeSend = zummerVolumeSpn.getSelectedItem().toString();
        String modemConfigSend = modemConfigSpn.getTransitionName();

        StringBuilder command = new StringBuilder();
        if(floorSend != null && !floorSend.isEmpty()){
            command.append(SshConnection.FLOOR_COMMAND);
            command.append(" ");
            command.append(floorSend);
        }
        if(zummerTypeSend != null && !zummerTypeSend.isEmpty()){
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
        if(modemConfigSend != null && !modemConfigSend.isEmpty()){
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
        SshConnection connection = new SshConnection(((StationContentFragment) App.get().getFragmentHandler().getCurrentFragment()));
        connection.execute(statTransiver.getIp(), SshConnection.SEND_STATION_CONTENT_COMMAND, command.toString());
    }
}
