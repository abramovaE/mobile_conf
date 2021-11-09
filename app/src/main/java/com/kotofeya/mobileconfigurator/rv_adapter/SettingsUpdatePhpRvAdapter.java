package com.kotofeya.mobileconfigurator.rv_adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.fragments.update.UpdateOsFragment;
import com.kotofeya.mobileconfigurator.transivers.Transiver;
import java.util.List;

public class SettingsUpdatePhpRvAdapter extends RvAdapter {

    public static final String SSH_CONN = "ssh_conn";

    public SettingsUpdatePhpRvAdapter(Context context, Utils utils, List<Transiver> objects) {
        super(context, utils, objects);
    }

    @Override
    public void onBindViewHolderStep2(ViewHolder holder, int position) {
        Transiver transiver = getTransiver(position);
        String version = utils.getVersion(transiver.getSsid());
        TextView textItem0 = holder.getRvCustomView().getTextItem0();
        TextView ssid = holder.getRvCustomView().getSsid();
        RvAdapterView linearLayout = holder.getRvCustomView();
        textItem0.setText(version);
        textItem0.setVisibility(View.VISIBLE);
        Logger.d(Logger.UPDATE_LOG, "update php step 2, version: " + version);

        if(version != null && !version.startsWith(SSH_CONN)) {
            textItem0.setTextColor(ContextCompat.getColor(ctx, R.color.black));
            ssid.setTextColor(ContextCompat.getColor(ctx, R.color.black));
        } else {
            textItem0.setTextColor(ContextCompat.getColor(ctx, R.color.lightGrey));
            ssid.setTextColor(ContextCompat.getColor(ctx, R.color.lightGrey));
        }
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (version != null && !version.startsWith(SSH_CONN)) {
                    Logger.d(Logger.SCANNER_ADAPTER_LOG, "Update php was pressed");
                    Bundle bundle = new Bundle();
                    bundle.putString(BundleKeys.IP_KEY, transiver.getIp());
                    UpdateOsFragment.UpdatePhpConfDialog dialog = new UpdateOsFragment.UpdatePhpConfDialog();
                    dialog.setArguments(bundle);
                    dialog.show(App.get().getFragmentHandler().getFragmentManager(),
                            App.get().getFragmentHandler().CONFIRMATION_DIALOG_TAG);
                } else {
                    Toast.makeText(ctx, "Не удается установить ssh-подключение", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}