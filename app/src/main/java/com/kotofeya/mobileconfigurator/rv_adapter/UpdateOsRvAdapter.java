package com.kotofeya.mobileconfigurator.rv_adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Downloader;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.fragments.update.UpdateOsFragment;
import com.kotofeya.mobileconfigurator.transivers.Transiver;
import java.util.List;

public class UpdateOsRvAdapter extends RvAdapter {
    public UpdateOsRvAdapter(Context context, Utils utils, List<Transiver> objects) {
        super(context, utils, objects);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        textItem0.setVisibility(View.VISIBLE);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Downloader.tempUpdateOsFile.length() > 1000) {
                    Bundle bundle = new Bundle();
                    bundle.putString(BundleKeys.IP_KEY, transiver.getIp());
                    UpdateOsFragment.UpdateOsConfDialog dialog = new UpdateOsFragment.UpdateOsConfDialog();
                    dialog.setArguments(bundle);
                    dialog.show(App.get().getFragmentHandler().getFragmentManager(),
                            App.get().getFragmentHandler().CONFIRMATION_DIALOG_TAG);
                }
            }
        });
    }
}
