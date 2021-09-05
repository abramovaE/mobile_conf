package com.kotofeya.mobileconfigurator.rv_adapter;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.kotofeya.mobileconfigurator.FragmentHandler;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.List;

public class ConfigStationRvAdapter extends RvAdapter {
    public ConfigStationRvAdapter(Context context, Utils utils, List<Transiver> objects) {
        super(context, utils, objects);
    }

    @Override
    public void onBindViewHolder(@NonNull RvAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (transiver.isStationary()) {
            if (transiver.getIncrementOfContent() != null && transiver.getIncrementOfContent().isEmpty()) {
                textItem0.setText("no incr");
            } else {
                textItem0.setText(transiver.getIncrementOfContent());
            }
            textItem0.setVisibility(View.VISIBLE);
            linearLayout.setOnClickListener(configListener(FragmentHandler.STATION_CONTENT_FRAGMENT, transiver.getSsid()));
        }
    }
}
