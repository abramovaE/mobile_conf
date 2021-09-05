package com.kotofeya.mobileconfigurator.rv_adapter;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.kotofeya.mobileconfigurator.FragmentHandler;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.transivers.Transiver;
import com.kotofeya.mobileconfigurator.transivers.TransportTransiver;

import java.util.List;

public class ConfigTransportRvAdapter extends RvAdapter {
    public ConfigTransportRvAdapter(Context context, Utils utils, List<Transiver> objects) {
        super(context, utils, objects);
    }

    @Override
    public void onBindViewHolder(@NonNull RvAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Logger.d(Logger.SCANNER_ADAPTER_LOG, "Config transport");
        if (transiver.isTransport()) {
            try {
                TransportTransiver transportTransiver = (TransportTransiver) transiver;
                textItem0.setText(transportTransiver.getTransportType() + " / " + transportTransiver.getFullNumber());
                textItem0.setVisibility(View.VISIBLE);
                textItem1.setText(transportTransiver.getStringDirection());
                textItem1.setVisibility(View.VISIBLE);
                linearLayout.setOnClickListener(configListener(FragmentHandler.TRANSPORT_CONTENT_FRAGMENT, transiver.getSsid()));
            } catch (ClassCastException e) {
            }
        }
    }
}
