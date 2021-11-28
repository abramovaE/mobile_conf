package com.kotofeya.mobileconfigurator;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.user.UserFactory;
import com.kotofeya.mobileconfigurator.user.UserInterface;

import java.util.List;

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
    private Button scUartBtn;
    private Button updatePhp;
    private Button updateCore;

    private TextView mainTvScanner;
    private TextView mainTvUpdate;
    private TextView mainTvConfig;
    private TextView mainTvSettings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);
        List<UserInterface> interfaces = UserFactory.getUser().getInterfaces();


        basicScannerbtn = view.findViewById(R.id.main_basic_scanner_btn);
        bleScannerBtn = view.findViewById(R.id.main_ble_scanner_btn);
        updateOSBtn = view.findViewById(R.id.main_update_os_btn);
        updateStmBtn = view.findViewById(R.id.main_update_stm_btn);
        updateContentBtn = view.findViewById(R.id.main_update_content_btn);
        configTransportBtn = view.findViewById(R.id.main_config_transport_btn);
        configStationBtn = view.findViewById(R.id.main_config_station_btn);
        settingsBtn = view.findViewById(R.id.main_settings_btn);
        stmLogBtn = view.findViewById(R.id.main_stm_log_btn);
        settingsWifiBtn = view.findViewById(R.id.main_settings_wifi_btn);
        settingsNetworkBtn = view.findViewById(R.id.main_settings_network_btn);
        scUartBtn = view.findViewById(R.id.main_settings_scuart_btn);
        updatePhp = view.findViewById(R.id.main_settings_update_php_btn);
        updateCore = view.findViewById(R.id.main_settings_update_core_btn);

        mainTvScanner = view.findViewById(R.id.main_tv_scanner);
        mainTvUpdate = view.findViewById(R.id.main_tv_update);
        mainTvConfig = view.findViewById(R.id.main_tv_config);
        mainTvSettings = view.findViewById(R.id.main_tv_settings);

        if(interfaces.contains(UserInterface.WIFI_SCANNER)){
            basicScannerbtn.setOnClickListener(this);
            basicScannerbtn.setVisibility(View.VISIBLE);
            mainTvScanner.setVisibility(View.VISIBLE);
        }
        if(interfaces.contains(UserInterface.BLE_SCANNER)){
            bleScannerBtn.setOnClickListener(this);
            bleScannerBtn.setVisibility(View.VISIBLE);
            mainTvScanner.setVisibility(View.VISIBLE);
        }
        if(interfaces.contains(UserInterface.UPDATE_OS)){
            updateOSBtn.setOnClickListener(this);
            updateOSBtn.setVisibility(View.VISIBLE);
            mainTvUpdate.setVisibility(View.VISIBLE);
        }

        if(interfaces.contains(UserInterface.UPDATE_CONTENT)){
            updateContentBtn.setOnClickListener(this);
            updateContentBtn.setVisibility(View.VISIBLE);
            mainTvUpdate.setVisibility(View.VISIBLE);
        }
        if(interfaces.contains(UserInterface.UPDATE_STM_LOG)){
            stmLogBtn.setOnClickListener(this);
            stmLogBtn.setVisibility(View.VISIBLE);
            mainTvUpdate.setVisibility(View.VISIBLE);
        }
        if(interfaces.contains(UserInterface.CONF_TRANSPORT)){
            configTransportBtn.setOnClickListener(this);
            configTransportBtn.setVisibility(View.VISIBLE);
            mainTvConfig.setVisibility(View.VISIBLE);
        }
        if(interfaces.contains(UserInterface.CONF_STATIONARY)){
            configStationBtn.setOnClickListener(this);
            configStationBtn.setVisibility(View.VISIBLE);
            mainTvConfig.setVisibility(View.VISIBLE);
        }
        if(interfaces.contains(UserInterface.SETTINGS_WIFI)){
            settingsWifiBtn.setOnClickListener(this);
            settingsWifiBtn.setVisibility(View.VISIBLE);
            mainTvSettings.setVisibility(View.VISIBLE);
        }
        if(interfaces.contains(UserInterface.SETTINGS_NETWORK)){
            settingsNetworkBtn.setOnClickListener(this);
            settingsNetworkBtn.setVisibility(View.VISIBLE);
            mainTvSettings.setVisibility(View.VISIBLE);
        }
        if(interfaces.contains(UserInterface.SETTINGS_SCUART)){
            scUartBtn.setOnClickListener(this);
            scUartBtn.setVisibility(View.VISIBLE);
            mainTvSettings.setVisibility(View.VISIBLE);
        }
        if(interfaces.contains(UserInterface.SETTINGS_UPDATE_PHP)){
            updatePhp.setOnClickListener(this);
            updatePhp.setVisibility(View.VISIBLE);
            mainTvSettings.setVisibility(View.VISIBLE);
        }
        if(interfaces.contains(UserInterface.SETTINGS_UPDATE_CORE)){
            updateCore.setOnClickListener(this);
            updateCore.setVisibility(View.VISIBLE);
            mainTvSettings.setVisibility(View.VISIBLE);
        }
        if(interfaces.contains(UserInterface.APP_SETTINGS)){
            settingsBtn.setOnClickListener(this);
            settingsBtn.setVisibility(View.VISIBLE);
        }

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
            case R.id.main_settings_scuart_btn:
                Logger.d(Logger.MAIN_LOG, "main settings scuart was pressed");
                App.get().getFragmentHandler().changeFragment(FragmentHandler.SETTINGS_SCUART_FRAGMENT);
                break;
            case R.id.main_settings_update_php_btn:
                Logger.d(Logger.MAIN_LOG, "main settings update php was pressed");
                App.get().getFragmentHandler().changeFragment(FragmentHandler.SETTINGS_UPDATE_PHP_FRAGMENT);
                break;
            case R.id.main_settings_update_core_btn:
                Logger.d(Logger.MAIN_LOG, "main settings update core was pressed");
                App.get().getFragmentHandler().changeFragment(FragmentHandler.SETTINGS_UPDATE_CORE_FRAGMENT);
                break;
        }
    }
}
