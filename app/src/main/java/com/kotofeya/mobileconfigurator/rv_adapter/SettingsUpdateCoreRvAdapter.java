package com.kotofeya.mobileconfigurator.rv_adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Downloader;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.fragments.update.SettingsUpdateCoreFragment;
import com.kotofeya.mobileconfigurator.transivers.Transiver;
import java.util.List;

public class SettingsUpdateCoreRvAdapter extends RvAdapter {
    public SettingsUpdateCoreRvAdapter(Context context, Utils utils, List<Transiver> objects) {
        super(context, utils, objects);
    }

    @Override
    public String getExpText(Transiver transiver) {
        return "";
    }

    @Override
    public void onBindViewHolderStep2(ViewHolder holder, int position) {
        Transiver transiver = getTransiver(position);
        String version = utils.getVersion(transiver.getSsid());
        String ip = utils.getIp(transiver.getSsid());
        TextView textItem0 = holder.getRvCustomView().getTextItem0();
        RvAdapterView linearLayout = holder.getRvCustomView();

        textItem0.setText((version == null) ? "old" : "new");
        textItem0.setVisibility(View.VISIBLE);
        Logger.d(Logger.SCANNER_ADAPTER_LOG, "transiver selected, ip: " + ip + ", version: " + version);
        if(ip != null) {
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Logger.d(Logger.SCANNER_ADAPTER_LOG, "Update core was pressed");
                    Bundle bundle = new Bundle();
                    bundle.putString(BundleKeys.IP_KEY, ip);
                    bundle.putString(BundleKeys.SERIAL_KEY, transiver.getSsid());
                    DialogFragment dialog;
                    if(Downloader.isCoreUpdatesDownloadCompleted()){
                        dialog = new SettingsUpdateCoreFragment.UpdateCoreConfDialog();
                    } else{
                        dialog = new SettingsUpdateCoreFragment.DownloadFilesDialog();
                    }
                    dialog.setArguments(bundle);
                    dialog.show(App.get().getFragmentHandler().getFragmentManager(),
                            App.get().getFragmentHandler().DIALOG_FRAGMENT_TAG);
                }
            });
        }
    }
}
