package com.kotofeya.mobileconfigurator;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.kotofeya.mobileconfigurator.fragments.update.UpdateContentFragment;
import com.kotofeya.mobileconfigurator.fragments.update.UpdateFragment;
import com.kotofeya.mobileconfigurator.fragments.update.UpdateOsFragment;
import com.kotofeya.mobileconfigurator.fragments.update.UpdateStmFragment;
import com.kotofeya.mobileconfigurator.transivers.Transiver;
import com.kotofeya.mobileconfigurator.transivers.TransportTransiver;

import java.util.List;

public class ScannerAdapter extends BaseAdapter implements OnTaskCompleted {
    Context ctx;
    LayoutInflater lInflater;
    List<Transiver> objects;
    Utils utils;

    private int scannerType;

    public List<Transiver> getObjects() {
        return objects;
    }

    public void setObjects(List<Transiver> objects) {
        this.objects = objects;
    }

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

    public ScannerAdapter(Context context, Utils utils, int scannerType, List<Transiver> objects) {
        this.ctx = context;
        this.utils = utils;
        this.objects = objects;
        this.lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.scannerType = scannerType;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        if(objects.size() > position)
        return objects.get(position);
        return null;
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
        TextView textItem0 = view.findViewById(R.id.scanner_lv_item0);
        TextView textItem1 = view.findViewById(R.id.scanner_lv_item1);
        LinearLayout linearLayout = view.findViewById(R.id.scanner_lv);

        Logger.d(Logger.SCANNER_ADAPTER_LOG, "get view, p: " + p);
        Logger.d(Logger.SCANNER_ADAPTER_LOG, "scanner type: " + scannerType);


        if(p != null) {
            ssid.setText(p.getSsid());
            textItem0.setText(p.getOsVersion());
            textItem1.setText(p.getStmFirmware());
            TextView textItem2 = view.findViewById(R.id.scanner_lv_item2);
            textItem2.setText(p.getStmBootloader());

            final TextView exp = view.findViewById(R.id.scanner_lv_item_txt_exp);


            Button expButton = view.findViewById(R.id.scanner_lv_exp_btn);
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
                    Logger.d(Logger.SCANNER_ADAPTER_LOG, p.getSsid() + " isTransport: " + isTransport + ", isStationary: " + isStationary);
                    textItem0.setVisibility(View.VISIBLE);
                    Logger.d(Logger.SCANNER_ADAPTER_LOG, "increement of content: " + p.getIncrementOfContent());
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
                                dialogFragment = new UpdateContentFragment.UpdateContentConfDialog();
                            } else if (isStationary) {
                                String key = p.getSsid();
                                if (Downloader.tempUpdateStationaryContentFiles.containsKey(key)) {
                                    bundle.putString("key", key);
                                    bundle.putString("value", key + "/data.tar.bz2");
                                    dialogFragment = new UpdateContentFragment.UpdateContentConfDialog.UploadContentConfDialog();
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
                        view.setOnClickListener(configListener(FragmentHandler.TRANSPORT_CONTENT_FRAGMENT, p.getSsid()));
                    } catch (ClassCastException e) {
                    }
                }

            } else if (scannerType == CONFIG_STATION) {
                Logger.d(Logger.SCANNER_ADAPTER_LOG, "p.is stationary: " + p.isStationary() + " " + p.getSsid());
                if (p.isStationary()) {
                    if (p.getIncrementOfContent() != null && p.getIncrementOfContent().isEmpty()) {
                        textItem0.setText("no incr");
                    } else {
                        textItem0.setText(p.getIncrementOfContent());
                    }
                    textItem0.setVisibility(View.VISIBLE);
                    view.setOnClickListener(configListener(FragmentHandler.STATION_CONTENT_FRAGMENT, p.getSsid()));
                }
            }
            else if (scannerType == STM_LOG) {
                Logger.d(Logger.SCANNER_ADAPTER_LOG, "transiver selected");
                view.setOnClickListener(configListener(FragmentHandler.TRANSIVER_STM_LOG_FRAGMENT, p.getSsid()));
            }
            else if (scannerType == SETTINGS_WIFI) {
                Logger.d(Logger.SCANNER_ADAPTER_LOG, "transiver selected");
                view.setOnClickListener(configListener(FragmentHandler.TRANSIVER_SETTINGS_WIFI_FRAGMENT, p.getSsid()));
            }
            else if (scannerType == SETTINGS_NETWORK) {
                Logger.d(Logger.SCANNER_ADAPTER_LOG, "transiver selected");
                view.setOnClickListener(configListener(FragmentHandler.TRANSIVER_SETTINGS_NETWORK_FRAGMENT, p.getSsid()));
            }
            else if (scannerType == SETTINGS_SCUART) {
                Logger.d(Logger.SCANNER_ADAPTER_LOG, "transiver selected");
                view.setOnClickListener(configListener(FragmentHandler.TRANSIVER_SETTINGS_SCUART_FRAGMENT, p.getSsid()));
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

                textItem0.setText(version);

                Logger.d(Logger.SCANNER_ADAPTER_LOG, "transiver selected, ip: " + ip);

                if(ip != null) {
                    linearLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Logger.d(Logger.SCANNER_ADAPTER_LOG, "Update core was pressed");
                            Transiver transiver = getTransiver(position);
                            Bundle bundle = new Bundle();
                            bundle.putString("ip", transiver.getIp());
                            UpdateFragment.UpdateCoreConfDialog dialog = new UpdateFragment.UpdateCoreConfDialog();
                            dialog.setArguments(bundle);
                            dialog.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().CONFIRMATION_DIALOG_TAG);
                        }
                    });
                }
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
                Logger.d(Logger.SCANNER_ADAPTER_LOG, "onClick: " + ssid);
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