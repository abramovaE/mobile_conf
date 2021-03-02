package com.kotofeya.mobileconfigurator.fragments.config;

import android.os.Bundle;
import android.util.Log;
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
import com.kotofeya.mobileconfigurator.transivers.TransportTransiver;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.Arrays;

public class TransportContentFragment extends ContentFragment implements View.OnClickListener {


    Spinner spnType;
    EditText number;
//    EditText liter;
    Spinner spnDir;
    EditText liter1;
    EditText liter2;
    EditText liter3;


    TransportTransiver transportTransiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        String ssid = getArguments().getString("ssid");

        transportTransiver = (TransportTransiver) utils.getBySsid(ssid);
        Logger.d(Logger.TRANSPORT_CONTENT_LOG, "getbyssid: " + transportTransiver);

        mainTxtLabel.setText(transportTransiver.getSsid() + "\n (" + transportTransiver.getTransportType() + "/" + transportTransiver.getFullNumber() + "/" + transportTransiver.getStringDirection() + ")");

        spnType = view.findViewById(R.id.content_spn_0);
        String[] transports = getResources().getStringArray(R.array.transports);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, transports);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnType.setAdapter(adapter);
        spnType.setSelection(transportTransiver.getTransportType());
        spnType.setVisibility(View.VISIBLE);
        spnType.setOnItemSelectedListener(onItemSelectedListener);

        liter1 = view.findViewById(R.id.content_txt_1);
        liter1.setText(transportTransiver.getLiteraN(1));
        liter1.setHint(R.string.content_transport_litera3_hint);
        liter1.setVisibility(View.VISIBLE);
        liter1.addTextChangedListener(textWatcher);
        liter1.setOnKeyListener(onKeyListener);

        number = view.findViewById(R.id.content_txt_2);
        number.setText(transportTransiver.getNumber() + "");
        number.setHint(R.string.content_transport_number_hint);
        number.setVisibility(View.VISIBLE);
        number.addTextChangedListener(textWatcher);
        number.setOnKeyListener(onKeyListener);

        liter2 = view.findViewById(R.id.content_txt_3);
        liter2.setText(transportTransiver.getLiteraN(2));
        liter2.setHint(R.string.content_transport_litera1_hint);
        liter2.setVisibility(View.VISIBLE);
        liter2.addTextChangedListener(textWatcher);
        liter2.setOnKeyListener(onKeyListener);

        liter3 = view.findViewById(R.id.content_txt_4);
        liter3.setText(transportTransiver.getLiteraN(3));
        liter3.setHint(R.string.content_transport_litera2_hint);
        liter3.setVisibility(View.VISIBLE);
        liter3.addTextChangedListener(textWatcher);
        liter3.setOnKeyListener(onKeyListener);

        spnDir = view.findViewById(R.id.content_spn_1);
        String[] directions = getResources().getStringArray(R.array.direction);
        ArrayAdapter<String> adapterDir = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, directions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnDir.setAdapter(adapterDir);
        spnDir.setSelection(transportTransiver.getDirection());
        spnDir.setVisibility(View.VISIBLE);
        spnType.setOnItemSelectedListener(onItemSelectedListener);
        updateBtnCotentSendState();
        btnContntSend.setOnClickListener(this);
        return view;
    }


    protected void updateBtnCotentSendState(){
        if(spnType.getSelectedItemPosition() > 0
//                && !number.getText().toString().isEmpty()
//                && !liter.getText().toString().isEmpty()
//                && spnDir.getSelectedItemPosition() > 0
                && utils.getIp(transportTransiver.getSsid()) != null){
                btnContntSend.setEnabled(true);
        }
        else {
            btnContntSend.setEnabled(false);
        }
    }

    @Override
    public void updateFields() {
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
        int type = spnType.getSelectedItemPosition();
        String typeHex = type + "";

        int num = 61166;
        try {
            num = Integer.parseInt(number.getText().toString());
        }catch (NumberFormatException e){
        }
        String numHex = num + "";

        String lit1 = liter1.getText().toString().toLowerCase();
        String lit2 = liter2.getText().toString().toLowerCase();
        String lit3 = liter3.getText().toString().toLowerCase();
        String litHex = "";
        try {
            String lit = toHex(lit1) + toHex(lit3) + toHex(lit2);
            if(!lit.isEmpty()){
                litHex = Long.parseLong(lit, 16) + "";
            }
            else {
                litHex = 0 + "";
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        int dir = spnDir.getSelectedItemPosition();
        String dirHex = dir + "";
        Logger.d(Logger.TRANSPORT_CONTENT_LOG, "send: " + typeHex + " " + numHex + " " + litHex + " " + dirHex);
        SshConnection connection = new SshConnection(((TransportContentFragment) App.get().getFragmentHandler().getCurrentFragment()));
        String ip = transportTransiver.getIp();
        if(ip == null){
            ip = utils.getIp(transportTransiver.getSsid());
        }
        connection.execute(ip, SshConnection.SEND_TRANSPORT_CONTENT_CODE, typeHex, numHex, litHex, dirHex);
    }

    public String toHex(String arg) throws UnsupportedEncodingException {
        if(arg.trim().isEmpty()){
            return "00";
        }
        return String.format("%x", new BigInteger(1, arg.getBytes("cp1251"))).toUpperCase();
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}