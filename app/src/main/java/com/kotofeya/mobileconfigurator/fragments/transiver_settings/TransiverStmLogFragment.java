package com.kotofeya.mobileconfigurator.fragments.transiver_settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class TransiverStmLogFragment extends TransiverSettingsFragment {

    private Button showStmLog;
    private Button clearStmLog;
    private TextView stmLog;
    private static final String LOG_IS_EMPTY = "Stm log is empty";

    @Override
    public View getView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.transiver_stm_log_fragment, container, false);
    }

    @Override
    public void updateUIBtns(List<Transiver> transivers) {
        updateBtnsState();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mainTxtLabel.setText("Stm log: " + ssid);
        showStmLog = view.findViewById(R.id.show_stm_log);
        clearStmLog = view.findViewById(R.id.clear_stm_log);
        showStmLog.setEnabled(false);
        clearStmLog.setEnabled(false);
        showStmLog.setOnClickListener(this);
        clearStmLog.setOnClickListener(this);
        stmLog = view.findViewById(R.id.stm_log);
        return view;
    }

    private void updateBtnsState() {
        if(utils.getVersion(ssid) != null && !utils.getVersion(ssid).equals("ssh_conn")){
            if(utils.getIp(ssid) != null){
                showStmLog.setEnabled(true);
                clearStmLog.setEnabled(true);
            }
        }
    }

    @Override
    public void onClick(View v) {
        String ip = utils.getIp(ssid);
        Thread thread;
        switch (v.getId()){
            case R.id.show_stm_log:
                thread = new Thread(new PostInfo((TransiverStmLogFragment) App.get().getFragmentHandler().getCurrentFragment(), ip,
                        PostCommand.STM_UPDATE_LOG));
                thread.start();
                break;
            case R.id.clear_stm_log:
                thread = new Thread(new PostInfo((TransiverStmLogFragment) App.get().getFragmentHandler().getCurrentFragment(), ip,
                        PostCommand.STM_UPDATE_LOG_CLEAR));
                thread.start();
                break;
        }
    }

    @Override
    public void onTaskCompleted(Bundle result) {
        String command = result.getString(BundleKeys.COMMAND_KEY);
        String response = result.getString(BundleKeys.RESPONSE_KEY);
        if(command != null) {
            switch (command) {
                case PostCommand.STM_UPDATE_LOG:
                    if(response.isEmpty()){
                        response = LOG_IS_EMPTY;
                    }
                    updateText(response);
                    break;
                case PostCommand.STM_UPDATE_LOG_CLEAR:
                    utils.showMessage("Stm log file was cleared");
                    updateText(LOG_IS_EMPTY);
                    break;
            }
        }
    }

    public void updateUI() {
        stmLog.setText(text);
    }

}