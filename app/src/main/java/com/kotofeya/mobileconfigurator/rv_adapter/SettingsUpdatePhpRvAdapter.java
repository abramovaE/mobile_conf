package com.kotofeya.mobileconfigurator.rv_adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.fragments.update.UpdateOsFragment;
import com.kotofeya.mobileconfigurator.transivers.Transiver;
import java.util.List;

public class SettingsUpdatePhpRvAdapter extends RvAdapter {
    public SettingsUpdatePhpRvAdapter(Context context, Utils utils, List<Transiver> objects) {
        super(context, utils, objects);
    }
    @Override
    public void onBindViewHolder(@NonNull RvAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        String version = utils.getVersion(transiver.getSsid());
        textItem0.setText(version);
        textItem0.setVisibility(View.VISIBLE);
        if(version != null && !version.startsWith("ssh_conn")) {
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Logger.d(Logger.SCANNER_ADAPTER_LOG, "Update php was pressed");
                    Bundle bundle = new Bundle();
                    bundle.putString(BundleKeys.IP_KEY, transiver.getIp());
                    UpdateOsFragment.UpdatePhpConfDialog dialog = new UpdateOsFragment.UpdatePhpConfDialog();
                    dialog.setArguments(bundle);
                    dialog.show(App.get().getFragmentHandler().getFragmentManager(),
                            App.get().getFragmentHandler().CONFIRMATION_DIALOG_TAG);
                }
            });
        }
    }
}
