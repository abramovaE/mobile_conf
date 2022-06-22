package com.kotofeya.mobileconfigurator.fragments.transiver_settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.activities.CustomViewModel;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.databinding.TransiverSettingsFragmentBinding;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.List;

public abstract class TransceiverSettingsFragment extends Fragment
        implements PostCommand, OnTaskCompleted {

    private static final String TAG = TransceiverSettingsFragment.class.getSimpleName();
    protected FragmentHandler fragmentHandler;
    protected TransiverSettingsFragmentBinding binding;
    protected CustomViewModel viewModel;
    protected String ssid;

    private void updateUIButtons(List<Transiver> transceivers) {
        String version = CustomViewModel.getVersion(ssid);
        if(version != null && !version.equals("ssh_conn")){
            String ip = viewModel.getIp(ssid);
            if(ip != null){
                updateButtonsState();
            }
        }
    }

    protected abstract void updateButtonsState();
    protected abstract String getShowSettingsCommand();
    protected abstract String getDefaultSettingsCommand();
    protected abstract DialogFragment getDialogSettings();
    protected abstract String getAddSettingsCommand();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Logger.d(TAG, new Throwable().getStackTrace()[0].getMethodName());
        this.ssid = getArguments() != null ? getArguments().getString("ssid") : null;

        binding = TransiverSettingsFragmentBinding.inflate(inflater, container, false);
        viewModel = ViewModelProviders.of(requireActivity(),
                new CustomViewModel.ModelFactory()).get(CustomViewModel.class);
        viewModel.setMainBtnRescanVisibility(View.GONE);
        viewModel.getTransivers().observe(getViewLifecycleOwner(), this::updateUIButtons);
        viewModel.transceiverSettingsText().observe(getViewLifecycleOwner(), this::updateUI);

        fragmentHandler = ((MainActivity) requireActivity()).getFragmentHandler();


        binding.showSettingsBtn.setOnClickListener( v-> {
            String ip = viewModel.getIp(ssid);
            Thread thread = new Thread(new PostInfo(this, ip, getShowSettingsCommand()));
            thread.start();
        });
        binding.setDefaultSettings.setOnClickListener(v -> {
            String ip = viewModel.getIp(ssid);
            Thread thread = new Thread(new PostInfo(this, ip, getDefaultSettingsCommand()));
            thread.start();

        });
        binding.addNewSettings.setOnClickListener( v-> {String ip = viewModel.getIp(ssid);
            Bundle bundle = new Bundle();
            bundle.putString("ip", ip);
            DialogFragment dialog = getDialogSettings();
            dialog.setArguments(bundle);
            dialog.show(fragmentHandler.getFragmentManager(), getAddSettingsCommand());});
        return binding.getRoot();
    }

//    @Override
//    public void onStart() {
//        Logger.d(TAG, new Throwable().getStackTrace()[0].getMethodName());
//        super.onStart();
//        this.ssid = getArguments() != null ? getArguments().getString("ssid") : null;
//    }

    private void updateUI(String text) {
        binding.settingsTv.setText(text);
    }

    public void onTaskCompleted(Bundle result) {
        String command = result.getString(BundleKeys.COMMAND_KEY);
        String response = result.getString(BundleKeys.RESPONSE_KEY);
        if(command != null) {
            switch (command) {
                case PostCommand.SCUART:
                case PostCommand.READ_NETWORK:
                case PostCommand.READ_WPA:
                    response = response.isEmpty() ? "Empty response" : response;
                    viewModel.setTransceiverSettingsText(response);
                    break;

                case PostCommand.NETWORK_CLEAR:
                case PostCommand.WIFI_CLEAR:
                    fragmentHandler.showMessage((response.startsWith("Ok")) ?
                            "Настройки сброшены и примутся при перезапуске." : "Error");
                    break;

                case PostCommand.STATIC:
                case PostCommand.WIFI:
                    fragmentHandler.showMessage((response.startsWith("Ok")) ?
                            "Новые параметры заданы и примутся при перезапуске." : "Error");
                    break;

                case POST_COMMAND_ERROR:
                    fragmentHandler.showMessage(response);
                    break;
            }
        }
    }
}