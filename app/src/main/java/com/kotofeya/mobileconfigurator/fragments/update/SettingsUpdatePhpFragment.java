package com.kotofeya.mobileconfigurator.fragments.update;

import static com.kotofeya.mobileconfigurator.network.PostCommand.POST_COMMAND_ERROR;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.fragments.FragmentHandler;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.rv_adapter.RvAdapterType;
import com.kotofeya.mobileconfigurator.domain.transceiver.Transceiver;

public class SettingsUpdatePhpFragment extends UpdateFragment {
    public static final String SSH_CONN = "ssh_conn";

    @Override
    public void setMainTextLabelText() {
        viewModel.setMainTxtLabel("Update PHP");
    }
    @Override
    public RvAdapterType getAdapterType() {
        return RvAdapterType.SETTINGS_UPDATE_PHP;
    }
    @Override
    public void onStart() {
        Logger.d(Logger.STM_LOG_LOG, "onStart");
        super.onStart();
        binding.version1.setVisibility(View.GONE);
        binding.checkVersionBtn.setVisibility(View.GONE);
    }
    @Override
    public void onTaskCompleted(Bundle result) {
        String command = result.getString(BundleKeys.COMMAND_KEY);
        String response = result.getString(BundleKeys.RESPONSE_KEY);
        if(command != null) {
            switch (command) {
                case PostCommand.UPDATE_PHP:
                    if(response.startsWith("Ok")){
                        fragmentHandler.showMessage("Php version updated successfully");
                    } else {
                        fragmentHandler.showMessage("Updating Php version failed");
                    }
                    break;
                case POST_COMMAND_ERROR:
                    fragmentHandler.showMessage(response);
                    break;
            }
        }
    }

    @Override
    public void adapterItemOnClick(Transceiver transiver) {
        if (version != null && !version.startsWith(SSH_CONN)) {
            Logger.d(Logger.SCANNER_ADAPTER_LOG, "Update php was pressed");
            Bundle bundle = new Bundle();
            bundle.putString(BundleKeys.IP_KEY, transiver.getIp());
            UpdatePhpConfDialog dialog = new UpdatePhpConfDialog();
            dialog.setArguments(bundle);
            dialog.show(fragmentHandler.getFragmentManager(),
                    FragmentHandler.CONFIRMATION_DIALOG_TAG);
        } else {
            Toast.makeText(requireActivity(), "Не удается установить ssh-подключение", Toast.LENGTH_SHORT).show();
        }
    }
}
