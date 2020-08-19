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
import com.kotofeya.mobileconfigurator.transivers.TransportTransiver;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

public class TransportContentFragment extends ContentFragment implements View.OnClickListener {




    Spinner spnType;
    EditText number;
    EditText liter;
    Spinner spnDir;

    TransportTransiver transportTransiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        String ssid = getArguments().getString("ssid");
        transportTransiver = (TransportTransiver) utils.getBySsid(ssid);



//        TransportTransiver transportTransiver = (TransportTransiver) utils.getCurrentTransiver();
        mainTxtLabel.setText(transportTransiver.getSsid() + "\n (" + transportTransiver.getTransportType() + "/" + transportTransiver.getFullNumber() + "/" + transportTransiver.getDirection() + ")");

        spnType = view.findViewById(R.id.content_spn_0);
        String[] transports = getResources().getStringArray(R.array.transports);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, transports);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnType.setAdapter(adapter);
        spnType.setSelection(transportTransiver.getTransportType());
        spnType.setVisibility(View.VISIBLE);
        spnType.setOnItemSelectedListener(onItemSelectedListener);

        number = view.findViewById(R.id.content_txt_1);
        number.setText(transportTransiver.getNumber() + "");
        number.setHint(R.string.content_transport_number_hint);
        number.setVisibility(View.VISIBLE);
        number.addTextChangedListener(textWatcher);
        number.setOnKeyListener(onKeyListener);

        liter = view.findViewById(R.id.content_txt_2);
        liter.setText(transportTransiver.getPreLitera() + transportTransiver.getPostLitera());
        liter.setHint(R.string.content_transport_litera_hint);
        liter.setVisibility(View.VISIBLE);
        liter.addTextChangedListener(textWatcher);
        liter.setOnKeyListener(onKeyListener);

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
        if(spnType.getSelectedItemPosition()> 0 && !number.getText().toString().isEmpty()
                && !liter.getText().toString().isEmpty() && spnDir.getSelectedItemPosition() > 0
                && utils.getIp(transportTransiver.getSsid()) != null){
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
        int type = spnType.getSelectedItemPosition();
        String typeHex = Integer.toHexString(type);

        int num = Integer.parseInt(number.getText().toString());
        String numHex = Integer.toHexString(num);

        String lit = liter.getText().toString().toLowerCase();
        String litHex = "";

        int dir = spnDir.getSelectedItemPosition();
        String dirHex = Integer.toHexString(dir);


        try {
                // TODO: 16.08.2020 третья и четвертая литеры?
            if(!lit.isEmpty()){
                if(lit.toCharArray().length == 1){
                    litHex = toHex(lit);
                    Logger.d(Logger.TRANSPORT_CONTENT_LOG, "litera1: " + litHex);
                } else if(lit.toCharArray().length == 2){
                    litHex = toHex(lit.substring(0, 1)) + toHex(lit.substring(1, 2));
                    Logger.d(Logger.TRANSPORT_CONTENT_LOG, "litera2: " + litHex);
                }
            }
            else {
                litHex = toHex(0 + "");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Logger.d(Logger.TRANSPORT_CONTENT_LOG, "send: " + typeHex + " " + numHex + " " + litHex + " " + dirHex);
        SshConnection connection = new SshConnection(((TransportContentFragment) App.get().getFragmentHandler().getCurrentFragment()));
        connection.execute(transportTransiver.getIp(), SshConnection.SEND_TRANSPORT_CONTENT_CODE, typeHex, numHex, litHex, dirHex);
    }

    public String toHex(String arg) throws UnsupportedEncodingException {
        return String.format("%x", new BigInteger(1, arg.getBytes("cp1251")));
    }


}

