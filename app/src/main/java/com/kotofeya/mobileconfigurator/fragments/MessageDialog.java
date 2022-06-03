package com.kotofeya.mobileconfigurator.fragments;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

class MessageDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        String message = requireArguments().getString("message");
        builder.setMessage(message);
        builder.setPositiveButton("ok", (dialog, id) -> {});
        builder.setCancelable(true);
        return builder.create();
    }
//
//    public static void showMessage(FragmentManager fragmentManager, String message){
//        Bundle bundle = new Bundle();
//        bundle.putString("message", message);
//        MessageDialog dialog = new MessageDialog();
//        dialog.setArguments(bundle);
//        dialog.show(fragmentManager, FragmentHandler.CONFIRMATION_DIALOG_TAG);
//    }
}