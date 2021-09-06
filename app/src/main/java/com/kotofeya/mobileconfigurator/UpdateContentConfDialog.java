package com.kotofeya.mobileconfigurator;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.kotofeya.mobileconfigurator.activities.MainActivity;

import java.util.Map;

public class UpdateContentConfDialog extends DialogFragment {
    boolean isTransport;
    boolean isStationary;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.isTransport = getArguments().getBoolean(BundleKeys.IS_TRANSPORT_KEY);
        this.isStationary = getArguments().getBoolean(BundleKeys.IS_STATIONARY_KEY);
        Logger.d(Logger.UPDATE_CONTENT_LOG, "isTransport: " + isTransport +", isStationary: " + isStationary);
        String ip = getArguments().getString(BundleKeys.IP_KEY);
        Map<String, String> transportContent = ((MainActivity) getActivity()).getUtils().getTransportContent();
        AlertDialog.Builder builder = createUpdateContentConfDialog(ip, transportContent);
        builder.setCancelable(true);
        return builder.create();
    }

    private AlertDialog.Builder createUpdateContentConfDialog(String ip, Map<String, String> contentMap){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose the city for upload");
        String[] content = contentMap.keySet().toArray(new String[contentMap.size()]);
        builder.setItems(content,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Logger.d(Logger.UPDATE_CONTENT_LOG, "dialogContent: " + content[which]);
                        Bundle bundle = new Bundle();
                        bundle.putString("key", "transp " + content[which]);
                        bundle.putString("value", contentMap.get(content[which]));
                        bundle.putString(BundleKeys.IP_KEY, ip);
                        bundle.putBoolean(BundleKeys.IS_TRANSPORT_KEY, isTransport);
                        bundle.putBoolean(BundleKeys.IS_STATIONARY_KEY, isStationary);
                        UploadContentConfDialog d = new UploadContentConfDialog();
                        d.setArguments(bundle);
                        d.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().CONFIRMATION_DIALOG_TAG);
                    }
                });
        return builder;
    }
}
