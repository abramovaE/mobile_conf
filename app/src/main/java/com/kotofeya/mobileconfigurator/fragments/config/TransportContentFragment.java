package com.kotofeya.mobileconfigurator.fragments.config;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

public class TransportContentFragment extends ContentFragment {
    private static final String TAG = TransportContentFragment.class.getSimpleName();

    Spinner spnType;
    EditText number;
    Spinner spnDir;
    EditText liter1;
    EditText liter2;
    EditText liter3;
    Transceiver transportTransiver;

    @Override
    protected void setFields() {
        transportTransiver = viewModel.getTransceiverBySsid(ssid);
        Logger.d(TAG, "getBySsid: " + transportTransiver);

        viewModel.setMainTxtLabel(transportTransiver.getSsid()
//                 + "\n (" + transportTransiver.getTransportType() +
//                "/" + transportTransiver.getFullNumber() +
//                "/" + transportTransiver.getStringDirection() + ")"
        );


        spnType = binding.contentSpn0;
        String[] transports = getResources().getStringArray(R.array.transports);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item, transports);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnType.setAdapter(adapter);
//        spnType.setSelection(transportTransiver.getTransportType());
        spnType.setVisibility(View.VISIBLE);
        spnType.setOnItemSelectedListener(onItemSelectedListener);

        liter1 = binding.contentTxt1;
//        liter1.setText(transportTransiver.getLiteraN(1));
        liter1.setHint(R.string.content_transport_litera3_hint);
        liter1.setVisibility(View.VISIBLE);
        liter1.addTextChangedListener(textWatcher);
        liter1.setOnKeyListener(onKeyListener);

        number = binding.contentTxt2;
//        number.setText(String.valueOf(transportTransiver.getNumber()));
        number.setHint(R.string.content_transport_number_hint);
        number.setVisibility(View.VISIBLE);
        number.addTextChangedListener(textWatcher);
        number.setOnKeyListener(onKeyListener);

        liter2 = binding.contentTxt3;
//        liter2.setText(transportTransiver.getLiteraN(2));
        liter2.setHint(R.string.content_transport_litera1_hint);
        liter2.setVisibility(View.VISIBLE);
        liter2.addTextChangedListener(textWatcher);
        liter2.setOnKeyListener(onKeyListener);

        liter3 = binding.contentTxt4;
//        liter3.setText(transportTransiver.getLiteraN(3));
        liter3.setHint(R.string.content_transport_litera2_hint);
        liter3.setVisibility(View.VISIBLE);
        liter3.addTextChangedListener(textWatcher);
        liter3.setOnKeyListener(onKeyListener);

        spnDir = binding.contentSpn1;
        String[] directions = getResources().getStringArray(R.array.direction);
        ArrayAdapter<String> adapterDir = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item, directions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnDir.setAdapter(adapterDir);
//        spnDir.setSelection(transportTransiver.getDirection());
        spnDir.setVisibility(View.VISIBLE);
        spnType.setOnItemSelectedListener(onItemSelectedListener);
        updateBtnContentSendState();
        binding.contentBtnSend.setOnClickListener(this);
    }

    protected void updateBtnContentSendState(){
        Transceiver tr = viewModel.getTransceiverBySsid(transportTransiver.getSsid());
        String ip = tr.getIp();
        String version = tr.getVersion();
        binding.contentBtnSend
                .setEnabled(spnType.getSelectedItemPosition() > 0 && ip != null && version != null);
    }
    @Override
    public void updateFields() {}

    @Override
    public void onClick(View v) {
        Logger.d(TAG, "onClick()");
        int type = spnType.getSelectedItemPosition();
        String typeHex = type + "";
        int num = 61166;
        try {
            num = Integer.parseInt(number.getText().toString());
        } catch (NumberFormatException e){ }
        String numHex = num + "";
        String lit1 = liter1.getText().toString().toLowerCase();
        String lit2 = liter2.getText().toString().toLowerCase();
        String lit3 = liter3.getText().toString().toLowerCase();
        int dir = spnDir.getSelectedItemPosition();
        String dirHex = dir + "";

        String ip = viewModel.getTransceiverBySsid(transportTransiver.getSsid()).getIp();
        updateTransportContent(ip, typeHex, numHex, dirHex, lit1, lit2, lit3);
    }


    private void updateTransportContent(String ip, String typeHex, String numHex, String dirHex, String lit1, String lit2, String lit3) {
        String version = transportTransiver.getVersion();
        Logger.d(TAG, "updateTransportContent(), version: " + version);

        if(version != null){
            if (!version.equals("ssh_conn")) {
                Thread thread = new Thread(new PostInfo(this, ip, transpConfig(typeHex, lit1, numHex, lit2, lit3, dirHex)));
                thread.start();
            } else {
                String litHex = "";
                try {
                    String lit = toHex(lit1) + toHex(lit3) + toHex(lit2);
                    litHex = "" + ((!lit.isEmpty())? Long.parseLong(lit, 16) : 0);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Logger.d(TAG, "send: " + typeHex + " " + numHex + " "
                        + dirHex + " " + lit1 + " " + lit2 + " " + lit3);
                SshConnection connection = new SshConnection(ip, ((TransportContentFragment) fragmentHandler.getCurrentFragment()));
                connection.execute(SshConnection.SEND_TRANSPORT_CONTENT_CODE, typeHex, numHex, litHex, dirHex);
            }
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
        if(command != null) {
            switch (command) {
                case PostCommand.TRANSP_CONTENT:
                case PostCommand.REBOOT + "_" + ContentFragment.REBOOT_RASP:
                    fragmentHandler.changeFragment(FragmentHandler.CONFIG_TRANSPORT_FRAGMENT, false);
                    break;
                case PostCommand.REBOOT + "_" + ContentFragment.REBOOT_STM:
                    fragmentHandler.showMessage((response.startsWith("Ok"))? getString(R.string.stm_rebooted) : "reboot stm error ");
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