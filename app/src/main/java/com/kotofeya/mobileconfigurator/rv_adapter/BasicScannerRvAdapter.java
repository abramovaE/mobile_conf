package com.kotofeya.mobileconfigurator.rv_adapter;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;

import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.transivers.Transiver;
import java.util.List;

public class BasicScannerRvAdapter extends RvAdapter {
    public BasicScannerRvAdapter(Context context, Utils utils, List<Transiver> objects) {
        super(context, utils, objects);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        textItem0.setVisibility(View.VISIBLE);
        textItem1.setVisibility(View.VISIBLE);
        textItem2.setVisibility(View.VISIBLE);
        expButton.setVisibility(View.VISIBLE);
        exp.setText(transiver.getExpBasicScanInfo());
    }
}
