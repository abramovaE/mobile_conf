package com.kotofeya.mobileconfigurator.fragments.update;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.activities.CustomViewModel;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.List;

public class TransiverSettingsScUartFragment extends Fragment implements PostCommand, OnTaskCompleted {


    private final Handler myHandler = new Handler();
    private String text;
    public Context context;
    public Utils utils;
    protected CustomViewModel viewModel;
    protected String ssid;

    private TextView scuart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.transiver_settings_scuart_fragment, container, false);

        this.ssid = getArguments().getString("ssid");

        TextView mainTxtLabel = ((MainActivity)context).findViewById(R.id.main_txt_label);
        ImageButton mainBtnRescan = ((MainActivity)context).findViewById(R.id.main_btn_rescan);
        mainTxtLabel.setText("ScUart: " + ssid);
        mainBtnRescan.setVisibility(View.GONE);

        scuart = view.findViewById(R.id.settings_scuart);

        Thread thread = new Thread(new PostInfo((TransiverSettingsScUartFragment) App.get().getFragmentHandler().getCurrentFragment(), utils.getIp(ssid),
                PostCommand.SCUART));
        thread.start();

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Logger.d(Logger.CONTENT_LOG, "on view created");
        super.onViewCreated(view, savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity(), new CustomViewModel.ModelFactory()).get(CustomViewModel.class);
        viewModel.getTransivers().observe(getViewLifecycleOwner(), this::updateUI);
    }


    private void updateUI(List<Transiver> transivers){
        updateBtnsState();
    }

    private void updateBtnsState() {
        if(utils.getVersion(ssid) != null && !utils.getVersion(ssid).equals("ssh_conn")){
            if(utils.getIp(ssid) != null){

            }
        }
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
                case PostCommand.SCUART:
                    if(response.isEmpty()){
                        response = "Empty response";
                    }
                    updateLogText(response);
                    break;
            }
        }
    }

    protected void updateUI() {
        scuart.setText(text);
    }

    final Runnable updateRunnable = new Runnable() {
        public void run() {
            updateUI();
        }
    };

    public void updateLogText(String text){
        Logger.d(Logger.MAIN_LOG, "update log text: " + text);
        this.text = text;
        myHandler.post(updateRunnable);
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        this.utils = ((MainActivity) context).getUtils();
        super.onAttach(context);
    }

    @Override
    public void onProgressUpdate(Integer downloaded) {
    }

    @Override
    public void setProgressBarVisible() {

    }

    @Override
    public void setProgressBarGone() {

    }

    @Override
    public void clearProgressBar() {

    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.d(Logger.CONTENT_LOG, "contentFragment onStart");
        this.ssid = getArguments().getString("ssid");
    }
}