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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.List;

public class ScannerAdapter extends BaseAdapter implements OnTaskCompleted{
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
            LinearLayout linearLayout = view.findViewById(R.id.scanner_lv);

            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Logger.d(Logger.SCANNER_ADAPTER_LOG, "linear layout was pressed");
                    Transiver transiver = getTransiver(position);
                    utils.setCurrentTransiver(transiver);
                    Logger.d(Logger.SCANNER_ADAPTER_LOG, "updateOsFileLength: " + Downloader.tempUpdateOsFile.length());

                    if(Downloader.tempUpdateOsFile.length() > 1000){
                        Bundle bundle = new Bundle();
                        bundle.putString("transIp", transiver.getIp());
                        UpdateOsConfDialog dialog = new UpdateOsConfDialog();
                        dialog.setArguments(bundle);
                        dialog.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().CONFIRMATION_DIALOG_TAG);
                    }
                }
            });
        }

        else if(scannerType == UPDATE_STM_TYPE){
            stmFirmware.setVisibility(View.VISIBLE);
            LinearLayout linearLayout = view.findViewById(R.id.scanner_lv);
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Logger.d(Logger.SCANNER_ADAPTER_LOG, "linear layout was pressed");
                    Transiver transiver = getTransiver(position);
                    utils.setCurrentTransiver(transiver);
                    Logger.d(Logger.SCANNER_ADAPTER_LOG, "updateStmFilsSize: " + Downloader.tempUpdateStmFiles.size());

                    if(Downloader.tempUpdateStmFiles != null && !Downloader.tempUpdateStmFiles.isEmpty()){
                        Bundle bundle = new Bundle();
                        bundle.putString("transIp", transiver.getIp());
                        UpdateStmConfDialog dialog = new UpdateStmConfDialog();
                        dialog.setArguments(bundle);
                        dialog.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().CONFIRMATION_DIALOG_TAG);
                    }
                }
            });


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

    @Override
    public void onTaskCompleted(String result) {

    }

    @Override
    public void onProgressUpdate(Integer downloaded) {

    }


    public static class UpdateOsConfDialog extends DialogFragment {


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String ip = getArguments().getString("transIp");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Confirmation is required");
        builder.setMessage("Confirm the upload of the updates");
        builder.setPositiveButton("upload", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SshConnection connection = new SshConnection(((UpdateOsFragment)App.get().getFragmentHandler().getCurrentFragment()));
                connection.execute(ip, SshConnection.UPDATE_OS_LOAD_FILE_COMMAND);
            }
        });

        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        builder.setCancelable(true);
        return builder.create();
    }


    }


    public static class UpdateStmConfDialog extends DialogFragment {


        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String ip = getArguments().getString("transIp");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Choose the stm version for upload");

            String[] content = Downloader.tempUpdateStmFiles.toArray(new String[Downloader.tempUpdateStmFiles.size()]);
//            for(String s: content){
//                Logger.d(Logger.SCANNER_ADAPTER_LOG, "dialogContent: " + s);
//            }

            builder.setItems(content,
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Logger.d(Logger.SCANNER_ADAPTER_LOG, "dialogContent: " + content[which]);
                    Downloader downloader = new Downloader((UpdateStmFragment)App.get().getFragmentHandler().getCurrentFragment());
                    downloader.execute(content[which]);

                    
//                    SshConnection connection = new SshConnection(((UpdateStmFragment)App.get().getFragmentHandler().getCurrentFragment()));
//                    connection.execute(ip, SshConnection.UPDATE_STM_LOAD_FILE_COMMAND, content[which]);
                }
            });

//            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int id) {
//                }
//            });

            builder.setCancelable(true);
            return builder.create();
        }


    }

}