package com.kotofeya.mobileconfigurator.rv_adapter;

import android.content.Context;

import androidx.annotation.NonNull;

import com.kotofeya.mobileconfigurator.FragmentHandler;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.List;

public class SettingsNetworkRvAdapter extends RvAdapter {
    public SettingsNetworkRvAdapter(Context context, Utils utils, List<Transiver> objects) {
        super(context, utils, objects);
    }

    @Override
    public void onBindViewHolder(@NonNull RvAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        linearLayout.setOnClickListener(configListener(FragmentHandler.TRANSIVER_SETTINGS_NETWORK_FRAGMENT, transiver.getSsid()));
    }
}
