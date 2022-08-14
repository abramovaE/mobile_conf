package com.kotofeya.mobileconfigurator.rv_adapter;

import android.view.View;

import androidx.annotation.NonNull;

import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;
import java.util.List;

public class BasicScannerRvAdapter extends RvAdapter {
    public BasicScannerRvAdapter(List<Transceiver> objects) {
        super(objects);
    }

    @Override
    public String getExpText(Transceiver transiver) {
        return transiver.getExpBasicScanInfo();
    }

    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.setTextItem0Visibility(View.VISIBLE);
        holder.setTextItem1Visibility(View.VISIBLE);
        holder.setTextItem2Visibility(View.VISIBLE);
        holder.setExpBtnVisibility(View.VISIBLE);
    }
}
