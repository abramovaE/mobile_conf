package com.kotofeya.mobileconfigurator.fragments.update;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.RvAdapter;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;

import java.util.ArrayList;

import static com.kotofeya.mobileconfigurator.network.PostCommand.POST_COMMAND_ERROR;

public class SettingsUpdatePhpFragment extends UpdateFragment {

    @Override
    protected void loadUpdates() {
    }
    @Override
    protected void loadVersion() {
    }
    @Override
    protected void setMainTextLabelText() {
        mainTxtLabel.setText("Update PHP");
    }
    @Override
    protected int getAdapterType() {
        return RvAdapter.STM_LOG;
    }
    @Override
    public void onStart() {
        Logger.d(Logger.STM_LOG_LOG, "onStart");
        super.onStart();
        versionLabel.setVisibility(View.GONE);
        checkVersionButton.setVisibility(View.GONE);
    }
    @Override
    public void onTaskCompleted(Bundle result) {
        String command = result.getString(PostInfo.COMMAND);
        String ip = result.getString(PostInfo.IP);
        String response = result.getString(PostInfo.RESPONSE);
        Logger.d(Logger.TRANSIVER_STM_LOG_LOG, "on task completed, result: " + result);
        Logger.d(Logger.TRANSIVER_STM_LOG_LOG, "command: " + command);
        Logger.d(Logger.TRANSIVER_STM_LOG_LOG, "ip: " + ip);
        Logger.d(Logger.TRANSIVER_STM_LOG_LOG, "response: " + response);
        if(command != null) {
            switch (command) {
                case PostCommand.UPDATE_PHP:
                    if(response.startsWith("Ok")){
                        utils.showMessage("Php version updated successfully");
                    } else {
                        utils.showMessage("Updating Php version failed");
                    }
                    break;
                case POST_COMMAND_ERROR:
                    utils.showMessage(response);
                    break;
            }
        }
    }

    @Override
    public void setProgressBarVisible() {
    }
    @Override
    public void setProgressBarGone() {

    }
}
