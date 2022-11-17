package com.kotofeya.mobileconfigurator.presentation.rv_adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class RvAdapter extends RecyclerView.Adapter<RvAdapter.ViewHolder> {

    public static final int GENERAL_TYPE = -1;
    public static final int BASIC_SCANNER_TYPE = 0;
    public static final int UPDATE_OS_TYPE = 1;
    public static final int UPDATE_STM_TYPE = 2;
    public static final int UPDATE_CONTENT_TYPE = 3;
    public static final int CONFIG_TRANSPORT = 4;
    public static final int CONFIG_STATION = 5;
    public static final int SETTINGS = 6;
    public static final int SETTINGS_UPDATE_PHP = 7;
    public static final int SETTINGS_UPDATE_CORE = 8;
    public static final String NO_UPDATES = "no updates";
    public static final String NO_INCR = "no incr";
    public static final String SSH_CONN = "ssh_conn";

    private List<Transceiver> objects;
    private final RvAdapterType adapterType;
    protected AdapterListener adapterListener;
    private static final String TAG = RvAdapter.class.getSimpleName();

    private Map<String, String> statVersions;
    private List<String> transpVersions;

    public RvAdapter(List<Transceiver> objects,
                     AdapterListener adapterListener,
                     RvAdapterType adapterType) {
        this.objects = objects;
        this.objects.sort(Comparator.comparing(Transceiver::getSsid));
        this.adapterListener = adapterListener;
        this.adapterType = adapterType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId;
        switch (viewType){
            case BASIC_SCANNER_TYPE:
                layoutId = R.layout.item_basic_scanner_type;
                break;
            case UPDATE_CONTENT_TYPE:
                layoutId = R.layout.item_update_content_type;
                break;
            default:
                layoutId = R.layout.item_general_type;
                break;
        }
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(layoutId, parent, false);
        itemView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Logger.d(TAG, "onBindViewHolder(), type: " + adapterType);
        Transceiver transceiver = getTransceiver(position);

        if(transceiver != null) {
            String ssid = transceiver.getSsid();
            boolean isTransport = transceiver.isTransport();
            boolean isStationary = transceiver.isStationary();

            if(transceiver.getUpdatingTime() != null) {
                String updatingSsid = ssid + " " + transceiver.getUpdatingTime();
                holder.ssid.setText(updatingSsid);
            } else {
                holder.ssid.setText(ssid);
            }

            holder.itemView.setOnClickListener(v ->
                    adapterListener.adapterItemOnClick(transceiver));

            if(adapterType != null) {
                String text0;
                switch (adapterType) {
                    case BASIC_SCANNER_TYPE:
                        text0 = "os: " + transceiver.getOsVersion() +
                                "\n stm: " + transceiver.getStmFirmware() +
                                "\n bootloader: " + transceiver.getStmBootloader();

                        holder.expButton.setOnClickListener(v -> {
                            holder.expText.setText(transceiver.getExpBasicScanInfo());
                            holder.expText
                                    .setVisibility(holder.expText.getVisibility() == View.GONE
                                    ? View.VISIBLE : View.GONE);
                            holder.expButton
                                    .setText(holder.expText.getVisibility() == View.GONE ? "+" : "-");
                        });
                        holder.text0.setText(text0);
                        break;

                    case CONFIG_STATION:
                    case CONFIG_TRANSPORT:
                        text0 = "os: \n" + transceiver.getOsVersion();
                        holder.text0.setText(text0);
                        break;

                    case UPDATE_CONTENT_TYPE:
                        StringBuilder sb = new StringBuilder();
                        sb.append(transceiver.getTType())
                                .append("\n")
                                .append(transceiver.getContent())
                                .append("os: ")
                                .append(transceiver.getOsVersion());
                        text0 = sb.toString().isEmpty() ? NO_INCR : sb.toString();
                        holder.text1.setText(NO_UPDATES);

                        if (isTransport) {
                            if (transpVersions != null) {
                                holder.text1.setText("");
                            }
                        } else if (isStationary) {
                            if (statVersions != null
                                    && statVersions.containsKey(ssid)) {
                                holder.text1.setText(statVersions.get(ssid));
                            }
                        }
                        holder.text0.setText(text0);
                        break;

                    case UPDATE_STM_TYPE:
                        text0 = "stm: " + transceiver.getStmFirmware();
                        holder.text0.setText(text0);
                        break;

                    case UPDATE_OS_TYPE:
                        text0 = transceiver.getOsVersion();
                        holder.text0.setText(text0);
                        break;

                    case SETTINGS_UPDATE_CORE:
                        text0 = transceiver.getVersionString();
                        holder.text0.setText(text0);
                        break;

                    case SETTINGS_UPDATE_PHP:
                        String version = transceiver.getVersion();
                        text0 = version;
                        Context context = App.get().getApplicationContext();
                        int color;
                        if(version != null && !version.startsWith(SSH_CONN)) {
                            color = ContextCompat.getColor(context, R.color.black);
                        } else {
                            color = ContextCompat.getColor(context, R.color.lightGrey);
                        }
                        holder.text0.setText(text0);
                        holder.text0.setTextColor(color);
                        holder.ssid.setTextColor(color);
                        break;
                    }
                }
            }
    }

    public Transceiver getTransceiver(int position) {
        if(objects.size() > position)
            return objects.get(position);
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        switch (adapterType){
            case BASIC_SCANNER_TYPE:
                return BASIC_SCANNER_TYPE;
            case UPDATE_OS_TYPE:
                return UPDATE_OS_TYPE;
            case UPDATE_STM_TYPE:
                return UPDATE_STM_TYPE;
            case UPDATE_CONTENT_TYPE:
                return UPDATE_CONTENT_TYPE;
            case CONFIG_TRANSPORT:
                return CONFIG_TRANSPORT;
            case CONFIG_STATION:
                return CONFIG_STATION;
            case SETTINGS:
                return SETTINGS;
            case SETTINGS_UPDATE_PHP:
                return SETTINGS_UPDATE_PHP;
            case SETTINGS_UPDATE_CORE:
                return SETTINGS_UPDATE_CORE;
            default:
                return GENERAL_TYPE;
        }
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public ViewHolder(View itemView) {
            super(itemView);
        }
        TextView ssid = itemView.findViewById(R.id.ssid);
        TextView text0 = itemView.findViewById(R.id.text0);
        TextView text1 = itemView.findViewById(R.id.text1);
        Button expButton = itemView.findViewById(R.id.exp_btn);
        TextView expText = itemView.findViewById(R.id.exp_text);
    }

    public void setObjects(List<Transceiver> objects) {
        this.objects = objects;
    }

    public void setStatVersions(Map<String, String> map){
        this.statVersions = map;
    }
    public void setTranspVersions(List<String> versions){
        this.transpVersions = versions;
    }

}