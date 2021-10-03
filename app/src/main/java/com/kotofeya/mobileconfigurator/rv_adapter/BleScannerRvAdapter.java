package com.kotofeya.mobileconfigurator.rv_adapter;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.List;

public class BleScannerRvAdapter extends RvAdapter {
    public BleScannerRvAdapter(Context context, Utils utils, List<Transiver> objects) {
        super(context, utils, objects);
    }

    @Override
    public String getExpText(Transiver transiver) {
        return transiver.getBleExpText();
    }

    @Override
    public void onBindViewHolderStep2(ViewHolder holder, int position) {
        Button expButton = holder.getRvCustomView().getExpButton();
        expButton.setVisibility(View.VISIBLE);
    }
}
