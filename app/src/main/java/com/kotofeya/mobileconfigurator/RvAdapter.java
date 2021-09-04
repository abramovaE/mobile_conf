package com.kotofeya.mobileconfigurator;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.kotofeya.mobileconfigurator.fragments.update.SettingsUpdateCoreFragment;
import com.kotofeya.mobileconfigurator.fragments.update.UpdateContentFragment;
import com.kotofeya.mobileconfigurator.fragments.update.UpdateOsFragment;
import com.kotofeya.mobileconfigurator.fragments.update.UpdateStmFragment;
import com.kotofeya.mobileconfigurator.transivers.Transiver;
import com.kotofeya.mobileconfigurator.transivers.TransportTransiver;

import java.util.List;

public class RvAdapter extends RecyclerView.Adapter<RvAdapter.ViewHolder> {
    public static final int BASIC_SCANNER_TYPE = 0;
    public static final int BLE_SCANNER_TYPE = 1;
    public static final int UPDATE_OS_TYPE = 2;
    public static final int UPDATE_STM_TYPE = 3;
    public static final int UPDATE_CONTENT_TYPE = 4;
    public static final int CONFIG_TRANSPORT = 5;
    public static final int CONFIG_STATION = 6;
    public static final int STM_LOG = 7;
    public static final int SETTINGS_WIFI = 8;
    public static final int SETTINGS_NETWORK = 9;
    public static final int SETTINGS_SCUART = 10;
    public static final int SETTINGS_UPDATE_PHP = 11;
    public static final int SETTINGS_UPDATE_CORE = 12;
    Context ctx;
    LayoutInflater lInflater;
    List<Transiver> objects;
    Utils utils;
    int scannerType;

    public RvAdapter(Context context, Utils utils, int scannerType, List<Transiver> objects) {
        this.ctx = context;
        this.utils = utils;
        this.objects = objects;
        this.lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.scannerType = scannerType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RvAdapterView itemView = new RvAdapterView(parent.getContext());
        itemView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Transiver p = getTransiver(position);
            TextView ssid = holder.getRvCustomView().getSsid();
            TextView textItem0 = holder.getRvCustomView().getTextItem0();
            TextView textItem1 = holder.getRvCustomView().getTextItem1();
            TextView textItem2 = holder.getRvCustomView().getTextItem2();
            TextView exp = holder.getRvCustomView().getExp();
            Button expButton = holder.getRvCustomView().getExpButton();
            ConstraintLayout linearLayout = holder.getRvCustomView();

            Logger.d(Logger.MAIN_LOG, "scannertype: " + scannerType);

            if(p != null) {
                ssid.setText(p.getSsid());
                textItem0.setText(p.getOsVersion());
                textItem1.setText(p.getStmFirmware());
                textItem2.setText(p.getStmBootloader());
                expButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        exp.setVisibility(exp.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
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
                            Logger.d(Logger.SCANNER_ADAPTER_LOG, "Update os was pressed");
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
                            Logger.d(Logger.SCANNER_ADAPTER_LOG, "Update stm was pressed");
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
                    if (p != null) {
                        boolean isTransport = p.isTransport();
                        boolean isStationary = p.isStationary();
                        textItem0.setVisibility(View.VISIBLE);
                        if (p.getIncrementOfContent() == null || p.getIncrementOfContent().isEmpty()) {
                            textItem0.setText("no incr");
                        } else {
                            if (isTransport) {
                                TransportTransiver t = (TransportTransiver) p;
                                textItem0.setText(t.getCityCode(t.getCity()) + " " + p.getIncrementOfContent());
                            } else {
                                textItem0.setText(p.getIncrementOfContent());
                            }
                        }
                        textItem1.setText("no updates");
                        if (isTransport) {
                            if (Downloader.tempUpdateTransportContentFiles != null) {
                                textItem1.setText("          ");
                            }
                        } else if (isStationary) {
                            if (Downloader.tempUpdateStationaryContentFiles != null
                                    && Downloader.tempUpdateStationaryContentFiles.containsKey(ssid.getText())) {
                                textItem1.setText(Downloader.tempUpdateStationaryContentFiles.get(ssid.getText()));
                            } else {
                                textItem1.setText("no updates");
                            }
                        }
                        textItem1.setVisibility(View.VISIBLE);

                        linearLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Logger.d(Logger.SCANNER_ADAPTER_LOG, "Update content was pressed");
                                utils.getNewBleScanner().stopScan();
//                    utils.getBluetooth().stopScan(true);
                                Transiver transiver = getTransiver(position);
                                Bundle bundle = new Bundle();
                                bundle.putString("ip", transiver.getIp());
                                bundle.putBoolean("isTransport", isTransport);
                                bundle.putBoolean("isStationary", isStationary);
//                    Logger.d(Logger.SCANNER_ADAPTER_LOG, "putToBundle: " + isTransport + " " + isStationary);
                                DialogFragment dialogFragment = null;
                                if (isTransport) {
                                    dialogFragment = new UpdateContentConfDialog();
                                } else if (isStationary) {
                                    String key = p.getSsid();
                                    if (Downloader.tempUpdateStationaryContentFiles != null &&
                                            Downloader.tempUpdateStationaryContentFiles.containsKey(key)) {
                                        bundle.putString("key", key);
                                        bundle.putString("value", key + "/data.tar.bz2");
                                        dialogFragment = new UploadContentConfDialog();
                                    }
                                }
                                if (dialogFragment != null) {
                                    dialogFragment.setArguments(bundle);
                                    dialogFragment.show(App.get().getFragmentHandler().getFragmentManager(),
                                            App.get().getFragmentHandler().CONFIRMATION_DIALOG_TAG);
                                }
                            }
                        });
                    }
                } else if (scannerType == CONFIG_TRANSPORT) {
                    Logger.d(Logger.SCANNER_ADAPTER_LOG, "Config transport");
                    if (p.isTransport()) {
                        try {
                            TransportTransiver transportTransiver = (TransportTransiver) p;
                            textItem0.setText(transportTransiver.getTransportType() + " / " + transportTransiver.getFullNumber());
                            textItem0.setVisibility(View.VISIBLE);
                            textItem1.setText(transportTransiver.getStringDirection());
                            textItem1.setVisibility(View.VISIBLE);
                            linearLayout.setOnClickListener(configListener(FragmentHandler.TRANSPORT_CONTENT_FRAGMENT, p.getSsid()));
                        } catch (ClassCastException e) {
                        }
                    }

                } else if (scannerType == CONFIG_STATION) {
                    Logger.d(Logger.SCANNER_ADAPTER_LOG, "p.is stationary: " + p.isStationary() + " " + p.getSsid());
                    Logger.d(Logger.SCANNER_ADAPTER_LOG, "p.getincrement: " + p.getIncrementOfContent());

                    if (p.isStationary()) {
                        if (p.getIncrementOfContent() != null && p.getIncrementOfContent().isEmpty()) {
                            textItem0.setText("no incr");
                        } else {
                            textItem0.setText(p.getIncrementOfContent());
                        }
                        textItem0.setVisibility(View.VISIBLE);
                        holder.getRvCustomView().setOnClickListener(configListener(FragmentHandler.STATION_CONTENT_FRAGMENT, p.getSsid()));
                    }
                }
                else if (scannerType == STM_LOG) {
                    Logger.d(Logger.SCANNER_ADAPTER_LOG, "transiver selected");
                    linearLayout.setOnClickListener(configListener(FragmentHandler.TRANSIVER_STM_LOG_FRAGMENT, p.getSsid()));
                }
                else if (scannerType == SETTINGS_WIFI) {
                    Logger.d(Logger.SCANNER_ADAPTER_LOG, "transiver selected");
                    linearLayout.setOnClickListener(configListener(FragmentHandler.TRANSIVER_SETTINGS_WIFI_FRAGMENT, p.getSsid()));
                }
                else if (scannerType == SETTINGS_NETWORK) {
                    Logger.d(Logger.SCANNER_ADAPTER_LOG, "transiver selected");
                    linearLayout.setOnClickListener(configListener(FragmentHandler.TRANSIVER_SETTINGS_NETWORK_FRAGMENT, p.getSsid()));
                }
                else if (scannerType == SETTINGS_SCUART) {
                    Logger.d(Logger.SCANNER_ADAPTER_LOG, "transiver selected");
                    linearLayout.setOnClickListener(configListener(FragmentHandler.TRANSIVER_SETTINGS_SCUART_FRAGMENT, p.getSsid()));
                }
                else if(scannerType == SETTINGS_UPDATE_PHP){
                    Logger.d(Logger.SCANNER_ADAPTER_LOG, "transiver selected");
                    textItem0.setVisibility(View.VISIBLE);
                    String version = utils.getVersion(p.getSsid());
                    textItem0.setText(version);
                    if(version != null && !version.startsWith("ssh_conn")) {
                        linearLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Logger.d(Logger.SCANNER_ADAPTER_LOG, "Update php was pressed");
                                Transiver transiver = getTransiver(position);
                                Bundle bundle = new Bundle();
                                bundle.putString("ip", transiver.getIp());
                                UpdateOsFragment.UpdatePhpConfDialog dialog = new UpdateOsFragment.UpdatePhpConfDialog();
                                dialog.setArguments(bundle);
                                dialog.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().CONFIRMATION_DIALOG_TAG);
                            }
                        });
                    }
                }

                else if(scannerType == SETTINGS_UPDATE_CORE){

                    textItem0.setVisibility(View.VISIBLE);
                    String version = utils.getVersion(p.getSsid());
                    String ip = utils.getIp(p.getSsid());

                    if(version == null){
                        textItem0.setText("old");
                    } else {
                        textItem0.setText("new");
                    }

                    Logger.d(Logger.SCANNER_ADAPTER_LOG, "transiver selected, ip: " + ip + ", version: " + version);


                    if(ip != null) {
                        linearLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Logger.d(Logger.SCANNER_ADAPTER_LOG, "Update core was pressed");
                                Transiver transiver = getTransiver(position);
                                Bundle bundle = new Bundle();
                                bundle.putString("ip", transiver.getIp());
                                bundle.putString("serial", transiver.getSsid());


                                if(Downloader.isCoreUpdatesDownloadCompleted()){
                                    SettingsUpdateCoreFragment.UpdateCoreConfDialog dialog = new SettingsUpdateCoreFragment.UpdateCoreConfDialog();
                                    dialog.setArguments(bundle);
                                    dialog.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().CONFIRMATION_DIALOG_TAG);
                                }
                                else{
                                    SettingsUpdateCoreFragment.DownloadFilesDialog dialog = new SettingsUpdateCoreFragment.DownloadFilesDialog();
                                    dialog.setArguments(bundle);
                                    dialog.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().DOWNLOAD_FILES_DIALOG);
                                }


                            }
                        });
                    }
                }

            }
        }





    private View.OnClickListener configListener(String fragment, String ssid) {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.d(Logger.SCANNER_ADAPTER_LOG, "onClick: " + ssid);
                Bundle bundle = new Bundle();
                bundle.putString("ssid", ssid);
                App.get().getFragmentHandler().changeFragmentBundle(fragment, bundle);
            }
        };
        return onClickListener;
    }

    Transiver getTransiver(int position) {
        if(objects.size() > position)
            return objects.get(position);
        return null;
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    public static  class ViewHolder extends RecyclerView.ViewHolder{
        private RvAdapterView customView;
        public ViewHolder(View itemView) {
            super(itemView);
            customView = (RvAdapterView) itemView;
        }
        public RvAdapterView getRvCustomView(){
            return customView;
        }
    }

    public List<Transiver> getObjects() {
        return objects;
    }

    public void setObjects(List<Transiver> objects) {
        this.objects = objects;
    }
}