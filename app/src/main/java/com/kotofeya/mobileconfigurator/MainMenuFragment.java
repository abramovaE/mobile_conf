package com.kotofeya.mobileconfigurator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.databinding.MainFragmentBinding;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.user.UserFactory;
import com.kotofeya.mobileconfigurator.user.UserInterface;

import java.util.List;

public class MainMenuFragment extends Fragment {

    private static final String TAG = MainMenuFragment.class.getSimpleName();
    private FragmentHandler fragmentHandler;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        MainFragmentBinding binding = MainFragmentBinding.inflate(inflater, container, false);
        List<UserInterface> interfaces = UserFactory.getUser().getInterfaces();
        Logger.d(TAG, "mainMenuIntf(): " + interfaces);
        fragmentHandler = ((MainActivity)requireActivity()).getFragmentHandler();

        if(interfaces.contains(UserInterface.WIFI_SCANNER)){
            binding.mainBasicScannerBtn.setOnClickListener(v -> {
                Logger.d(Logger.MAIN_LOG, "wifi scanner was pressed");
                fragmentHandler.changeFragment(FragmentHandler.BASIC_SCANNER_FRAGMENT);
            });
            binding.mainBasicScannerBtn.setVisibility(View.VISIBLE);
            binding.mainTvScanner.setVisibility(View.VISIBLE);
        }

        if(interfaces.contains(UserInterface.UPDATE_OS)){
            binding.mainUpdateOsBtn.setOnClickListener(v->{
                Logger.d(Logger.MAIN_LOG, "update os was pressed");
                fragmentHandler.changeFragment(FragmentHandler.UPDATE_OS_FRAGMENT);
            });
            binding.mainUpdateOsBtn.setVisibility(View.VISIBLE);
            binding.mainTvUpdate.setVisibility(View.VISIBLE);
        }

        if(interfaces.contains(UserInterface.UPDATE_CONTENT)){
            binding.mainUpdateContentBtn.setOnClickListener(v->{
                Logger.d(Logger.MAIN_LOG, "update content was pressed");
                fragmentHandler.changeFragment(FragmentHandler.UPDATE_CONTENT_FRAGMENT);
            });
            binding.mainUpdateContentBtn.setVisibility(View.VISIBLE);
            binding.mainTvUpdate.setVisibility(View.VISIBLE);
        }
        if(interfaces.contains(UserInterface.UPDATE_STM_LOG)){
            binding.mainStmLogBtn.setOnClickListener(v->{
                Logger.d(Logger.MAIN_LOG, "update stm was pressed");
                fragmentHandler.changeFragment(FragmentHandler.STM_LOG_FRAGMENT);
            });
            binding.mainStmLogBtn.setVisibility(View.VISIBLE);
            binding.mainTvUpdate.setVisibility(View.VISIBLE);
        }
        if(interfaces.contains(UserInterface.CONF_TRANSPORT)){
            binding.mainConfigTransportBtn.setOnClickListener(v->{
                Logger.d(Logger.MAIN_LOG, "config transport was pressed");
                fragmentHandler.changeFragment(FragmentHandler.CONFIG_TRANSPORT_FRAGMENT);
            });
            binding.mainConfigTransportBtn.setVisibility(View.VISIBLE);
            binding.mainTvConfig.setVisibility(View.VISIBLE);
        }
        if(interfaces.contains(UserInterface.CONF_STATIONARY)){
            binding.mainConfigStationBtn.setOnClickListener(v->{
                Logger.d(Logger.MAIN_LOG, "config station was pressed");
                fragmentHandler.changeFragment(FragmentHandler.CONFIG_STATION_FRAGMENT);
            });
            binding.mainConfigStationBtn.setVisibility(View.VISIBLE);
            binding.mainTvConfig.setVisibility(View.VISIBLE);
        }
        if(interfaces.contains(UserInterface.SETTINGS_WIFI)){
            binding.mainSettingsWifiBtn.setOnClickListener(v->{
                Logger.d(Logger.MAIN_LOG, "main settings wifi was pressed");
                fragmentHandler.changeFragment(FragmentHandler.SETTINGS_WIFI_FRAGMENT);
            });
            binding.mainSettingsWifiBtn.setVisibility(View.VISIBLE);
            binding.mainTvSettings.setVisibility(View.VISIBLE);
        }
        if(interfaces.contains(UserInterface.SETTINGS_NETWORK)){
            binding.mainSettingsNetworkBtn.setOnClickListener(v->{
                Logger.d(Logger.MAIN_LOG, "main settings network was pressed");
                fragmentHandler.changeFragment(FragmentHandler.SETTINGS_NETWORK_FRAGMENT);
            });
            binding.mainSettingsNetworkBtn.setVisibility(View.VISIBLE);
            binding.mainTvSettings.setVisibility(View.VISIBLE);
        }
        if(interfaces.contains(UserInterface.SETTINGS_SCUART)){
            binding.mainSettingsScuartBtn.setOnClickListener(v->{
                Logger.d(Logger.MAIN_LOG, "main settings SCUart was pressed");
                fragmentHandler.changeFragment(FragmentHandler.SETTINGS_SCUART_FRAGMENT);
            });
            binding.mainSettingsScuartBtn.setVisibility(View.VISIBLE);
            binding.mainTvSettings.setVisibility(View.VISIBLE);
        }
        if(interfaces.contains(UserInterface.SETTINGS_UPDATE_PHP)){
            binding.mainSettingsUpdatePhpBtn.setOnClickListener(v->{
                Logger.d(Logger.MAIN_LOG, "main settings update php was pressed");
                fragmentHandler.changeFragment(FragmentHandler.SETTINGS_UPDATE_PHP_FRAGMENT);
            });
            binding.mainSettingsUpdatePhpBtn.setVisibility(View.VISIBLE);
            binding.mainTvSettings.setVisibility(View.VISIBLE);
        }
        if(interfaces.contains(UserInterface.SETTINGS_UPDATE_CORE)){
            binding.mainSettingsUpdateCoreBtn.setOnClickListener(v->{
                Logger.d(Logger.MAIN_LOG, "main settings update core was pressed");
                fragmentHandler.changeFragment(FragmentHandler.SETTINGS_UPDATE_CORE_FRAGMENT);
            });
            binding.mainSettingsUpdateCoreBtn.setVisibility(View.VISIBLE);
            binding.mainTvSettings.setVisibility(View.VISIBLE);
        }
        if(interfaces.contains(UserInterface.APP_SETTINGS)){
            binding.mainSettingsBtn.setOnClickListener(v->{
                Logger.d(Logger.MAIN_LOG, "settings was pressed");
                fragmentHandler.changeFragment(FragmentHandler.SETTINGS_FRAGMENT);
            });
            binding.mainSettingsBtn.setVisibility(View.VISIBLE);
        }

        return binding.getRoot();
    }
}
