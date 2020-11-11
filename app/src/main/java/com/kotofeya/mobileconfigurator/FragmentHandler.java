package com.kotofeya.mobileconfigurator;


import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.fragments.config.ConfigStatFragment;
import com.kotofeya.mobileconfigurator.fragments.config.ConfigTransportFragment;
import com.kotofeya.mobileconfigurator.fragments.config.StationContentFragment;
import com.kotofeya.mobileconfigurator.fragments.config.TransportContentFragment;
import com.kotofeya.mobileconfigurator.fragments.scanner.BasicScannerFragment;
import com.kotofeya.mobileconfigurator.fragments.scanner.BleScannerFragment;
import com.kotofeya.mobileconfigurator.fragments.update.UpdateContentFragment;
import com.kotofeya.mobileconfigurator.fragments.update.UpdateOsFragment;
import com.kotofeya.mobileconfigurator.fragments.update.UpdateStmFragment;

public class FragmentHandler {

    private FragmentManager fragmentManager;
    private Fragment currentFragment;

    public final static String MAIN_FRAGMENT_TAG = "MAIN_FRAGMENT";
    final static String BASIC_SCANNER_FRAGMENT = "BASIC_SCANNER_FRAGMENT";
    final static String BLE_SCANNER_FRAGMENT = "BLE_SCANNER_FRAGMENT";
    final static String UPDATE_OS_FRAGMENT = "UPDATE_OS_FRAGMENT";
    final static String UPDATE_STM_FRAGMENT = "UPDATE_STM_FRAGMENT";
    final static String UPDATE_CONTENT_FRAGMENT = "UPDATE_CONTENT_FRAGMENT";
    final static String CONFIG_TRANSPORT_FRAGMENT = "CONFIG_TRANSPORT_FRAGMENT";
    final static String CONFIG_STATION_FRAGMENT = "CONFIG_STATION_FRAGMENT";
    final static String TRANSPORT_CONTENT_FRAGMENT = "TRANSPORT_CONTENT_FRAGMENT";
    final static String STATION_CONTENT_FRAGMENT = "STATION_CONTENT_FRAGMENT";

    public final static String CONFIRMATION_DIALOG_TAG = "CONFIRMATION_DIALOG";

    public FragmentManager getFragmentManager() {
        return fragmentManager;
    }

    public FragmentHandler(Context context){
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
                case BLE_SCANNER_FRAGMENT:
                    return new BleScannerFragment();
                case UPDATE_OS_FRAGMENT:
                    return new UpdateOsFragment();
                case UPDATE_STM_FRAGMENT:
                    return new UpdateStmFragment();
                case UPDATE_CONTENT_FRAGMENT:
                    return new UpdateContentFragment();
                case CONFIG_TRANSPORT_FRAGMENT:
                    return new ConfigTransportFragment();
                case CONFIG_STATION_FRAGMENT:
                    return new ConfigStatFragment();
                case TRANSPORT_CONTENT_FRAGMENT:
                    return new TransportContentFragment();
                case STATION_CONTENT_FRAGMENT:
                    return new StationContentFragment();
            }
        }
        return fragment;
    }

    public void changeFragmentBundle(String fragmentTag, Bundle bundle){
        Fragment fragment = getFragment(fragmentTag);
        fragment.setArguments(bundle);
        setFragment(fragment, fragmentTag, true);
    }

    public void changeFragment(String fragmentTag){
        Fragment fragment = getFragment(fragmentTag);
        setFragment(fragment, fragmentTag, true);
    }

    private void setFragment(Fragment fragment, String tag, boolean stacked){
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