package com.kotofeya.mobileconfigurator;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class StationContentFragment extends ContentFragment {

    EditText floorTxt;
    Spinner zummerTypesSpn;
    Spinner zummerVolumeSpn;
    Spinner modemConfigSpn;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        StatTransiver statTransiver = (StatTransiver) utils.getCurrentTransiver();

        mainTxtLabel.setText(utils.getCurrentTransiver().getSsid() + " (" + statTransiver.getType() + ")");

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
        return view;
    }


    protected void updateBtnCotentSendState(){
        if(!floorTxt.getText().toString().isEmpty() || zummerTypesSpn.getSelectedItemPosition()> 0
                || zummerVolumeSpn.getSelectedItemPosition() > 0 || modemConfigSpn.getSelectedItemPosition() > 0){
            btnContntSend.setEnabled(true);
        }
        else {
            btnContntSend.setEnabled(false);
        }
    }
}
