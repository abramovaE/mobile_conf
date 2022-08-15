package com.kotofeya.mobileconfigurator.fragments;


import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.MainMenuFragment;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.fragments.config.ConfigStatFragment;
import com.kotofeya.mobileconfigurator.fragments.config.ConfigTransportFragment;
import com.kotofeya.mobileconfigurator.fragments.config.StationContentFragment;
import com.kotofeya.mobileconfigurator.fragments.config.TransportContentFragment;
import com.kotofeya.mobileconfigurator.fragments.scanner.BasicScannerFragment;
import com.kotofeya.mobileconfigurator.fragments.settings.SettingsFragment;
import com.kotofeya.mobileconfigurator.fragments.transiver_settings.TransceiverSettingsNetworkFragment;
import com.kotofeya.mobileconfigurator.fragments.transiver_settings.TransceiverSettingsScUartFragment;
import com.kotofeya.mobileconfigurator.fragments.transiver_settings.TransceiverSettingsWifiFragment;
import com.kotofeya.mobileconfigurator.fragments.transiver_settings.TransceiverStmLogFragment;
import com.kotofeya.mobileconfigurator.fragments.update.SettingsNetworkFragment;
import com.kotofeya.mobileconfigurator.fragments.update.SettingsScUartFragment;
import com.kotofeya.mobileconfigurator.fragments.update.content.UpdateContentFragment2;
import com.kotofeya.mobileconfigurator.fragments.update.UpdateCoreFragment2;
import com.kotofeya.mobileconfigurator.fragments.update.SettingsUpdatePhpFragment;
import com.kotofeya.mobileconfigurator.fragments.update.SettingsWifiFragment;
import com.kotofeya.mobileconfigurator.fragments.update.StmLogFragment;
import com.kotofeya.mobileconfigurator.fragments.update.os.UpdateOsFragment2;
import com.kotofeya.mobileconfigurator.fragments.update.UpdateStmFragment;

public class FragmentHandler {

    private static final String TAG = FragmentHandler.class.getSimpleName();
    private FragmentManager fragmentManager;
    private Context context;
    private Fragment currentFragment;

    public final static String MAIN_FRAGMENT_TAG = "MAIN_FRAGMENT";
    public final static String BASIC_SCANNER_FRAGMENT = "BASIC_SCANNER_FRAGMENT";
    public final static String BLE_SCANNER_FRAGMENT = "BLE_SCANNER_FRAGMENT";
    public final static String UPDATE_OS_FRAGMENT = "UPDATE_OS_FRAGMENT";
    public final static String UPDATE_STM_FRAGMENT = "UPDATE_STM_FRAGMENT";
    public final static String UPDATE_CONTENT_FRAGMENT = "UPDATE_CONTENT_FRAGMENT";
    public final static String CONFIG_TRANSPORT_FRAGMENT = "CONFIG_TRANSPORT_FRAGMENT";
    public final static String CONFIG_STATION_FRAGMENT = "CONFIG_STATION_FRAGMENT";
    public final static String TRANSPORT_CONTENT_FRAGMENT = "TRANSPORT_CONTENT_FRAGMENT";
    public final static String STATION_CONTENT_FRAGMENT = "STATION_CONTENT_FRAGMENT";
    public final static String SETTINGS_FRAGMENT = "SETTINGS_FRAGMENT";
    public final static String STM_LOG_FRAGMENT = "STM_LOG_FRAGMENT";
    public final static String TRANSIVER_STM_LOG_FRAGMENT = "TRANSIVER_STM_LOG_FRAGMENT";
    public final static String SETTINGS_WIFI_FRAGMENT = "SETTINGS_WIFI_FRAGMENT";
    public final static String TRANSIVER_SETTINGS_WIFI_FRAGMENT = "TRANSIVER_SETTINGS_WIFI_FRAGMENT";
    public final static String SETTINGS_NETWORK_FRAGMENT = "SETTINGS_NETWORK_FRAGMENT";
    public final static String TRANSIVER_SETTINGS_NETWORK_FRAGMENT = "TRANSIVER_SETTINGS_NETWORK_FRAGMENT";
    public final static String SETTINGS_SCUART_FRAGMENT = "SETTINGS_SCUART_FRAGMENT";
    public final static String TRANSIVER_SETTINGS_SCUART_FRAGMENT = "TRANSIVER_SETTINGS_SCUART_FRAGMENT";
    public final static String SETTINGS_UPDATE_PHP_FRAGMENT = "SETTINGS_UPDATE_PHP_FRAGMENT";
    public final static String TRANSIVER_SETTINGS_UPDATE_PHP_FRAGMENT = "TRANSIVER_SETTINGS_UPDATE_PHP_FRAGMENT";
    public final static String SETTINGS_UPDATE_CORE_FRAGMENT = "SETTINGS_UPDATE_CORE_FRAGMENT";


    public final static String ENABLE_MOBILE_DIALOG_TAG = "ENABLE_MOBILE_DIALOG";
    public final static String CONFIRMATION_DIALOG_TAG = "CONFIRMATION_DIALOG";
    public final static String ADD_NEW_WIFI_SETTINGS_DIALOG = "ADD_NEW_WIFI_SETTINGS_DIALOG";
    public final static String ADD_NEW_ETHERNET_SETTINGS_DIALOG = "ADD_NEW_ETHERNET_SETTINGS_DIALOG";
    public final static String DOWNLOAD_FILES_DIALOG = "DOWNLOAD_FILES_DIALOG";
    public final static String DIALOG_FRAGMENT_TAG = "DIALOG_FRAGMENT";

    public void showMessage(String message) {
        Logger.d(TAG, "showMessage(): " + message);
        ((MainActivity) context).runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(message);
                builder.setPositiveButton(R.string.ok, (dialog, id) -> {});
                builder.setCancelable(true);
                builder.show();
            }
        });
    }

    public FragmentManager getFragmentManager() {
        return fragmentManager;
    }

    public FragmentHandler(Context context){
        this.context = context;
        fragmentManager = ((MainActivity) context).getSupportFragmentManager();
    }

    private Fragment getFragment(String fragmentTag){
        Fragment fragment = fragmentManager.findFragmentByTag(fragmentTag);
        if(fragment == null){
            switch (fragmentTag){
                case MAIN_FRAGMENT_TAG:
                    return new MainMenuFragment();
                case BASIC_SCANNER_FRAGMENT:
                    return new BasicScannerFragment();
                case UPDATE_OS_FRAGMENT:
                    return new UpdateOsFragment2();
                case UPDATE_STM_FRAGMENT:
                    return new UpdateStmFragment();
                case UPDATE_CONTENT_FRAGMENT:
                    return new UpdateContentFragment2();
                case CONFIG_TRANSPORT_FRAGMENT:
                    return new ConfigTransportFragment();
                case CONFIG_STATION_FRAGMENT:
                    return new ConfigStatFragment();
                case TRANSPORT_CONTENT_FRAGMENT:
                    return new TransportContentFragment();
                case STATION_CONTENT_FRAGMENT:
                    return new StationContentFragment();
                case SETTINGS_FRAGMENT:
                    return new SettingsFragment();
                case STM_LOG_FRAGMENT:
                    return new StmLogFragment();
                case TRANSIVER_STM_LOG_FRAGMENT:
                    return new TransceiverStmLogFragment();
                case SETTINGS_WIFI_FRAGMENT:
                    return new SettingsWifiFragment();
                case TRANSIVER_SETTINGS_WIFI_FRAGMENT:
                    return new TransceiverSettingsWifiFragment();
                case SETTINGS_NETWORK_FRAGMENT:
                    return new SettingsNetworkFragment();
                case TRANSIVER_SETTINGS_NETWORK_FRAGMENT:
                    return new TransceiverSettingsNetworkFragment();
                case SETTINGS_SCUART_FRAGMENT:
                    return new SettingsScUartFragment();
                case TRANSIVER_SETTINGS_SCUART_FRAGMENT:
                    return new TransceiverSettingsScUartFragment();
                case SETTINGS_UPDATE_PHP_FRAGMENT:
                    return new SettingsUpdatePhpFragment();
                case SETTINGS_UPDATE_CORE_FRAGMENT:
                    return new UpdateCoreFragment2();
            }
        }
        return fragment;
    }

    public void changeFragmentBundle(String fragmentTag, Bundle bundle){
        Logger.d(TAG, new Throwable().getStackTrace()[0].getMethodName() + ": " + fragmentTag + ", " + bundle);
        Fragment fragment = getFragment(fragmentTag);
        fragment.setArguments(bundle);
        setFragment(fragment, fragmentTag, true);
    }

    public void changeFragment(String fragmentTag){
        Logger.d(TAG, new Throwable().getStackTrace()[0].getMethodName() + ": " + fragmentTag);
        Fragment fragment = getFragment(fragmentTag);
        setFragment(fragment, fragmentTag, true);
    }

    public void changeFragment(String fragmentTag, boolean stacked){
        Logger.d(TAG, new Throwable().getStackTrace()[0].getMethodName() + ": " + fragmentTag + ", " + stacked);
        Fragment fragment = getFragment(fragmentTag);
        setFragment(fragment, fragmentTag, stacked);
    }

    private void setFragment(Fragment fragment, String tag, boolean stacked){
        Logger.d(TAG, new Throwable().getStackTrace()[0].getMethodName() + ": " + tag + ", " + stacked);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
        if (stacked) {
            fragmentTransaction.addToBackStack(tag);
        }
        fragmentTransaction.commit();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        currentFragment = fragment;
    }

    public Fragment getCurrentFragment() {
        return currentFragment;
    }

}