package com.kotofeya.mobileconfigurator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

public class ScannerAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    List<Transiver> objects;

    private int scannerType;

    public static final int BASIC_SCANNER_TYPE = 0;
    public static final int BLE_SCANNER_TYPE = 1;
    public static final int UPDATE_OS_TYPE = 2;
    public static final int UPDATE_STM_TYPE = 3;
    public static final int UPDATE_CONTENT_TYPE = 4;
    public static final int CONFIG_TRANSPORT = 5;
    public static final int CONFIG_STATION = 6;


    ScannerAdapter(Context context, List<Transiver> transivers, int scannerType) {
        ctx = context;
        objects = transivers;
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
        version.setText(p.getVersion());
        TextView stmFirmware = view.findViewById(R.id.scanner_lv_item1);
        stmFirmware.setText(p.getStmFirmware());
        TextView stmBootloader = view.findViewById(R.id.scanner_lv_item2);
        stmBootloader.setText(p.getStmBootloader());
        TextView exp = view.findViewById(R.id.scanner_lv_item_txt_exp);
        exp.setText(p.getBasicScanInfo());
        if(scannerType == BASIC_SCANNER_TYPE){
            version.setVisibility(View.VISIBLE);
            stmFirmware.setVisibility(View.VISIBLE);
            stmBootloader.setVisibility(View.VISIBLE);
            exp.setVisibility(View.VISIBLE);
        }

        else if(scannerType == BLE_SCANNER_TYPE){
            exp.setVisibility(View.VISIBLE);
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
            // TODO: 18.07.20  set type & direction
//            version.setText();
//            version.setVisibility(View.VISIBLE);
//            stmFirmware.setText();
//            stmFirmware.setVisibility(View.VISIBLE);
        }

        else if(scannerType == CONFIG_STATION){
            // TODO: 18.07.20  set increment
//            version.setText();
//            version.setVisibility(View.VISIBLE);
        }
        return view;
    }

    Transiver getTransiver(int position) {
        return ((Transiver) getItem(position));
    }
}
