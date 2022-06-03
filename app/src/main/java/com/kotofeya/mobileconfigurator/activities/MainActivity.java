package com.kotofeya.mobileconfigurator.activities;

import static com.kotofeya.mobileconfigurator.newBleScanner.CustomBluetooth.REQUEST_BT_ENABLE;
import static com.kotofeya.mobileconfigurator.newBleScanner.CustomBluetooth.REQUEST_GPS_ENABLE;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.gson.Gson;
import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.City;
import com.kotofeya.mobileconfigurator.Downloader;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.SendLogToServer;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.databinding.ActivityMainClBinding;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.SshCommand;
import com.kotofeya.mobileconfigurator.network.post_response.TakeInfoFull;
import com.kotofeya.mobileconfigurator.newBleScanner.CustomBluetooth;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity  implements OnTaskCompleted, InterfaceUpdateListener {

    public final static String TAG = MainActivity.class.getSimpleName();
    public static City[] cities;

    private ActivityMainClBinding binding;

    Utils utils;

    private static final int TETHER_REQUEST_CODE = 1;
    private static final String HOTSPOT_DIALOG_TAG = "HOTSPOT_DIALOG";
    private CustomBluetooth newBleScanner;
    private CustomViewModel viewModel;
    private AlertDialog scanClientsDialog;
    private FragmentHandler fragmentHandler;

    @Override
    public void onStart() {
        super.onStart();
        newBleScanner.stopScan();
        utils.startRvTimer();
        scanClientsDialog = utils.getScanClientsDialog();
        scanClientsDialog.show();
        utils.updateClients(this);
    }

    private void launchHotspotSettings() {
        Intent tetherSettings = new Intent();
        tetherSettings.setClassName("com.android.settings",
                "com.android.settings.TetherSettings");
        startActivityForResult(tetherSettings, TETHER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TETHER_REQUEST_CODE:
                break;
            case REQUEST_BT_ENABLE:
            case REQUEST_GPS_ENABLE:
                newBleScanner.startScan();
                break;
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
        newBleScanner = new CustomBluetooth(this);
        utils = new Utils(this, newBleScanner);
        fragmentHandler = new FragmentHandler(this);

        fragmentHandler.changeFragment(FragmentHandler.MAIN_FRAGMENT_TAG, false);

        binding.mainBtnRescan.setVisibility(View.GONE);
        binding.mainTxtLabel.setText(R.string.main_menu_main_label);

        binding.mainTxtLogin.setText(App.get().getLogin());
        Runnable runnable = new CountDownRunner();
        Thread timerThread = new Thread(runnable);
        timerThread.start();
        Downloader cityDownloader = new Downloader(this);
        cityDownloader.execute(Downloader.CITY_URL);
        if (App.get().isAskForTeneth()) {
            HotSpotSettingsDialog dialog = new HotSpotSettingsDialog();
            dialog.show(fragmentHandler.getFragmentManager(), HOTSPOT_DIALOG_TAG);
        }
        boolean isInternetEnabled = utils.getInternetConnection().hasInternetConnection();
        if (isInternetEnabled) {
            new Thread(new SendLogToServer(Logger.getServiceLogString(), this)).start();
        }
        viewModel = ViewModelProviders.of(this, new CustomViewModel.ModelFactory()).get(CustomViewModel.class);
        viewModel.getClients().observe(this, this::updateUI);

        viewModel.mainTxtLabel().observe(this, this::updateMainTxtLabel);
        viewModel.mainBtnRescanVisibility().observe(this, this::updateBtnRescanVisibility);
    }

    private void updateBtnRescanVisibility(Integer integer) {
        binding.mainBtnRescan.setVisibility(integer);
    }

    private void updateMainTxtLabel(String s) {
        binding.mainTxtLabel.setText(s);
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 10;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
    };


    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public CustomViewModel getViewModel() {
        return viewModel;
    }

    private void updateUI(List<String> strings) {
        String text = "(" + strings.size() + ")";
        binding.mainTxtDevCount.setText(text);
    }

    @Override
    public void clientsScanFinished() {
        Logger.d(TAG, "clientsScanFinished()");
        scanClientsDialog.dismiss();
    }

    public static class HotSpotSettingsDialog extends DialogFragment implements CompoundButton.OnCheckedChangeListener {
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
            App.get().setAskForTeneth(!isChecked);
        }
    }

    @Override
    public void onBackPressed() {
        binding.mainTxtLabel.setText(R.string.main_menu_main_label);
        binding.mainBtnRescan.setVisibility(View.GONE);
        super.onBackPressed();
    }

    public Utils getUtils() {
        return utils;
    }

    @Override
    public void onTaskCompleted(Bundle result) {
        String command = result.getString(BundleKeys.COMMAND_KEY);
        String ip = result.getString(BundleKeys.IP_KEY);
        String response = result.getString(BundleKeys.RESPONSE_KEY);
        Parcelable parcelableResponse = result.getParcelable(BundleKeys.PARCELABLE_RESPONSE_KEY);
        int resultCode = result.getInt(BundleKeys.RESULT_CODE_KEY);

        String res = result.getString(BundleKeys.RESULT);
        if(command == null){
            command = "";
        }
        if (command.equals(PostCommand.POST_COMMAND_ERROR) || command.equals(SshCommand.SSH_COMMAND_ERROR)) {
            if (response.contains("Connection refused") || response.contains("Auth fail")) {
                utils.removeClient(ip);
            } else {
                fragmentHandler.showMessage("Error: " + response);
            }
        } else if (command.equals(PostCommand.TAKE_INFO_FULL)) {
            String version = result.getString(BundleKeys.VERSION_KEY);
            Logger.d(Logger.MAIN_LOG, "version: " + version);
            viewModel.addTakeInfoFull(ip, version, (TakeInfoFull) parcelableResponse, true);
        } else if (command.equals(SshCommand.SSH_TAKE_COMMAND)) {
            viewModel.addTakeInfo(response, true);
        } else if (resultCode == TaskCode.DOWNLOAD_CITIES_CODE) {
            getCities(res);
        } else if (resultCode == TaskCode.SEND_LOG_TO_SERVER_CODE) {
            int code = result.getInt("code");
            Logger.d(Logger.MAIN_LOG, "send log to server code: " + resultCode);
            if (code == 1) {
                Logger.clearLogReport();
            }
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
    public void onProgressUpdate(Integer downloaded) {
    }

    public void doWork() {
        runOnUiThread(() -> {
            try {
                TextView txtCurrentTime = findViewById(R.id.main_txt_date);
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy/HH:mm:ss", Locale.getDefault());
                String currentDateTime = sdf.format(new Date());
                txtCurrentTime.setText(currentDateTime);
            } catch (Exception e) {
            }
        });
    }

    class CountDownRunner implements Runnable {
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    doWork();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    protected void onStop() {
        Logger.d(Logger.MAIN_LOG, "main activity on stop");
        boolean isInternetEnabled = utils.getInternetConnection().hasInternetConnection();
        Thread setToServer;
        if (isInternetEnabled) {
            setToServer = new Thread(new SendLogToServer(Logger.getServiceLogString(), this));
            setToServer.start();
        }
        newBleScanner.stopScan();
        utils.stopClientsHandler();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        newBleScanner.stopScan();
        utils.startRvTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        newBleScanner.stopScan();
    }
}