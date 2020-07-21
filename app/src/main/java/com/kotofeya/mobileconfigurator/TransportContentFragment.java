package com.kotofeya.mobileconfigurator;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TransportContentFragment extends ContentFragment {

    private int selectedTransport;
    Spinner spinner;
    EditText number;
    EditText liter;
    Spinner spinnerDir;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        TransportTransiver transportTransiver = (TransportTransiver) utils.getCurrentTransiver();
        mainTxtLabel.setText(utils.getCurrentTransiver().getSsid() + "\n (" + transportTransiver.getTransportType() + "/" + transportTransiver.getFullNumber() + "/" + transportTransiver.getDirection() + ")");

        spinner = view.findViewById(R.id.content_spn_0);
        String[] transports = getResources().getStringArray(R.array.transports);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, transports);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(transportTransiver.getTransportType());
        spinner.setVisibility(View.VISIBLE);
        spinner.setOnItemSelectedListener(onItemSelectedListener);

        number = view.findViewById(R.id.content_txt_1);
        number.setText(transportTransiver.getFullNumber());
        number.setHint(R.string.content_transport_number_hint);
        number.setVisibility(View.VISIBLE);
        number.addTextChangedListener(textWatcher);
        number.setOnKeyListener(onKeyListener);

        liter = view.findViewById(R.id.content_txt_2);
        liter.setText(transportTransiver.getFullNumber());
        liter.setHint(R.string.content_transport_litera_hint);
        liter.setVisibility(View.VISIBLE);
        liter.addTextChangedListener(textWatcher);
        liter.setOnKeyListener(onKeyListener);

        spinnerDir = view.findViewById(R.id.content_spn_1);
        String[] directions = getResources().getStringArray(R.array.direction);
        ArrayAdapter<String> adapterDir = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, directions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDir.setAdapter(adapterDir);
        spinnerDir.setSelection(transportTransiver.getDirection());
        spinnerDir.setVisibility(View.VISIBLE);
        spinner.setOnItemSelectedListener(onItemSelectedListener);

        updateBtnCotentSendState();
        return view;
    }


    protected void updateBtnCotentSendState(){
        if(spinner.getSelectedItemPosition()> 0 && !number.getText().toString().isEmpty() && !liter.getText().toString().isEmpty() && spinnerDir.getSelectedItemPosition() > 0){
            btnContntSend.setEnabled(true);
        }
        else {
            btnContntSend.setEnabled(false);
        }
    }


}
