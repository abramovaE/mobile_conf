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
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;
import com.kotofeya.mobileconfigurator.transivers.TransportTransiver;


import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

public class TransportContentFragment extends ContentFragment {
    Spinner spnType;
    EditText number;
    Spinner spnDir;
    EditText liter1;
    EditText liter2;
    EditText liter3;
    TransportTransiver transportTransiver;

    @Override
    protected void setFields() {
        transportTransiver = (TransportTransiver) viewModel.getTransiverBySsid(ssid);
        Logger.d(Logger.TRANSPORT_CONTENT_LOG, "getbyssid: " + transportTransiver);
        Logger.d(Logger.TRANSPORT_CONTENT_LOG, "ip: " + viewModel.getIp(transportTransiver.getSsid()));

        mainTxtLabel.setText(transportTransiver.getSsid() + "\n (" + transportTransiver.getTransportType() + "/" + transportTransiver.getFullNumber() + "/" + transportTransiver.getStringDirection() + ")");

        spnType = getView().findViewById(R.id.content_spn_0);
        String[] transports = getResources().getStringArray(R.array.transports);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, transports);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnType.setAdapter(adapter);
        spnType.setSelection(transportTransiver.getTransportType());
        spnType.setVisibility(View.VISIBLE);
        spnType.setOnItemSelectedListener(onItemSelectedListener);

        liter1 = getView().findViewById(R.id.content_txt_1);
        liter1.setText(transportTransiver.getLiteraN(1));
        liter1.setHint(R.string.content_transport_litera3_hint);
        liter1.setVisibility(View.VISIBLE);
        liter1.addTextChangedListener(textWatcher);
        liter1.setOnKeyListener(onKeyListener);

        number = getView().findViewById(R.id.content_txt_2);
        number.setText(transportTransiver.getNumber() + "");
        number.setHint(R.string.content_transport_number_hint);
        number.setVisibility(View.VISIBLE);
        number.addTextChangedListener(textWatcher);
        number.setOnKeyListener(onKeyListener);

        liter2 = getView().findViewById(R.id.content_txt_3);
        liter2.setText(transportTransiver.getLiteraN(2));
        liter2.setHint(R.string.content_transport_litera1_hint);
        liter2.setVisibility(View.VISIBLE);
        liter2.addTextChangedListener(textWatcher);
        liter2.setOnKeyListener(onKeyListener);

        liter3 = getView().findViewById(R.id.content_txt_4);
        liter3.setText(transportTransiver.getLiteraN(3));
        liter3.setHint(R.string.content_transport_litera2_hint);
        liter3.setVisibility(View.VISIBLE);
        liter3.addTextChangedListener(textWatcher);
        liter3.setOnKeyListener(onKeyListener);

        spnDir = getView().findViewById(R.id.content_spn_1);
        String[] directions = getResources().getStringArray(R.array.direction);
        ArrayAdapter<String> adapterDir = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, directions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnDir.setAdapter(adapterDir);
        spnDir.setSelection(transportTransiver.getDirection());
        spnDir.setVisibility(View.VISIBLE);
        spnType.setOnItemSelectedListener(onItemSelectedListener);
        updateBtnCotentSendState();
        btnContntSend.setOnClickListener(this);
    }

    protected void updateBtnCotentSendState(){
        String ip = transportTransiver.getIp();
        String version = viewModel.getVersion(transportTransiver.getSsid());
        if(ip == null){
            ip = viewModel.getIp(transportTransiver.getSsid());
        }
        if(spnType.getSelectedItemPosition() > 0 && ip != null && version != null){
                btnContntSend.setEnabled(true);
        } else {
            btnContntSend.setEnabled(false);
        }
    }

    @Override
    public void updateFields() {}



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
        int dir = spnDir.getSelectedItemPosition();
        String dirHex = dir + "";
        String ip = utils.getIp(transportTransiver.getSsid());
        updateTransportContent(ip, typeHex, numHex, dirHex, lit1, lit2, lit3);
    }


    private void updateTransportContent(String ip, String typeHex, String numHex, String dirHex, String lit1, String lit2, String lit3) {
        String version = viewModel.getVersion(transportTransiver.getSsid());
        Logger.d(Logger.TRANSPORT_CONTENT_LOG, "update transport content version: " + version);

        if (version != null && !version.equals("ssh_conn")) {
            Thread thread = new Thread(new PostInfo(this, ip, transpConfig(typeHex, lit1, numHex, lit2, lit3, dirHex)));
            thread.start();
        } else if (version != null && version.equals("ssh_conn")){
            String litHex = "";
            try {
                String lit = toHex(lit1) + toHex(lit3) + toHex(lit2);
                litHex = "" + ((!lit.isEmpty())? Long.parseLong(lit, 16) : 0);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Logger.d(Logger.TRANSPORT_CONTENT_LOG, "send: " + typeHex + " " + numHex + " "
                    + dirHex + " " + lit1 + " " + lit2 + " " + lit3);
            SshConnection connection = new SshConnection(((TransportContentFragment) App.get().getFragmentHandler().getCurrentFragment()));
            connection.execute(ip, SshConnection.SEND_TRANSPORT_CONTENT_CODE, typeHex, numHex, litHex, dirHex);
        }
    }

    public String toHex(String arg) throws UnsupportedEncodingException {
        if(arg.trim().isEmpty()){
            return "00";
        }
        return String.format("%x", new BigInteger(1, arg.getBytes("cp1251"))).toUpperCase();
    }

    @Override
    public void onTaskCompleted(Bundle result) {
        String command = result.getString(BundleKeys.COMMAND_KEY);
        String ip = result.getString(BundleKeys.IP_KEY);
        String response = result.getString(BundleKeys.RESPONSE_KEY);
        Logger.d(Logger.TRANSPORT_CONTENT_LOG, "on task completed, result: " + result);
        Logger.d(Logger.TRANSPORT_CONTENT_LOG, "command: " + command);
        Logger.d(Logger.TRANSPORT_CONTENT_LOG, "ip: " + ip);
        Logger.d(Logger.TRANSPORT_CONTENT_LOG, "response: " + response);

        if(command != null) {
            switch (command) {
                case PostCommand.TRANSP_CONTENT:
                case PostCommand.REBOOT + "_" + ContentFragment.REBOOT_RASP:
                    App.get().getFragmentHandler().changeFragment(FragmentHandler.CONFIG_TRANSPORT_FRAGMENT, false);
                    break;
                case PostCommand.REBOOT + "_" + ContentFragment.REBOOT_STM:
                    utils.showMessage((response.startsWith("Ok"))? getString(R.string.stm_rebooted) : "reboot stm error ");
                    break;
                case PostCommand.REBOOT + "_" + ContentFragment.REBOOT_ALL:
                    showMessageAndChangeFragment(response, getString(R.string.all_rebooted),
                            "reboot all error ", FragmentHandler.CONFIG_TRANSPORT_FRAGMENT);
                    break;
                case PostCommand.ERASE_CONTENT:
                    showMessageAndChangeFragment(response, getString(R.string.rasp_was_cleared),
                            "clear rasp error ", FragmentHandler.CONFIG_TRANSPORT_FRAGMENT);
                    break;
                default:
                    super.onTaskCompleted(result);
            }
        } else {
            super.onTaskCompleted(result);
        }
    }
}