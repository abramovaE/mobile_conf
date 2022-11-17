package com.kotofeya.mobileconfigurator.presentation.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.gson.Gson;
import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.domain.city.City;
import com.kotofeya.mobileconfigurator.network.download.DownloadCityListener;
import com.kotofeya.mobileconfigurator.network.download.DownloadCityUseCase;
import com.kotofeya.mobileconfigurator.network.InternetConn;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.network.request.SendLogToServerListener;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.network.request.SendLogToServerUseCase;
import com.kotofeya.mobileconfigurator.databinding.ActivityMainClBinding;
import com.kotofeya.mobileconfigurator.presentation.fragments.FragmentHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class MainActivity extends AppCompatActivity
        implements DownloadCityListener {

    public final static String TAG = MainActivity.class.getSimpleName();
    public static City[] cities;
    public static final String TITLE_SCAN_CLIENTS = "";
    public static final String MESSAGE_SCAN_CLIENTS = "Поиск подключенных клиентов";

    private ActivityMainClBinding binding;
    private static final int TETHER_REQUEST_CODE = 1;
    public static final String HOTSPOT_DIALOG_TAG = "HOTSPOT_DIALOG";

    private MainActivityViewModel viewModel;
    private AlertDialog scanClientsDialog;
    private FragmentHandler fragmentHandler;

    private void init(Boolean isHotspotDialogShowing){
        if(!isHotspotDialogShowing){
            Logger.d(TAG, "init(Boolean isHotspotDialogShowing)");
            startScan();
        }
    }

    private void startScan(){
        Logger.d(TAG, "startScan()");
        viewModel.updateConnectedClients();
    }

    public void launchHotspotSettings() {
        Intent tetherSettings = new Intent();
        tetherSettings.setClassName("com.android.settings",
                "com.android.settings.TetherSettings");
        startActivityForResult(tetherSettings, TETHER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TETHER_REQUEST_CODE) {
            startScan();
        }
    }

    public FragmentHandler getFragmentHandler(){
        if(fragmentHandler == null){
            fragmentHandler = new FragmentHandler(this);
        }
        return fragmentHandler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainClBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        verifyStoragePermissions(this);

        fragmentHandler = new FragmentHandler(this);
        fragmentHandler.changeFragment(FragmentHandler.MAIN_FRAGMENT_TAG, false);

        binding.mainTxtLabel.setText(R.string.main_menu_main_label);
        binding.mainTxtLogin.setText(App.get().getLogin());

        new DownloadCityUseCase(this).newRequest();

        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        Runnable runnable = new CountDownRunner(viewModel);
        Thread timerThread = new Thread(runnable);
        timerThread.start();

        viewModel.mainBtnRescanVisibility().observe(this, this::updateBtnRescanVisibility);
        viewModel.setMainBtnRescanVisibility(View.GONE);

        if (App.get().isAskForTenet()) {
            HotSpotSettingsDialog dialog = new HotSpotSettingsDialog();
            dialog.show(fragmentHandler.getFragmentManager(), HOTSPOT_DIALOG_TAG);
            viewModel.setIsHotspotDialogShowing(true);
        } else {
            viewModel.setIsHotspotDialogShowing(false);
        }
        boolean isInternetEnabled = InternetConn.hasInternetConnection();
        if (isInternetEnabled) {
            new SendLogToServerUseCase(Logger.getServiceLogString(),
                    new SendLogToServerListener() {
                @Override
                public void sendLogSuccessful() {
                    Logger.clearLogReport();
                }
                @Override
                public void sendLogFailed(String error) {
                    Logger.d(TAG, "sendLogFailed(), error: " + error);
                }
            }).run();
        }
        viewModel.clients.observe(this, this::updateClientsCount);
        viewModel.mainTxtLabel().observe(this, this::updateMainTxtLabel);
        viewModel.isHotspotDialogShowing.observe(this, this::init);
        viewModel.isScanning.observe(this, this::updateScanClientsDialog);
        viewModel.time().observe(this, this::updateTime);
        binding.mainBtnRescan.setOnClickListener(v -> rescan());
    }





    @Override
    public void downloadSuccessful(String result) {
        Logger.d(TAG, "downloadSuccessful(): " + result);
        getCities(result);
    }

    @Override
    public void downloadFailed(String error) {
        Logger.d(TAG, "downloadFailed(): " + error);

    }



    protected void rescan(){
        Logger.d(TAG, "rescan");
        viewModel.clearClients();
        viewModel.clearTransceivers();
        viewModel.updateAndPollConnectedClients();
        viewModel.rescanPressed();
    }

    private void updateTime(String s) {
        binding.mainTxtDate.setText(s);
    }

    private void updateScanClientsDialog(Boolean isScanning) {
        Logger.d(TAG, "updateScanClientsDialog(): " + isScanning);
        if(!isScanning){
            dismissScanClientsDialog();
        } else {
            showScanClientsDialog();
        }
    }
    private void updateBtnRescanVisibility(Integer integer) {
        Logger.d(TAG, "updateBtnRescanVisibility(Integer integer): " + integer);
        binding.mainBtnRescan.setVisibility(integer);
    }
    private void updateMainTxtLabel(String s) {
        Logger.d(TAG, "updateMainTxtLabel(String s): " + s);
        binding.mainTxtLabel.setText(s);
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 10;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void updateClientsCount(List<String> strings) {
        Logger.d(TAG, "updateClientsCount(List<String> strings): " + strings.size());
        String text = "(" + strings.size() + ")";
        binding.mainTxtDevCount.setText(text);
    }

    public void hotspotDialogOnDismiss() {
        viewModel.setIsHotspotDialogShowing(false);
    }



    public static class HotSpotSettingsDialog extends DialogFragment
            implements CompoundButton.OnCheckedChangeListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(getResources().getString(R.string.hotspot_dialog_title));
            View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.access_point_dialog, null);
            builder.setView(dialogView);
            builder.setPositiveButton(R.string.ok, (dialog, id) ->
                    ((MainActivity) requireActivity()).launchHotspotSettings());
            builder.setNegativeButton(R.string.cancel, (dialog, id) -> {});
            builder.setCancelable(true);
            CheckBox doNotAskAgain = dialogView.findViewById(R.id.doNotAskCheckbox);
            doNotAskAgain.setOnCheckedChangeListener(this);
            return builder.create();
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Logger.d(Logger.MAIN_LOG, "on checked changed, is checked: " + isChecked);
            App.get().setAskForTenet(!isChecked);
        }

        @Override
        public void onDismiss(@NonNull DialogInterface dialog) {
            super.onDismiss(dialog);
            ((MainActivity)requireActivity()).hotspotDialogOnDismiss();
        }
    }

    @Override
    public void onBackPressed() {
        binding.mainTxtLabel.setText(R.string.main_menu_main_label);
        binding.mainBtnRescan.setVisibility(View.GONE);
        if(fragmentHandler.getCurrentFragment() != null
                && fragmentHandler.getCurrentFragment().getTag() != null
                &&
                (fragmentHandler.getCurrentFragment().getTag()
                .equals(FragmentHandler.CONFIG_TRANSPORT_FRAGMENT)
                || fragmentHandler.getCurrentFragment().getTag()
                .equals(FragmentHandler.CONFIG_STATION_FRAGMENT))){
            fragmentHandler.changeFragment(FragmentHandler.MAIN_FRAGMENT_TAG);
        } else {
            super.onBackPressed();
        }
    }



    private void getCities(String res) {
        try {
            JSONObject jObject = new JSONObject(res);
            cities = new Gson().fromJson(jObject.get("city").toString(), City[].class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onStop() {
        Logger.d(Logger.MAIN_LOG, "onStop()");
        boolean isInternetEnabled = InternetConn.hasInternetConnection();

        if (isInternetEnabled) {
            new SendLogToServerUseCase(Logger.getServiceLogString(),
                    new SendLogToServerListener() {
                @Override
                public void sendLogSuccessful() {
                    Logger.d(TAG, "sendLogSuccessful()");
                    Logger.clearLogReport();
                }
                @Override
                public void sendLogFailed(String error) {
                    Logger.d(TAG, "sendLogFailed()");
                }
            }).run();
        }
        viewModel.stopClientsScanning();
        super.onStop();
    }

    private void showScanClientsDialog(){
        scanClientsDialog = new AlertDialog.Builder(this)
                .setTitle(TITLE_SCAN_CLIENTS)
                .setMessage(MESSAGE_SCAN_CLIENTS)
                .setCancelable(false)
                .show();
    }

    private void dismissScanClientsDialog(){
        if(scanClientsDialog != null){
            scanClientsDialog.dismiss();
        }
    }
}