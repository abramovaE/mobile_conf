package com.kotofeya.mobileconfigurator;


import android.content.Context;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class FragmentHandler {

    private FragmentManager fragmentManager;
    private Fragment currentFragment;
    private View containerView;

    final static String MAIN_FRAGMENT_TAG = "MAIN_FRAGMENT";
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
        fragmentManager = ((MainMenu) context).getSupportFragmentManager();
    }


    public void changeFragment(String fragmentTag){
        Fragment fragment = fragmentManager.findFragmentByTag(fragmentTag);

        if(fragment == null){
         switch (fragmentTag){
             case MAIN_FRAGMENT_TAG:
                 fragment = new MainMenuFragment();
                 break;

             case BASIC_SCANNER_FRAGMENT:
                 fragment = new BasicScannerFragment();
                 break;

             case BLE_SCANNER_FRAGMENT:
                 fragment = new BleScannerFragment();
                 break;

             case UPDATE_OS_FRAGMENT:
                 fragment = new UpdateOsFragment();
                 break;

             case UPDATE_STM_FRAGMENT:
                 fragment = new UpdateStmFragment();
                 break;

             case UPDATE_CONTENT_FRAGMENT:
                 fragment = new UpdateContentFragment();
                 break;

             case CONFIG_TRANSPORT_FRAGMENT:
                 fragment = new ConfigTransportFragment();
                 break;

             case CONFIG_STATION_FRAGMENT:
                 fragment = new ConfigStatFragment();
                 break;

             case TRANSPORT_CONTENT_FRAGMENT:
                 fragment = new TransportContentFragment();
                 break;

             case STATION_CONTENT_FRAGMENT:
                 fragment = new StationContentFragment();
                 break;
         }
        }
        setFragment(fragment, fragmentTag, true);

//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.replace(R.id.fragment_container, fragment, fragmentTag);
//        fragmentTransaction.commit();
    }

    public void addMainFragment(){

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, new MainMenuFragment(), MAIN_FRAGMENT_TAG);
        fragmentTransaction.commit();
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
