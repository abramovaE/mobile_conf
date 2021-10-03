package com.kotofeya.mobileconfigurator.rv_adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.List;

public class ConfigStationRvAdapter extends RvAdapter {
    public ConfigStationRvAdapter(Context context, Utils utils, List<Transiver> objects, String fragmentTag) {
        super(context, utils, objects, fragmentTag);
    }
    @Override
    public String getExpText(Transiver transiver) {
        return "";
    }

    @Override
    public void onBindViewHolderStep2(ViewHolder holder, int position) {
        RvAdapterView linearLayout = holder.getRvCustomView();
        TextView textItem0 = holder.getRvCustomView().getTextItem0();
        Transiver transiver = getTransiver(position);
        if (transiver.isStationary()) {
            if (transiver.getIncrementOfContent() != null && transiver.getIncrementOfContent().isEmpty()) {
                textItem0.setText("no incr");
            } else {
                textItem0.setText(transiver.getIncrementOfContent());
            }
            textItem0.setVisibility(View.VISIBLE);
            linearLayout.setOnClickListener(configListener(position, fragmentTag));
        }
    }
}
