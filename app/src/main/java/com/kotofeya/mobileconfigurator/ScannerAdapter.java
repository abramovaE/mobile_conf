package com.kotofeya.mobileconfigurator;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.kotofeya.mobileconfigurator.fragments.update.UpdateContentFragment;
import com.kotofeya.mobileconfigurator.fragments.update.UpdateOsFragment;
import com.kotofeya.mobileconfigurator.fragments.update.UpdateStmFragment;
import com.kotofeya.mobileconfigurator.transivers.StatTransiver;
import com.kotofeya.mobileconfigurator.transivers.Transiver;
import com.kotofeya.mobileconfigurator.transivers.TransportTransiver;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScannerAdapter extends BaseAdapter implements OnTaskCompleted {
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


    public ScannerAdapter(Context context, Utils utils, int scannerType) {
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

        TextView textItem0 = view.findViewById(R.id.scanner_lv_item0);
        textItem0.setText(p.getOsVersion());

        TextView textItem1 = view.findViewById(R.id.scanner_lv_item1);
        textItem1.setText(p.getStmFirmware());

        TextView textItem2 = view.findViewById(R.id.scanner_lv_item2);
        textItem2.setText(p.getStmBootloader());

        final TextView exp = view.findViewById(R.id.scanner_lv_item_txt_exp);

        LinearLayout linearLayout = view.findViewById(R.id.scanner_lv);

        Button expButton = view.findViewById(R.id.scanner_lv_exp_btn);
        expButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (exp.getVisibility() == View.GONE) {
                    exp.setVisibility(View.VISIBLE);
                } else {
                    exp.setVisibility(View.GONE);
                }
            }
        });

        if (scannerType == BASIC_SCANNER_TYPE) {
            textItem0.setVisibility(View.VISIBLE);
            textItem1.setVisibility(View.VISIBLE);
            textItem2.setVisibility(View.VISIBLE);
            expButton.setVisibility(View.VISIBLE);
            exp.setText(p.getExpBasicScanInfo());
        } else if (scannerType == BLE_SCANNER_TYPE) {
            Logger.d(Logger.SCANNER_ADAPTER_LOG, "scanner " + p.getSsid() + " " + p.isTransport());
            expButton.setVisibility(View.VISIBLE);
            exp.setText(p.getBleExpText());
        } else if (scannerType == UPDATE_OS_TYPE) {
            textItem0.setVisibility(View.VISIBLE);
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Logger.d(Logger.SCANNER_ADAPTER_LOG, "linear layout was pressed");
                    Transiver transiver = getTransiver(position);
                    if (Downloader.tempUpdateOsFile.length() > 1000) {
                        Bundle bundle = new Bundle();
                        bundle.putString("ip", transiver.getIp());
                        UpdateOsFragment.UpdateOsConfDialog dialog = new UpdateOsFragment.UpdateOsConfDialog();
                        dialog.setArguments(bundle);
                        dialog.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().CONFIRMATION_DIALOG_TAG);
                    }
                }
            });
        } else if (scannerType == UPDATE_STM_TYPE) {
            textItem1.setVisibility(View.VISIBLE);
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Logger.d(Logger.SCANNER_ADAPTER_LOG, "linear layout was pressed");
                    Transiver transiver = getTransiver(position);
                    if (Downloader.tempUpdateStmFiles != null && !Downloader.tempUpdateStmFiles.isEmpty()) {
                        Logger.d(Logger.SCANNER_ADAPTER_LOG, "updateStmFilesSize: " + Downloader.tempUpdateStmFiles.size());
                        Bundle bundle = new Bundle();
                        bundle.putString("ip", transiver.getIp());
                        Logger.d(Logger.UPDATE_STM_LOG, "isTransport: " + transiver.isTransport());
                        Logger.d(Logger.UPDATE_STM_LOG, "isStationary: " + transiver.isStationary());

                        bundle.putBoolean("isTransport", transiver.isTransport());
                        bundle.putBoolean("isStationary", transiver.isStationary());
                        UpdateStmFragment.UpdateStmConfDialog dialog = new UpdateStmFragment.UpdateStmConfDialog();
                        dialog.setArguments(bundle);
                        dialog.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().CONFIRMATION_DIALOG_TAG);
                    }
                }
            });
        } else if (scannerType == UPDATE_CONTENT_TYPE) {
            // TODO: 18.07.20  set increment

            utils.getBluetooth().stopScan(true);

//            StringBuilder sb = new StringBuilder();
//            boolean isTransport = p.getTType().equals("transport");
//            boolean isStationary = p.getTType().equals("stationary");


            boolean isTransport = p.isTransport();
            boolean isStationary = p.isStationary();


            Logger.d(Logger.SCANNER_ADAPTER_LOG, p.getSsid() + " isTransport: " + isTransport + ", isStationary: " + isStationary);

            textItem0.setVisibility(View.VISIBLE);
            Logger.d(Logger.SCANNER_ADAPTER_LOG, "increement of content: " + p.getIncrementOfContent());
            if(p.getIncrementOfContent() == null|| p.getIncrementOfContent().isEmpty()){
                textItem0.setText("no incr");
            }

            else {
                if(isTransport){
                    TransportTransiver t = (TransportTransiver) p;
                    textItem0.setText(t.getCityCode(t.getCity()) + " " + p.getIncrementOfContent());
                }
                else {
                    textItem0.setText(p.getIncrementOfContent());
                }
            }


            Logger.d(Logger.SCANNER_ADAPTER_LOG, "p: " + p.getSsid() + " isTransport: " + isTransport);
            Logger.d(Logger.SCANNER_ADAPTER_LOG, "p: " + p.getSsid() + " isStationary: " + isStationary);
            textItem1.setText("no updates");

            if(isTransport){
                if(Downloader.tempUpdateTransportContentFiles != null){
                    textItem1.setText("          ");

//                    for(String s: Downloader.tempUpdateTransportContentFiles){
//                        sb.append(s.substring(0, s.indexOf("/")));
//                        sb.append(", ");
//                    }
//                    sb.delete(sb.lastIndexOf(","), sb.length());
//                    textItem1.setText(sb.toString());
                }
            }

            else if(isStationary){
                if(Downloader.tempUpdateStationaryContentFiles != null
                        && Downloader.tempUpdateStationaryContentFiles.containsKey(ssid.getText())){
                    textItem1.setText(Downloader.tempUpdateStationaryContentFiles.get(ssid.getText()));
                }
                else {
                    textItem1.setText("no updates");
                }
            }
            textItem1.setVisibility(View.VISIBLE);

            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Logger.d(Logger.SCANNER_ADAPTER_LOG, "linear layout was pressed");
                    Transiver transiver = getTransiver(position);
                    Bundle bundle = new Bundle();
                    bundle.putString("ip", transiver.getIp());
                    bundle.putBoolean("isTransport", isTransport);
                    bundle.putBoolean("isStationary", isStationary);
                    DialogFragment dialogFragment = null;
                    if(isTransport){
                        dialogFragment = new UpdateContentFragment.UpdateContentConfDialog();
                    }
                    else if(isStationary){

                        String key = p.getSsid();
                        if(Downloader.tempUpdateStationaryContentFiles.containsKey(key)){
                            bundle.putString("key", key);
                            bundle.putString("value", key + "/data.tar.bz2");
                            dialogFragment = new UpdateContentFragment.UpdateContentConfDialog.UploadContentConfDialog();
                        }
                    }
                    if(dialogFragment != null){
                        dialogFragment.setArguments(bundle);
                        dialogFragment.show(App.get().getFragmentHandler().getFragmentManager(),
                                App.get().getFragmentHandler().CONFIRMATION_DIALOG_TAG);
                    }
                }
            });

        } else if (scannerType == CONFIG_TRANSPORT) {
            Logger.d(Logger.SCANNER_ADAPTER_LOG, "config transport");
            if (p.isTransport()) {
                try {
                    TransportTransiver transportTransiver = (TransportTransiver) p;
                    textItem0.setText(transportTransiver.getTransportType() + " / " + transportTransiver.getFullNumber());
                    textItem0.setVisibility(View.VISIBLE);
                    textItem1.setText(transportTransiver.getDirection() + "");
                    textItem1.setVisibility(View.VISIBLE);
                    view.setOnClickListener(configListener(FragmentHandler.TRANSPORT_CONTENT_FRAGMENT, p.getSsid()));
                } catch (ClassCastException e){}
            }

        } else if (scannerType == CONFIG_STATION) {
            Logger.d(Logger.SCANNER_ADAPTER_LOG, "p.isstationary: " + p.isStationary() + " " + p.getSsid());
            if (p.isStationary()) {
                if(p.getIncrementOfContent() != null && p.getIncrementOfContent().isEmpty()){
                    textItem0.setText("no incr");
                }
                else {
                    textItem0.setText(p.getIncrementOfContent());
                }
                textItem0.setVisibility(View.VISIBLE);
                view.setOnClickListener(configListener(FragmentHandler.STATION_CONTENT_FRAGMENT, p.getSsid()));
            }
        }
        return view;
    }

    Transiver getTransiver(int position) {
        return ((Transiver) getItem(position));
    }


    private View.OnClickListener configListener(String fragment, String ssid) {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("ssid", ssid);
                App.get().getFragmentHandler().changeFragmentBundle(fragment, bundle);
            }
        };
        return onClickListener;
    }


    @Override
    public void onTaskCompleted(Bundle result) {

    }

    @Override
    public void onProgressUpdate(Integer downloaded) {

    }
}

