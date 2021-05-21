package com.kotofeya.mobileconfigurator;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class MainMenuFragment extends Fragment implements View.OnClickListener {
    private Button basicScannerbtn;
    private Button bleScannerBtn;
    private Button updateOSBtn;
    private Button updateStmBtn;
    private Button updateContentBtn;
    private Button configTransportBtn;
    private Button configStationBtn;
    private Button settingsBtn;
    private Button stmLogBtn;
    private Button settingsWifiBtn;
    private Button settingsNetworkBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);
        basicScannerbtn = view.findViewById(R.id.main_basic_scanner_btn);
        basicScannerbtn.setOnClickListener(this);
        bleScannerBtn = view.findViewById(R.id.main_ble_scanner_btn);
        bleScannerBtn.setOnClickListener(this);
        updateOSBtn = view.findViewById(R.id.main_update_os_btn);
        updateOSBtn.setOnClickListener(this);
        updateStmBtn = view.findViewById(R.id.main_update_stm_btn);
        updateStmBtn.setOnClickListener(this);
        updateContentBtn = view.findViewById(R.id.main_update_content_btn);
        updateContentBtn.setOnClickListener(this);
        configTransportBtn = view.findViewById(R.id.main_config_transport_btn);
        configTransportBtn.setOnClickListener(this);
        configStationBtn = view.findViewById(R.id.main_config_station_btn);
        configStationBtn.setOnClickListener(this);
        settingsBtn = view.findViewById(R.id.main_settings_btn);
        settingsBtn.setOnClickListener(this);
        stmLogBtn = view.findViewById(R.id.main_stm_log_btn);
        stmLogBtn.setOnClickListener(this);
        settingsWifiBtn = view.findViewById(R.id.main_settings_wifi_btn);
        settingsWifiBtn.setOnClickListener(this);
        settingsNetworkBtn = view.findViewById(R.id.main_settings_network_btn);
        settingsNetworkBtn.setOnClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.main_basic_scanner_btn:
                Logger.d(Logger.MAIN_LOG, "wifi scanner was pressed");
                App.get().getFragmentHandler().changeFragment(FragmentHandler.BASIC_SCANNER_FRAGMENT);
                break;
            case R.id.main_ble_scanner_btn:
                Logger.d(Logger.MAIN_LOG, "bluetooth scanner was pressed");
                App.get().getFragmentHandler().changeFragment(FragmentHandler.BLE_SCANNER_FRAGMENT);
                break;
            case R.id.main_update_os_btn:
                Logger.d(Logger.MAIN_LOG, "update os was pressed");
                App.get().getFragmentHandler().changeFragment(FragmentHandler.UPDATE_OS_FRAGMENT);
                break;
            case R.id.main_update_stm_btn:
                Logger.d(Logger.MAIN_LOG, "update stm was pressed");
                App.get().getFragmentHandler().changeFragment(FragmentHandler.UPDATE_STM_FRAGMENT);
                break;
            case R.id.main_update_content_btn:
                Logger.d(Logger.MAIN_LOG, "update content was pressed");
                App.get().getFragmentHandler().changeFragment(FragmentHandler.UPDATE_CONTENT_FRAGMENT);
                break;
            case R.id.main_config_transport_btn:
                Logger.d(Logger.MAIN_LOG, "config transport was pressed");
                App.get().getFragmentHandler().changeFragment(FragmentHandler.CONFIG_TRANSPORT_FRAGMENT);
                break;
            case R.id.main_config_station_btn:
                Logger.d(Logger.MAIN_LOG, "config station was pressed");
                App.get().getFragmentHandler().changeFragment(FragmentHandler.CONFIG_STATION_FRAGMENT);
                break;
            case R.id.main_settings_btn:
                Logger.d(Logger.MAIN_LOG, "settings was pressed");
                App.get().getFragmentHandler().changeFragment(FragmentHandler.SETTINGS_FRAGMENT);
                break;
            case R.id.main_stm_log_btn:
                Logger.d(Logger.MAIN_LOG, "stm log was pressed");
                App.get().getFragmentHandler().changeFragment(FragmentHandler.STM_LOG_FRAGMENT);
                break;
            case R.id.main_settings_wifi_btn:
                Logger.d(Logger.MAIN_LOG, "main settings wifi was pressed");
                App.get().getFragmentHandler().changeFragment(FragmentHandler.SETTINGS_WIFI_FRAGMENT);
                break;
            case R.id.main_settings_network_btn:
                Logger.d(Logger.MAIN_LOG, "main settings network was pressed");
                App.get().getFragmentHandler().changeFragment(FragmentHandler.SETTINGS_NETWORK_FRAGMENT);
                break;
        }
    }
}
