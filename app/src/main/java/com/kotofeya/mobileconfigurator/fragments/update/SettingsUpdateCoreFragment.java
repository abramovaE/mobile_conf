package com.kotofeya.mobileconfigurator.fragments.update;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.ScannerAdapter;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;

import java.util.ArrayList;

import static com.kotofeya.mobileconfigurator.network.PostCommand.POST_COMMAND_ERROR;

public class SettingsUpdateCoreFragment extends UpdateFragment {

//    private TextView phpVersion;


    @Override
    protected void loadUpdates() {

    }

    @Override
    protected void loadVersion() {

    }

    @Override
    protected void setMainTextLabelText() {

    }

    @Override
    protected ScannerAdapter getScannerAdapter() {
        return new ScannerAdapter(context, utils, ScannerAdapter.SETTINGS_UPDATE_CORE, new ArrayList<>());
    }


    @Override
    public void onStart() {
        Logger.d(Logger.STM_LOG_LOG, "onStart");
        super.onStart();
        versionLabel.setVisibility(View.GONE);
        checkVersionButton.setVisibility(View.GONE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mainTxtLabel.setText("Update the core");
//        phpVersion = view.findViewById(R.id.text)
        return view;
    }

    @Override
    public void onTaskCompleted(Bundle result) {
        String command = result.getString(PostInfo.COMMAND);
        String ip = result.getString(PostInfo.IP);
        String response = result.getString(PostInfo.RESPONSE);
        Logger.d(Logger.UPDATE_CORE_LOG, "update core upload code: " + result);
        Logger.d(Logger.UPDATE_CORE_LOG, "command: " + command);
        Logger.d(Logger.UPDATE_CORE_LOG, "ip: " + ip);
        Logger.d(Logger.UPDATE_CORE_LOG, "response: " + response);
        progressBar.setVisibility(View.GONE);
        SshConnection connection = new SshConnection(((SettingsUpdateCoreFragment) App.get().getFragmentHandler().getCurrentFragment()));
        connection.execute(ip, SshConnection.UPDATE_CORE_UPLOAD_CODE);
    }
}
