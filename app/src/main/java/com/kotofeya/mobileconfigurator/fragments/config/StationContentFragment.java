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
import com.kotofeya.mobileconfigurator.SshConnectionRunnable;
import com.kotofeya.mobileconfigurator.network.NetworkService;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostResponse;
import com.kotofeya.mobileconfigurator.network.post_response.TakeInfoFull;
import com.kotofeya.mobileconfigurator.transivers.StatTransiver;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StationContentFragment extends ContentFragment implements View.OnClickListener {

    EditText floorTxt;
    Spinner zummerTypesSpn;
    Spinner zummerVolumeSpn;
    Spinner modemConfigSpn;

    StatTransiver statTransiver;
    String[] modemConfigs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        String ssid = getArguments().getString("ssid");
        statTransiver = (StatTransiver) utils.getBySsid(ssid);

        mainTxtLabel.setText(statTransiver.getSsid() + " (" + statTransiver.getStringType() + ")");

        floorTxt = view.findViewById(R.id.content_txt_0);
        floorTxt.setText(statTransiver.getFloor() + "");
        floorTxt.setVisibility(View.VISIBLE);
        floorTxt.addTextChangedListener(textWatcher);
        floorTxt.setOnKeyListener(onKeyListener);
        floorTxt.setHint(getString(R.string.floor_hint));


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
        zummerVolume[0] = getResources().getString(R.string.zummer_volume_hint);
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
        modemConfigs = getResources().getStringArray(R.array.modem_types);
        ArrayAdapter<String> modemConfigAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, modemConfigs);
        modemConfigAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modemConfigSpn.setAdapter(modemConfigAdapter);
        modemConfigSpn.setVisibility(View.VISIBLE);
        modemConfigSpn.setOnItemSelectedListener(onItemSelectedListener);

        setModem();
        updateBtnCotentSendState();
        btnContntSend.setOnClickListener(this);
        return view;
    }

    private void setModem(){
        if(statTransiver.getModem() != null){
            for(int i = 0; i < modemConfigs.length; i++){
                if(modemConfigs[i].equalsIgnoreCase(statTransiver.getModem())){
                    modemConfigSpn.setSelection(i);
                }
            }
        } else {
            String ip = statTransiver.getIp();
            if (ip == null) {
                ip = utils.getIp(statTransiver.getSsid());
            }
            try {

                new SshConnectionRunnable(((StationContentFragment) App.get().getFragmentHandler().getCurrentFragment()),
                        ip, SshConnection.TAKE_CODE);

            }
            catch (ClassCastException e){}


            final String finalIp = ip;

            String command = "info";
            NetworkService.getInstance()
                    .getJsonApi().postCommand("14.04.21_16.32", "dirvion", "fasterAnDfaster",
                    PostCommand.TAKE_INFO_FULL)
                    .enqueue(new Callback<PostResponse>() {
                        @Override
                        public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                            PostResponse postResponse = response.body();
                            Logger.d(Logger.UTILS_LOG, "on response: " + postResponse);
                            switch (command){
                                case PostCommand.TAKE_INFO_FULL:
                                    TakeInfoFull takeInfoFull = postResponse.getTakeInfoFull();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("ip", finalIp);
                                    bundle.putInt("resultCode", PostCommand.getResponseCode(PostCommand.TAKE_INFO_FULL));
                                    bundle.putString("result", takeInfoFull.toString());
                                    ((StationContentFragment) App.get().getFragmentHandler().getCurrentFragment()).onTaskCompleted(bundle);
                            }
                        }
                        @Override
                        public void onFailure(Call<PostResponse> call, Throwable t) {
                            Logger.d(Logger.UTILS_LOG, "Error occurred while getting request!");
                            t.printStackTrace();
                            Bundle bundle = new Bundle();
                            bundle.putString("ip", finalIp);
                            bundle.putInt("resultCode", PostCommand.getResponseCode(PostCommand.TAKE_INFO_FULL_ERROR));
                            bundle.putString("result", t.getMessage());
                            ((StationContentFragment) App.get().getFragmentHandler().getCurrentFragment()).onTaskCompleted(bundle);
                        }
                    });





        }
    }
    protected void updateBtnCotentSendState(){
        if((!floorTxt.getText().toString().isEmpty() || zummerTypesSpn.getSelectedItemPosition()> 0
                || zummerVolumeSpn.getSelectedItemPosition() > 0 || modemConfigSpn.getSelectedItemPosition() > 0)
                && utils.getIp(statTransiver.getSsid()) != null){
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
    }


    @Override
    public void onStart() {
        super.onStart();
    }
}
