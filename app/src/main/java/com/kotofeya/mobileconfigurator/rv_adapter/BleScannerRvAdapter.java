package com.kotofeya.mobileconfigurator.rv_adapter;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.List;

public class BleScannerRvAdapter extends RvAdapter {
    public BleScannerRvAdapter(Context context, Utils utils, List<Transiver> objects) {
        super(context, utils, objects);
    }

    @Override
    public void onBindViewHolder(@NonNull RvAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        expButton.setVisibility(View.VISIBLE);
        exp.setText(transiver.getBleExpText());
    }
}
