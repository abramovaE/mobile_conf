package com.kotofeya.mobileconfigurator.presentation.fragments;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

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
}