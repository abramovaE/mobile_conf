package com.kotofeya.mobileconfigurator.presentation.rv_adapter;

import android.view.View;

import androidx.annotation.NonNull;

import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;

import java.util.List;

public class BleScannerRvAdapter extends RvAdapter {
    public BleScannerRvAdapter(List<Transceiver> objects) {
        super(objects);
    }

    @Override
    public String getExpText(Transceiver transiver) {
        return "";
//        return transiver.getBleExpText();
    }

    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.setExpBtnVisibility(View.VISIBLE);
    }
}
