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
import androidx.fragment.app.Fragment;

public class TransportContentFragment extends ContentFragment {

    private Context context;
    private Utils utils;
    private int selectedTransport;
    Button btnContntSend;

    Spinner spinner;
    EditText number;
    EditText liter;
    Spinner spinnerDir;


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
        TransportTransiver transportTransiver = (TransportTransiver) utils.getCurrentTransiver();

        TextView mainTxtLabel = ((MainMenu)context).findViewById(R.id.main_txt_label);
        mainTxtLabel.setText(utils.getCurrentTransiver().getSsid() + "\n (" + transportTransiver.getTransportType() + "/" + transportTransiver.getFullNumber() + "/" + transportTransiver.getDirection() + ")");
        Button mainBtnRescan = ((MainMenu)context).findViewById(R.id.main_btn_rescan);

        spinner = view.findViewById(R.id.content_spn_0);
        String[] transports = getResources().getStringArray(R.array.transports);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, transports);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(transportTransiver.getTransportType());
        spinner.setVisibility(View.VISIBLE);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateBtnCotentSendState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateBtnCotentSendState();

            }
        });

        number = view.findViewById(R.id.content_txt_1);
        number.setText(transportTransiver.getFullNumber());
        number.setHint(R.string.content_transport_number_hint);
        number.setVisibility(View.VISIBLE);
        number.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                updateBtnCotentSendState();
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        number.setOnKeyListener(new View.OnKeyListener() {
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

        liter = view.findViewById(R.id.content_txt_2);
        liter.setText(transportTransiver.getFullNumber());
        liter.setHint(R.string.content_transport_litera_hint);
        liter.setVisibility(View.VISIBLE);
        liter.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                updateBtnCotentSendState();
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        liter.setOnKeyListener(new View.OnKeyListener() {
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


        spinnerDir = view.findViewById(R.id.content_spn_1);
        String[] directions = getResources().getStringArray(R.array.direction);
        ArrayAdapter<String> adapterDir = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, directions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDir.setAdapter(adapterDir);
        spinnerDir.setSelection(transportTransiver.getDirection());
        spinnerDir.setVisibility(View.VISIBLE);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        if(spinner.getSelectedItemPosition()> 0 && !number.getText().toString().isEmpty() && !liter.getText().toString().isEmpty() && spinnerDir.getSelectedItemPosition() > 0){
            btnContntSend.setEnabled(true);
        }
        else {
            btnContntSend.setEnabled(false);
        }
    }


}
