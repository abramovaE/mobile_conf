package com.kotofeya.mobileconfigurator.fragments.update;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;

public class UpdatePhpConfDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String ip = getArguments().getString(BundleKeys.IP_KEY);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.confirmation_is_required);
        builder.setMessage("Confirm the update of PHP version");

        builder.setPositiveButton("Update", (dialog, id) -> {
            FragmentHandler fragmentHandler = ((MainActivity)requireActivity()).getFragmentHandler();
            Thread thread = new Thread(
                    new PostInfo((SettingsUpdatePhpFragment) fragmentHandler.getCurrentFragment(),
                            ip, PostCommand.UPDATE_PHP));
            thread.start();
        });
        builder.setNegativeButton("cancel", (dialog, id) -> {
        });
        builder.setCancelable(true);
        return builder.create();
    }
}
