package com.kotofeya.mobileconfigurator.rv_adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Downloader;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.fragments.update.UpdateStmFragment;
import com.kotofeya.mobileconfigurator.transivers.Transiver;
import java.util.List;

public class UpdateStmRvAdapter extends RvAdapter{
    public UpdateStmRvAdapter(Context context, Utils utils, List<Transiver> objects) {
        super(context, utils, objects);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        textItem1.setVisibility(View.VISIBLE);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.d(Logger.SCANNER_ADAPTER_LOG, "Update stm was pressed");
                if (Downloader.tempUpdateStmFiles != null && !Downloader.tempUpdateStmFiles.isEmpty()) {
                    Bundle bundle = new Bundle();
                    bundle.putString(BundleKeys.IP_KEY, transiver.getIp());
                    bundle.putBoolean(BundleKeys.IS_TRANSPORT_KEY, transiver.isTransport());
                    bundle.putBoolean(BundleKeys.IS_STATIONARY_KEY, transiver.isStationary());
                    UpdateStmFragment.UpdateStmConfDialog dialog = new UpdateStmFragment.UpdateStmConfDialog();
                    dialog.setArguments(bundle);
                    dialog.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().CONFIRMATION_DIALOG_TAG);
                }
            }
        });
    }
}
