package com.kotofeya.mobileconfigurator.presentation.rv_adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;
import com.kotofeya.mobileconfigurator.network.response.TakeInfoStatContent;
import com.kotofeya.mobileconfigurator.network.response.TakeInfoTranspContent;

import java.util.Comparator;
import java.util.List;

public class RvAdapter extends RecyclerView.Adapter<RvAdapter.ViewHolder>
        implements ExpTextInt{
    private List<Transceiver> objects;
    private RvAdapterType adapterType;
    protected AdapterListener adapterListener;
    public static final String SSH_CONN = "ssh_conn";
    private static final String TAG = RvAdapter.class.getSimpleName();

    public RvAdapter(List<Transceiver> objects) {

        this.objects = objects;
        this.objects.sort(Comparator.comparing(Transceiver::getSsid));
    }

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
        RvAdapterView itemView = new RvAdapterView(parent.getContext());
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
            String increment = transceiver.getIncrementOfContent();

            String version = transceiver.getVersion();

            holder.setSsid(ssid);
            holder.setTextItem0Text("os: " + transceiver.getOsVersion()) ;
            holder.setTextItem1Text("stm: " + transceiver.getStmFirmware());
            holder.setTextItem2Text("bl: " + transceiver.getStmBootloader());

            holder.setExpButtonListener(v -> {
                holder.setExpText(getExpText(transceiver));
                holder.setExpVisibility();
                holder.setExpButtonText();
            });

            if(adapterType != null) {

                switch (adapterType) {
                    case SETTINGS:
                        holder.setCustomViewOnClickListener(v -> adapterListener.adapterItemOnClick(transceiver));
                        break;

                    case CONFIG_STATION:
                        if (isStationary) {
                            holder.setIncrement(increment);
                            holder.setTextItem0Visibility(View.VISIBLE);
                            holder.setCustomViewOnClickListener(v -> adapterListener.adapterItemOnClick(transceiver));
                        }
                        break;

                    case CONFIG_TRANSPORT:
                        Logger.d(TAG, "rvAdapter: " + adapterType);
                        Logger.d(TAG, "rvAdapter listener: " + adapterListener);

                        if (isTransport) {
                            holder.setCustomViewOnClickListener(v -> adapterListener.adapterItemOnClick(transceiver));
                        }
                        break;

                    case UPDATE_CONTENT_TYPE:
                        holder.setTextItem0Visibility(View.VISIBLE);
                        StringBuilder sb = new StringBuilder();
                        sb.append(transceiver.getTType());
                        sb.append("\n");
                        Logger.d(TAG, "tr contents: " + transceiver.getSsid() + " " + transceiver.isTransport());

                        if(transceiver.isTransport()) {
                            Logger.d(TAG, "tr contents: " + transceiver.getTranspContents());
                            if(transceiver.getTranspContents() != null) {
                                for (TakeInfoTranspContent transpContent : transceiver.getTranspContents()) {
                                    sb.append(transpContent.getLocalRouteList()).append(" - ").append(transpContent.getIncrRouteList());
                                    sb.append("\n");
                                }
                            }
                        } else if(transceiver.isStationary() && transceiver.getStatContents() != null){
                            for (TakeInfoStatContent statContent : transceiver.getStatContents()) {
                                sb.append(statContent.describeContents()).append(" - ").append(statContent.getShortInfo());
                                sb.append("\n");
                            }
                        }

                        sb.append("os: ").append(transceiver.getOsVersion());
                        holder.setIncrement(sb.toString());
                        holder.setTextItem1Text("no updates");
//                        if (isTransport) {
//                            if (UpdateContentFragment2.tempUpdateTransportContentFiles != null) {
//                                holder.setTextItem1Text("          ");
//                            }
//                        } else if (isStationary) {
//                            Map<String, String> contentFiles = UpdateContentFragment2.tempUpdateStationaryContentFiles;
//                            if (contentFiles != null
//                                    && contentFiles.containsKey(ssid)) {
//                                holder.setTextItem1Text(contentFiles.get(ssid));
//                            }
//                        }
                        holder.setTextItem1Visibility(View.VISIBLE);
                        holder.setCustomViewOnClickListener(v -> adapterListener.adapterItemOnClick(transceiver));
                        break;

                    case UPDATE_STM_TYPE:
                        holder.setTextItem1Visibility(View.VISIBLE);
                        holder.setCustomViewOnClickListener(v -> adapterListener.adapterItemOnClick(transceiver));
                        break;

                    case UPDATE_OS_TYPE:
                        holder.setTextItem0Visibility(View.VISIBLE);
                        holder.setCustomViewOnClickListener(v -> adapterListener.adapterItemOnClick(transceiver));
                        break;

                    case SETTINGS_UPDATE_CORE:
                        if(transceiver.getUpdatingTime() != null) {
                            holder.setSsid(ssid + " " + transceiver.getUpdatingTime());
                        }
                        String versionString = version + " " + (version.equals(Transceiver.VERSION_UNDEFINED) ? "old" : "new");
                        holder.setTextItem0Text(versionString);
                        holder.setTextItem0Visibility(View.VISIBLE);
                        holder.setCustomViewOnClickListener(v -> adapterListener.adapterItemOnClick(transceiver));
                        break;

                    case SETTINGS_UPDATE_PHP:
                        holder.setTextItem0Text(version);
                        holder.setTextItem0Visibility(View.VISIBLE);
                        holder.setVersionColor(version);
                        holder.setCustomViewOnClickListener(v -> adapterListener.adapterItemOnClick(transceiver));
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
    public int getItemCount() {
        return objects.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final RvAdapterView customView;
        public ViewHolder(View itemView) {
            super(itemView);
            customView = (RvAdapterView) itemView;
        }

        public void setSsid(String ssid){
            customView.setSsid(ssid);
        }

        public void setTextItem0Visibility(int visibility){
            customView.setTextItem0Visibility(visibility);
        }
        public void setTextItem1Visibility(int visibility){
            customView.setTextItem1Visibility(visibility);
        }
        public void setTextItem2Visibility(int visibility){
            customView.setTextItem2Visibility(visibility);
        }
        public void setTextItem0Text(String text){
            Logger.d(TAG, "setTextItem0Text(), text: " + text);
            customView.setTextItem0Text(text);
        }
        public void setTextItem1Text(String text){
            customView.setTextItem1Text(text);
        }
        public void setTextItem2Text(String text){
            customView.setTextItem2Text(text);
        }

        public void setExpBtnVisibility(int visibility){
            customView.setExpBtnVisibility(visibility);
        }
        public void setCustomViewOnClickListener(View.OnClickListener listener){
//            customView.setClickable(true);
            customView.setOnClickListener(listener);
        }
        public void setExpText(String text){
            customView.setExpText(text);
        }
        public void setExpVisibility(){
            customView.setExpVisibility();
        }
        public void setExpButtonText(){
            customView.setExpButtonText();
        }
        public void setExpButtonListener(View.OnClickListener listener) {
            customView.setExpButtonListener(listener);
        }

        private void setIncrement(String increment){
            if (increment != null && increment.isEmpty()) {
                customView.setTextItem0Text("no incr");
            } else {
                customView.setTextItem0Text(increment);
            }
        }

        private void setVersionColor(String version){
            Context context = App.get().getApplicationContext();
            if(version != null && !version.startsWith(SSH_CONN)) {
                customView.setTextItem0Color(ContextCompat.getColor(context, R.color.black));
                customView.setSsidColor(ContextCompat.getColor(context, R.color.black));
            } else {
                customView.setTextItem0Color(ContextCompat.getColor(context, R.color.lightGrey));
                customView.setSsidColor(ContextCompat.getColor(context, R.color.lightGrey));
            }
        }
    }

    public void setObjects(List<Transceiver> objects) {
        this.objects = objects;
    }
}