package com.kotofeya.mobileconfigurator.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.companion.WifiDeviceFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.aware.WifiAwareManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.rtt.WifiRttManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
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
import com.kotofeya.mobileconfigurator.FragmentHandler;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.SendLogToServer;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.WiFiLocalHotspot;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;
import com.kotofeya.mobileconfigurator.network.SshCommand;
import com.kotofeya.mobileconfigurator.network.post_response.TakeInfoFull;
import com.kotofeya.mobileconfigurator.newBleScanner.CustomBluetooth;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.kotofeya.mobileconfigurator.newBleScanner.CustomBluetooth.REQUEST_BT_ENABLE;
import static com.kotofeya.mobileconfigurator.newBleScanner.CustomBluetooth.REQUEST_GPS_ENABLE;

import javax.security.auth.login.LoginException;


/*
5. Обновление контента - при нажатии на стационарном трансивере ничего не происходит,
(данные актуальной версии подтягиваются с обсервера), если на сервере нет архива нужно об этом сообщить пользователю,
6. Обновление контента - при нажатии на транспортный трансивер вылетает окно выбора маршрутной сети,
после выбора сети подтверждение намерения загрузки - и все, если загрузка прошла успешно - нужно сообщить об этом
 */

public class MainActivity extends AppCompatActivity  implements OnTaskCompleted {

    public final static String TAG = "MainActivity";
    Utils utils;
    TextView label;
    ImageButton mainBtnRescan;
    TextView loginTxt;
    TextView devCountTxt;
    TextView dateTxt;

    public static City cities[];
    private static final int TETHER_REQUEST_CODE = 1;
    private static final String HOTSPOT_DIALOG_TAG = "HOTSPOT_DIALOG";
    private CustomBluetooth newBleScanner;

    private CustomViewModel viewModel;

//    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
////            Logger.d(TAG, "onReceive()");
//            final String action = intent.getAction();
//            Logger.d(TAG, "action: " + action);
//
////            if (action.equals()) {
////                Logger.d(TAG, "onReceive()");
////                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
////                switch(state) {
////                    case BluetoothAdapter.STATE_OFF:
////                        bScanner.setScanningFalse();
////                        Logger.d(TAG, "BluetoothAdapter.STATE_OFF");
////                        break;
////                    case BluetoothAdapter.STATE_TURNING_OFF:
////                        Logger.d(TAG, "BluetoothAdapter.STATE_TURNING_OFF");
////                        break;
////                    case BluetoothAdapter.STATE_ON:
////                        Logger.d(TAG, "BluetoothAdapter.STATE_ON");
////                        startScan();
////                        break;
////                    case BluetoothAdapter.STATE_TURNING_ON:
////                        Logger.d(TAG, "BluetoothAdapter.STATE_TURNING_ON");
////                        break;
////                }
////            }
//        }
//    };


            @Override
    public void onStart() {
        super.onStart();
        newBleScanner.stopScan();
        utils.startRvTimer();
        utils.updateClients();


//        IntentFilter adapterFilter = new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
//        adapterFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
//                adapterFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
//                adapterFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
//                adapterFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
//                adapterFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
//
//                adapterFilter.addAction(WifiRttManager.ACTION_WIFI_RTT_STATE_CHANGED);
//                adapterFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);


//        WifiP2pManager p2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
//        p2pManager.
//        registerReceiver(mBroadcastReceiver1, adapterFilter);
    }

    private void launchHotspotSettings() {
        Intent tetherSettings = new Intent();
        tetherSettings.setClassName("com.android.settings", "com.android.settings.TetherSettings");
        startActivityForResult(tetherSettings, TETHER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TETHER_REQUEST_CODE:
                break;
            case REQUEST_BT_ENABLE:
                newBleScanner.startScan();
                break;
            case REQUEST_GPS_ENABLE:
                newBleScanner.startScan();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_cl);
        verifyStoragePermissions(this);

        newBleScanner = new CustomBluetooth(this);
        utils = new Utils(this, newBleScanner);
        FragmentHandler fragmentHandler = new FragmentHandler(this);
        App.get().setFragmentHandler(fragmentHandler);
        fragmentHandler.changeFragment(FragmentHandler.MAIN_FRAGMENT_TAG, false);

        label = findViewById(R.id.main_txt_label);
        mainBtnRescan = findViewById(R.id.main_btn_rescan);
        mainBtnRescan.setVisibility(View.GONE);
        label.setText(R.string.main_menu_main_label);
        loginTxt = findViewById(R.id.main_txt_login);
        devCountTxt = findViewById(R.id.main_txt_dev_count);
        dateTxt = findViewById(R.id.main_txt_date);
        loginTxt.setText(App.get().getLogin());

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


    }


    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 10;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
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
        devCountTxt.setText("(" + strings.size() + ")");
    }

    public static class HotSpotSettingsDialog extends DialogFragment implements CompoundButton.OnCheckedChangeListener {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getResources().getString(R.string.hotspot_dialog_title));
            View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.access_point_dialog, null);
            builder.setView(dialogView);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ((MainActivity) getContext()).launchHotspotSettings();
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
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
        label.setText(R.string.main_menu_main_label);
        mainBtnRescan.setVisibility(View.GONE);
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
                utils.showMessage("Error: " + response);
            }
        } else if (command.equals(PostCommand.TAKE_INFO_FULL)) {
            String version = result.getString(BundleKeys.VERSION_KEY);
            Logger.d(Logger.MAIN_LOG, "version: " + version);
            viewModel.addTakeInfoFull(ip, version, (TakeInfoFull) parcelableResponse, true);
        } else if (command.equals(SshCommand.SSH_TAKE_COMMAND)) {
            viewModel.addTakeInfo(response, true);
        } else if (resultCode == TaskCode.SEND_LOG_TO_SERVER_CODE) {
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
            this.cities = new Gson().fromJson(jObject.get("city").toString(), City[].class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProgressUpdate(Integer downloaded) {
    }

    public void doWork() {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    TextView txtCurrentTime = findViewById(R.id.main_txt_date);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy/HH:mm:ss", Locale.getDefault());
                    String currentDateandTime = sdf.format(new Date());
                    txtCurrentTime.setText(currentDateandTime);
                } catch (Exception e) {
                }
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
        Thread setnToServer = null;
        if (isInternetEnabled) {
            setnToServer = new Thread(new SendLogToServer(Logger.getServiceLogString(), this));
            setnToServer.start();
        }
        super.onStop();
        newBleScanner.stopScan();
//        unregisterReceiver(mBroadcastReceiver1);

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