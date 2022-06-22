package com.kotofeya.mobileconfigurator;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.user.UserFactory;
import com.kotofeya.mobileconfigurator.user.UserType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UpdateContentConfDialog extends DialogFragment {
    private static final String TAG = UpdateContentConfDialog.class.getSimpleName();
    boolean isTransport;
    boolean isStationary;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.isTransport = requireArguments().getBoolean(BundleKeys.IS_TRANSPORT_KEY);
        this.isStationary = requireArguments().getBoolean(BundleKeys.IS_STATIONARY_KEY);
        Logger.d(TAG, "isTransport: " + isTransport +", isStationary: " + isStationary);
        String ip = requireArguments().getString(BundleKeys.IP_KEY);
        Map<String, String> transportContent = getTransportContent();
        AlertDialog.Builder builder = createUpdateContentConfDialog(ip, transportContent);
        builder.setCancelable(true);
        return builder.create();
    }

    private Map<String, String> addToTransportContent(Map<String, String> transportContent,
                                                             String key, String value){
        UserType userType = UserFactory.getUser().getUserType();
        if(userType.equals(UserType.USER_FULL) || userType.equals(UserType.USER_UPDATE_CORE)) {
            transportContent.put(key, value);
        } else if(userType.equals(UserType.USER_TRANSPORT)){
            String login = App.get().getLogin();
            String region = login.substring(login.lastIndexOf("_") + 1);
            if(value.contains(region) || value.contains("zzz")) {
                transportContent.put(key, value);
            }
        }
        return transportContent;
    }

    private String getTransportFileKey(String s, boolean isInternetEnabled){
        if(isInternetEnabled){
            return s.substring(0, s.indexOf("/"));
        } else {
            return s.substring(s.lastIndexOf("/") + 1).split("_")[0];
        }
    }

    public Map<String, String> getTransportContent(){

        Map<String, String> transportContent = new HashMap<>();
        boolean isInternetEnabled = InternetConn.hasInternetConnection();
        Collection<String> collection = (isInternetEnabled) ? Downloader.tempUpdateTransportContentFiles :
                App.get().getUpdateContentFilePaths();
        for (String s : collection) {
            String key = getTransportFileKey(s, isInternetEnabled);
            transportContent = addToTransportContent(transportContent, key, s);
        }
        return transportContent;
    }


    private AlertDialog.Builder createUpdateContentConfDialog(String ip, Map<String, String> contentMap){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Choose the city for upload");
        String[] content = contentMap.keySet().toArray(new String[contentMap.size()]);
        builder.setItems(content,
                (dialog, which) -> {
                    Logger.d(TAG, "dialogContent: " + content[which]);
                    Bundle bundle = new Bundle();
                    bundle.putString(BundleKeys.KEY, "transp " + content[which]);
                    bundle.putString(BundleKeys.VALUE, contentMap.get(content[which]));
                    bundle.putString(BundleKeys.IP_KEY, ip);
                    bundle.putBoolean(BundleKeys.IS_TRANSPORT_KEY, isTransport);
                    bundle.putBoolean(BundleKeys.IS_STATIONARY_KEY, isStationary);
                    UploadContentConfDialog d = new UploadContentConfDialog();
                    d.setArguments(bundle);
                    FragmentHandler fragmentHandler = ((MainActivity)requireActivity()).getFragmentHandler();
                    d.show(fragmentHandler.getFragmentManager(), FragmentHandler.CONFIRMATION_DIALOG_TAG);
                });
        return builder;
    }
}
