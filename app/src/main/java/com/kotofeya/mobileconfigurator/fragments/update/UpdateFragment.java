package com.kotofeya.mobileconfigurator.fragments.update;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.InternetConn;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.ProgressBarInt;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.fragments.scanner.ScannerFragment;
import com.kotofeya.mobileconfigurator.rv_adapter.AdapterListener;

public abstract class UpdateFragment extends ScannerFragment
        implements OnTaskCompleted, ProgressBarInt,
        IUpdateFragment,  AdapterListener {

    private static final String TAG = UpdateFragment.class.getSimpleName();
    protected String version = "version";

    protected static final int MOBILE_SETTINGS_RESULT = 0;


    @Override
    public void onStart() {
        Logger.d(TAG, "onStart");
        super.onStart();
        binding.version1.setVisibility(View.VISIBLE);
        binding.version1.setText(version);
        binding.checkVersionBtn.setVisibility(View.VISIBLE);
        viewModel.setMainBtnRescanVisibility(View.VISIBLE);
    }

    public AdapterListener getAdapterListener(){ return this; }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);

        binding.checkVersionBtn.setOnClickListener(v -> {
            Logger.d(TAG, "check updates button was pressed");
            boolean isInternetEnabled = InternetConn.hasInternetConnection();
            if(isInternetEnabled){
                scannerFragmentVM.setProgressBarVisibility(View.VISIBLE);
                loadUpdates();
            } else {
                EnableMobileConfDialog dialog = new EnableMobileConfDialog();
                dialog.show(fragmentHandler.getFragmentManager(),
                        FragmentHandler.ENABLE_MOBILE_DIALOG_TAG);
            }
        });
        loadVersion();
        scan();

        scannerFragmentVM.getProgressTvVisibility().observe(getViewLifecycleOwner(), this::updateProgressTvVisibility);
        scannerFragmentVM.getProgressTvText().observe(getViewLifecycleOwner(), this::updateProgressTvText);
        return binding.getRoot();
    }

    private void updateProgressTvText(String s) {
        binding.progressTv.setText(s);
    }

    private void updateProgressTvVisibility(int visibility) {
        binding.progressTv.setVisibility(visibility);
    }

    @Override
    public void onTaskCompleted(Bundle result) {
        Logger.d(TAG, "onTaskCompleted()");
        int resultCode = result.getInt(BundleKeys.RESULT_CODE_KEY);
        String resultStr = result.getString(BundleKeys.RESULT_KEY);
        String ipStr = result.getString(BundleKeys.IP_KEY);
        Logger.d(TAG, "ssh task completed: ip: " + ipStr + ", resultCode: " + resultCode);
        switch (resultCode){
            case TaskCode.SSH_ERROR_CODE:
                scannerFragmentVM.setProgressBarVisibility(View.GONE);
                if(resultStr.contains("Connection refused") || resultStr.contains("Auth fail")){
                    Logger.d(TAG, "result: " + result + ", remove client: " + ipStr);
                    viewModel.removeClient(ipStr);
                } else {
                    Logger.d(TAG, "ssh error: " + result);
                    fragmentHandler.showMessage("Error: " + result);
                }
                break;
            case TaskCode.DOWNLOADER_ERROR_CODE:
                Logger.d(TAG, "downloader error: " + result);
                fragmentHandler.showMessage("Error: " + result);
                scannerFragmentVM.setProgressBarVisibility(View.GONE);
                break;
        }
    }



    @Override
    public void onProgressUpdate(Integer downloaded) {
        binding.scannerProgressBar.setProgress(downloaded);
    }

    @Override
    public void setProgressBarVisibility(int visibility) {
        scannerFragmentVM.setProgressBarVisibility(visibility);
    }


    public static class EnableMobileConfDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Logger.d(TAG, "show enable mobile config dialog");
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(R.string.mobile_internet_title);
            builder.setMessage(R.string.mobile_internet_message);
            builder.setPositiveButton(getString(R.string.ok), (dialog, id) -> {
                Logger.d(TAG, "ok btn was pressed, show settings");
                startActivityForResult(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS), MOBILE_SETTINGS_RESULT);
            });
            builder.setNegativeButton(getString(R.string.cancel), (dialog, id) ->
                    Logger.d(TAG, "cancel btn was pressed, keep working without mobile internet"));
            builder.setCancelable(true);
            return builder.create();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MOBILE_SETTINGS_RESULT) {
            loadVersion();
        }
    }
}