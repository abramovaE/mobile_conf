package com.kotofeya.mobileconfigurator;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class StationContentFragment extends ContentFragment {
    private Context context;
    private Utils utils;

    EditText floorTxt;
    Spinner zummerTypesSpn;
    Spinner zummerVolumeSpn;
    Spinner modemConfigSpn;
    Button btnContntSend;

    @Override
    public void onAttach(Context context) {
        this.context = context;
        this.utils = ((MainMenu) context).getUtils();
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_fragment, container, false);
        StatTransiver statTransiver = (StatTransiver) utils.getCurrentTransiver();


        TextView mainTxtLabel = ((MainMenu)context).findViewById(R.id.main_txt_label);
        mainTxtLabel.setText(utils.getCurrentTransiver().getSsid() + " (" + statTransiver.getType() + ")");
        Button mainBtnRescan = ((MainMenu)context).findViewById(R.id.main_btn_rescan);



        floorTxt = view.findViewById(R.id.content_txt_0);
        floorTxt.setText(statTransiver.getFloor() + "");
        floorTxt.setVisibility(View.VISIBLE);
        floorTxt.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                updateBtnCotentSendState();
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        floorTxt.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) App.get().getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(v.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    return true;
                }
                return false;
            }
        });



        zummerTypesSpn = view.findViewById(R.id.content_spn_0);
        String[] zummerTypes = new String[2];
        zummerTypes[0] = "a";
        zummerTypes[1] = "b";
        ArrayAdapter<String> zummerTypesAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, zummerTypes);
        zummerTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        zummerTypesSpn.setAdapter(zummerTypesAdapter);
        zummerTypesSpn.setVisibility(View.VISIBLE);
        zummerTypesSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateBtnCotentSendState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateBtnCotentSendState();

            }
        });



        zummerVolumeSpn = view.findViewById(R.id.content_spn_1);
        zummerVolumeSpn.setVisibility(View.VISIBLE);
        String[] zummerVolume = new String[2];
        zummerVolume[0] = "c";
        zummerVolume[1] = "d";
        ArrayAdapter<String> zummerVolumeAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, zummerVolume);
        zummerVolumeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        zummerVolumeSpn.setAdapter(zummerVolumeAdapter);
        zummerVolumeSpn.setVisibility(View.VISIBLE);
        zummerVolumeSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateBtnCotentSendState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateBtnCotentSendState();

            }
        });



        modemConfigSpn = view.findViewById(R.id.content_spn_2);
        modemConfigSpn.setVisibility(View.VISIBLE);
        String[] modemConfigs = new String[2];
        modemConfigs[0] = "e";
        modemConfigs[1] = "f";
        ArrayAdapter<String> modemConfigAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, modemConfigs);
        modemConfigAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modemConfigSpn.setAdapter(modemConfigAdapter);
        modemConfigSpn.setVisibility(View.VISIBLE);
        modemConfigSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateBtnCotentSendState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateBtnCotentSendState();

            }
        });






        Button btnRebootRasp = view.findViewById(R.id.content_btn_rasp);
        Button btnRebootStm = view.findViewById(R.id.content_btn_stm);
        btnContntSend = view.findViewById(R.id.content_btn_send);


        updateBtnCotentSendState();

        return view;
    }


    private  void updateBtnCotentSendState(){
        if(!floorTxt.getText().toString().isEmpty() || zummerTypesSpn.getSelectedItemPosition()> 0
                || zummerVolumeSpn.getSelectedItemPosition() > 0 || modemConfigSpn.getSelectedItemPosition() > 0){
            btnContntSend.setEnabled(true);
        }
        else {
            btnContntSend.setEnabled(false);
        }
    }
}
