package com.kotofeya.mobileconfigurator.rv_adapter;

import android.content.Context;

import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.List;

public class SettingsRvAdapter extends RvAdapter {
    public SettingsRvAdapter(Context context, Utils utils, List<Transiver> objects, String fragmentTag) {
        super(context, utils, objects, fragmentTag);
    }

    @Override
    public String getExpText(Transiver transiver) {
        return "";
    }

    @Override
    public void onBindViewHolderStep2(ViewHolder holder, int position) {
        RvAdapterView linearLayout = holder.getRvCustomView();
        linearLayout.setOnClickListener(configListener(position, fragmentTag));
    }
}
