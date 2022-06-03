package com.kotofeya.mobileconfigurator.rv_adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Downloader;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.activities.CustomViewModel;
import com.kotofeya.mobileconfigurator.transivers.Transiver;
import com.kotofeya.mobileconfigurator.transivers.TransportTransiver;

import java.util.List;
import java.util.Map;

public class RvAdapter extends RecyclerView.Adapter<RvAdapter.ViewHolder>
        implements ExpTextInt{
    private List<Transiver> objects;
    private RvAdapterType adapterType;
    protected AdapterListener adapterListener;
    public static final String SSH_CONN = "ssh_conn";

    public RvAdapter(List<Transiver> objects) {
        this.objects = objects;
    }

    public RvAdapter(List<Transiver> objects,
                     AdapterListener adapterListener,
                     RvAdapterType adapterType) {
        this.objects = objects;
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
        Transiver transiver = getTransceiver(position);
        if(transiver != null) {
            String ssid = transiver.getSsid();
            boolean isTransport = transiver.isTransport();
            boolean isStationary = transiver.isStationary();
            String increment = transiver.getIncrementOfContent();
            String version = CustomViewModel.getVersion(transiver.getSsid());

            holder.setSsid(ssid);
            holder.setTextItem0Text(transiver.getOsVersion());
            holder.setTextItem1Text(transiver.getStmFirmware());
            holder.setTextItem2Text(transiver.getStmBootloader());
            holder.setExpButtonListener(v -> {
                holder.setExpText(getExpText(transiver));
                holder.setExpVisibility();
                holder.setExpButtonText();
            });


            switch (adapterType){
                case SETTINGS:
                    holder.setCustomViewOnClickListener(v -> adapterListener.adapterItemOnClick(transiver));
                    break;

                case CONFIG_STATION:
                    if (isStationary) {
                        holder.setIncrement(increment);
                        holder.setTextItem0Visibility(View.VISIBLE);
                        holder.setCustomViewOnClickListener(v -> adapterListener.adapterItemOnClick(transiver));
                    }
                    break;

                case CONFIG_TRANSPORT:
                    if (isTransport) {
                        TransportTransiver transportTransiver = (TransportTransiver) transiver;
                        holder.setTextItem0Text(transportTransiver.getTransportType() + " / " + transportTransiver.getFullNumber());
                        holder.setTextItem0Visibility(View.VISIBLE);
                        holder.setTextItem1Text(transportTransiver.getStringDirection());
                        holder.setTextItem1Visibility(View.VISIBLE);
                        holder.setCustomViewOnClickListener(v-> adapterListener.adapterItemOnClick(transiver));
                    }
                    break;

                case UPDATE_CONTENT_TYPE:
                        holder.setTextItem0Visibility(View.VISIBLE);
                        holder.setIncrement(increment);
                        holder.setTextItem1Text("no updates");
                        if (isTransport) {
                            TransportTransiver t = (TransportTransiver) transiver;
                            holder.setTextItem0Text(t.getCityCode(t.getCity()) + " " + increment);
                            if (Downloader.tempUpdateTransportContentFiles != null) {
                                holder.setTextItem1Text("          ");
                            }
                        } else if (isStationary) {
                            Map<String, String> contentFiles = Downloader.tempUpdateStationaryContentFiles;
                            if (contentFiles != null
                                    && contentFiles.containsKey(ssid)) {
                                holder.setTextItem1Text(contentFiles.get(ssid));
                            }
                        }
                        holder.setTextItem1Visibility(View.VISIBLE);
                        holder.setCustomViewOnClickListener(v -> adapterListener.adapterItemOnClick(transiver));
                    break;

                case UPDATE_STM_TYPE:
                    holder.setTextItem1Visibility(View.VISIBLE);
                    holder.setCustomViewOnClickListener(v -> adapterListener.adapterItemOnClick(transiver));
                    break;

                case UPDATE_OS_TYPE:
                    holder.setTextItem0Visibility(View.VISIBLE);
                    holder.setCustomViewOnClickListener(v -> adapterListener.adapterItemOnClick(transiver));
                    break;

                case SETTINGS_UPDATE_CORE:
                    holder.setTextItem0Text((version == null) ? "old" : "new");
                    holder.setTextItem0Visibility(View.VISIBLE);
                    holder.setCustomViewOnClickListener(v -> adapterListener.adapterItemOnClick(transiver));
                    break;

                case SETTINGS_UPDATE_PHP:
                    holder.setTextItem0Text(version);
                    holder.setTextItem0Visibility(View.VISIBLE);
                    holder.setVersionColor(version);
                    holder.setCustomViewOnClickListener(v -> adapterListener.adapterItemOnClick(transiver));
                    break;
            }
        }
    }

    public Transiver getTransceiver(int position) {
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

    public List<Transiver> getObjects() {
        return objects;
    }
    public void setObjects(List<Transiver> objects) {
        this.objects = objects;
    }
}