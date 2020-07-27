package com.kotofeya.mobileconfigurator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

public class ScannerAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    List<Transiver> objects;
    Utils utils;

    private int scannerType;

    public static final int BASIC_SCANNER_TYPE = 0;
    public static final int BLE_SCANNER_TYPE = 1;
    public static final int UPDATE_OS_TYPE = 2;
    public static final int UPDATE_STM_TYPE = 3;
    public static final int UPDATE_CONTENT_TYPE = 4;
    public static final int CONFIG_TRANSPORT = 5;
    public static final int CONFIG_STATION = 6;


    ScannerAdapter(Context context, Utils utils, int scannerType) {
        ctx = context;
        this.utils = utils;
        objects = utils.getTransivers();
        lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.scannerType = scannerType;

    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.scanner_lv_item, parent, false);
        }

        Transiver p = getTransiver(position);
        TextView ssid = view.findViewById(R.id.scanner_lv_item_ssid);
        ssid.setText(p.getSsid());

        TextView version = view.findViewById(R.id.scanner_lv_item0);
        version.setText(p.getOsVersion());

        TextView stmFirmware = view.findViewById(R.id.scanner_lv_item1);
        stmFirmware.setText(p.getStmFirmware());

        TextView stmBootloader = view.findViewById(R.id.scanner_lv_item2);
        stmBootloader.setText(p.getStmBootloader());



        final TextView exp = view.findViewById(R.id.scanner_lv_item_txt_exp);

        Button expButton = view.findViewById(R.id.scanner_lv_exp_btn);
        expButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(exp.getVisibility() == View.GONE) {
                    exp.setVisibility(View.VISIBLE);
                }
                else {
                    exp.setVisibility(View.GONE);
                }
            }
        });


        if(scannerType == BASIC_SCANNER_TYPE){
            version.setVisibility(View.VISIBLE);
            stmFirmware.setVisibility(View.VISIBLE);
            stmBootloader.setVisibility(View.VISIBLE);
            expButton.setVisibility(View.VISIBLE);
            exp.setText(p.getExpBasicScanInfo());
        }

        else if(scannerType == BLE_SCANNER_TYPE){

            Logger.d(Logger.UTILS_LOG, "scanner " + p.getSsid() + " " + p.isTransport());
            expButton.setVisibility(View.VISIBLE);
            StringBuilder text = new StringBuilder();
            text.append("inf state: /ready/busy/called");
            text.append("\n");
            for(int i = 0; i < 4; i++){
                text.append("inf" + i + ": " +  p.isCallReady(i) + "/" + p.isCallBusy(i) + "/" + p.isCalled(i));
                text.append("\n");
            }

            if(p.isTransport()){
                text.append("doors status: " + getStringDoorStatus(p.getFloorOrDoorStatus()));
                text.append("\n");
                text.append("direction: " + ((TransportTransiver) p).getDirection());
                text.append("\n");
                text.append("city: " +  ((TransportTransiver) p).getCity());
                text.append("\n");
            }

            text.append("crc: " +  p.getCrc());
            text.append("\n");
            text.append("incr: " + p.getIncrement());
            text.append("\n");


            if(p.isTransport()) {
                    text.append(((TransportTransiver)p).getTransportType());
                    text.append(" ");
                    text.append(((TransportTransiver)p).getFullNumber());

            }


            if(p.isStationary()){
                text.append(((StatTransiver)p).getType());
                text.append(" ");
                text.append(((StatTransiver)p).getGroupId());
            }

            exp.setText(text.toString());
        }

        else if(scannerType == UPDATE_OS_TYPE){


            version.setVisibility(View.VISIBLE);
        }

        else if(scannerType == UPDATE_STM_TYPE){
            stmFirmware.setVisibility(View.VISIBLE);
        }

        else if(scannerType == UPDATE_CONTENT_TYPE){
            // TODO: 18.07.20  set increment
//            version.setText();
//            version.setVisibility(View.VISIBLE);
        }

        else if(scannerType == CONFIG_TRANSPORT){
            TransportTransiver transportTransiver = (TransportTransiver) p;
            version.setText(transportTransiver.getTransportType() + " / " + transportTransiver.getFullNumber());
            version.setVisibility(View.VISIBLE);
            stmFirmware.setText(transportTransiver.getDirection() + "");
            stmFirmware.setVisibility(View.VISIBLE);
            view.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     utils.setCurrentTransiver(p);
                     App.get().getFragmentHandler().changeFragment(FragmentHandler.TRANSPORT_CONTENT_FRAGMENT);

                 }
             });
        }

        else if(scannerType == CONFIG_STATION){
            StatTransiver statTransiver = (StatTransiver) p;

//            version.setText();
//            version.setVisibility(View.VISIBLE);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    utils.setCurrentTransiver(p);
                    App.get().getFragmentHandler().changeFragment(FragmentHandler.STATION_CONTENT_FRAGMENT);

                }
            });


        }
        return view;
    }

    Transiver getTransiver(int position) {
        return ((Transiver) getItem(position));
    }


    public String getStringDoorStatus(int doorStatus) {
        switch (doorStatus) {
            case 0:
                return  ctx.getString(R.string.doorsClosed);

            case 1:
                return  ctx.getString(R.string.doorsOpened);

            case 2:
                return ctx.getString(R.string.doorsBroken);

            default:
                return null;
        }
    }


}
