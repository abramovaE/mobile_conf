package com.kotofeya.mobileconfigurator.fragments.update;

import android.os.Bundle;
import android.view.View;


import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;

import static com.kotofeya.mobileconfigurator.network.PostCommand.POST_COMMAND_ERROR;

public class SettingsUpdatePhpFragment extends UpdateFragment {

    @Override
    protected void setMainTextLabelText() {
        mainTxtLabel.setText("Update PHP");
    }
    @Override
    protected RvAdapterType getAdapterType() {
        return RvAdapterType.SETTINGS_UPDATE_PHP;
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
        String command = result.getString(BundleKeys.COMMAND_KEY);
        String ip = result.getString(BundleKeys.IP_KEY);
        String response = result.getString(BundleKeys.RESPONSE_KEY);
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
}
