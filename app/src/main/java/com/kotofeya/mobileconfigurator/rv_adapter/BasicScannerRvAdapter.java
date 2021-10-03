package com.kotofeya.mobileconfigurator.rv_adapter;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.transivers.Transiver;
import java.util.List;

public class BasicScannerRvAdapter extends RvAdapter {
    public BasicScannerRvAdapter(Context context, Utils utils, List<Transiver> objects) {
        super(context, utils, objects);
    }

    @Override
    public String getExpText(Transiver transiver) {
        return transiver.getExpBasicScanInfo();
    }

    @Override
    public void onBindViewHolderStep2(ViewHolder holder, int position) {
        TextView textItem0 = holder.getRvCustomView().getTextItem0();
        TextView textItem1 = holder.getRvCustomView().getTextItem1();
        TextView textItem2 = holder.getRvCustomView().getTextItem2();
        Button expButton = holder.getRvCustomView().getExpButton();
        textItem0.setVisibility(View.VISIBLE);
        textItem1.setVisibility(View.VISIBLE);
        textItem2.setVisibility(View.VISIBLE);
        expButton.setVisibility(View.VISIBLE);
    }
}
