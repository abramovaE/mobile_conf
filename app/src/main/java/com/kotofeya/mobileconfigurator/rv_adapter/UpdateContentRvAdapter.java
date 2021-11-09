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
import com.kotofeya.mobileconfigurator.UpdateContentConfDialog;
import com.kotofeya.mobileconfigurator.UploadContentConfDialog;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.transivers.Transiver;
import com.kotofeya.mobileconfigurator.transivers.TransportTransiver;
import java.util.List;

public class UpdateContentRvAdapter extends RvAdapter {
    public UpdateContentRvAdapter(Context context, Utils utils, List<Transiver> objects) {
        super(context, utils, objects);
    }

    @Override
    public void onBindViewHolderStep2(ViewHolder holder, int position) {
        Transiver transiver = getTransiver(position);
        TextView textItem0 = holder.getRvCustomView().getTextItem0();
        TextView textItem1 = holder.getRvCustomView().getTextItem1();
        RvAdapterView linearLayout = holder.getRvCustomView();

        if (transiver != null) {
            TextView ssid = holder.getRvCustomView().getSsid();
            boolean isTransport = transiver.isTransport();
            boolean isStationary = transiver.isStationary();
            textItem0.setVisibility(View.VISIBLE);
            if (transiver.getIncrementOfContent() == null || transiver.getIncrementOfContent().isEmpty()) {
                textItem0.setText("no incr");
            } else {
                String increment = transiver.getIncrementOfContent();
                if (isTransport) {
                    TransportTransiver t = (TransportTransiver) transiver;
                    textItem0.setText(t.getCityCode(t.getCity()) + " " + increment);
                } else {
                    textItem0.setText(increment);
                }
            }
            textItem1.setText("no updates");
            if (isTransport) {
                if (Downloader.tempUpdateTransportContentFiles != null) {
                    textItem1.setText("          ");
                }
            } else if (isStationary) {
                if (Downloader.tempUpdateStationaryContentFiles != null
                        && Downloader.tempUpdateStationaryContentFiles.containsKey(ssid.getText())) {
                    textItem1.setText(Downloader.tempUpdateStationaryContentFiles.get(ssid.getText()));
                }
            }
            textItem1.setVisibility(View.VISIBLE);
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Logger.d(Logger.SCANNER_ADAPTER_LOG, "Update content was pressed, isTransport: " +
                            isTransport + ", isStationary: " + isStationary);
                    utils.getNewBleScanner().stopScan();
                    Bundle bundle = new Bundle();
                    bundle.putString(BundleKeys.IP_KEY, transiver.getIp());
                    bundle.putBoolean(BundleKeys.IS_TRANSPORT_KEY, isTransport);
                    bundle.putBoolean(BundleKeys.IS_STATIONARY_KEY, isStationary);
                    DialogFragment dialogFragment = null;
                    if (isTransport) {
                        dialogFragment = new UpdateContentConfDialog();
                    } else if (isStationary) {
                        String key = transiver.getSsid();
                        if (Downloader.tempUpdateStationaryContentFiles != null &&
                                Downloader.tempUpdateStationaryContentFiles.containsKey(key)) {
                            bundle.putString(BundleKeys.KEY, key);
                            bundle.putString(BundleKeys.VALUE, key + "/data.tar.bz2");
                            dialogFragment = new UploadContentConfDialog();
                        }
                    }
                    if (dialogFragment != null) {
                        dialogFragment.setArguments(bundle);
                        dialogFragment.show(App.get().getFragmentHandler().getFragmentManager(),
                                App.get().getFragmentHandler().CONFIRMATION_DIALOG_TAG);
                    }
                }
            });
        }
    }
}