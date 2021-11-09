package com.kotofeya.mobileconfigurator.rv_adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.transivers.Transiver;
import com.kotofeya.mobileconfigurator.transivers.TransportTransiver;

import java.util.List;

public class ConfigTransportRvAdapter extends RvAdapter {
    public ConfigTransportRvAdapter(Context context, Utils utils, List<Transiver> objects, String fragmentTag) {
        super(context, utils, objects, fragmentTag);
    }

    @Override
    public void onBindViewHolderStep2(ViewHolder holder, int position) {
        Transiver transiver = getTransiver(position);
        TextView textItem0 = holder.getRvCustomView().getTextItem0();
        TextView textItem1 = holder.getRvCustomView().getTextItem1();
        RvAdapterView linearLayout = holder.getRvCustomView();
        if (transiver.isTransport()) {
            try {
                TransportTransiver transportTransiver = (TransportTransiver) transiver;
                textItem0.setText(transportTransiver.getTransportType() + " / " + transportTransiver.getFullNumber());
                textItem0.setVisibility(View.VISIBLE);
                textItem1.setText(transportTransiver.getStringDirection());
                textItem1.setVisibility(View.VISIBLE);
                linearLayout.setOnClickListener(configListener(position, fragmentTag));
            } catch (ClassCastException e) {
            }
        }
    }
}