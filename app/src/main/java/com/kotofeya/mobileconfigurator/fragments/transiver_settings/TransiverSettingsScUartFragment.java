package com.kotofeya.mobileconfigurator.fragments.transiver_settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.List;

public class TransiverSettingsScUartFragment extends TransiverSettingsFragment {
    private TextView scuart;

    @Override
    public View getView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.transiver_settings_scuart_fragment, container, false);
    }

    @Override
    public void updateUIBtns(List<Transiver> transivers) {
        updateBtnsState();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mainTxtLabel.setText("ScUart: " + ssid);
        scuart = view.findViewById(R.id.settings_scuart);
        Thread thread = new Thread(new PostInfo((TransiverSettingsScUartFragment)
                App.get().getFragmentHandler().getCurrentFragment(), utils.getIp(ssid),
                PostCommand.SCUART));
        thread.start();
        return view;
    }

    private void updateBtnsState() {
        if(utils.getVersion(ssid) != null && !utils.getVersion(ssid).equals("ssh_conn")){
            if(utils.getIp(ssid) != null){
            }
        }
    }

    @Override
    public void onTaskCompleted(Bundle result) {
        String command = result.getString(BundleKeys.COMMAND_KEY);
        String response = result.getString(BundleKeys.RESPONSE_KEY);
        if(command != null) {
            switch (command) {
                case PostCommand.SCUART:
                    if(response.isEmpty()){
                        response = "Empty response";
                    }
                    updateText(response);
                    break;
            }
        }
    }

    public void updateUI() {
        scuart.setText(text);
    }

    @Override
    public void onClick(View v) {

    }
}