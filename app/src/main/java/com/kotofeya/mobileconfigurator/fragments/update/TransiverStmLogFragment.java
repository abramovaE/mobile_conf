package com.kotofeya.mobileconfigurator.fragments.update;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class TransiverStmLogFragment extends Fragment implements View.OnClickListener, PostCommand, OnTaskCompleted {

//    private Transiver transiver;
    private Button showStmLog;
    private Button clearStmLog;
    private TextView stmLog;
    private final Handler myHandler = new Handler();
    private String text;
    private static final String LOG_IS_EMPTY = "Stm log is empty";
    public Context context;
    public Utils utils;
    protected CustomViewModel viewModel;
    protected String ssid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.transiver_stm_log_fragment, container, false);
        showStmLog = view.findViewById(R.id.show_stm_log);
        clearStmLog = view.findViewById(R.id.clear_stm_log);
        showStmLog.setEnabled(false);
        clearStmLog.setEnabled(false);
        showStmLog.setOnClickListener(this);
        clearStmLog.setOnClickListener(this);
        stmLog = view.findViewById(R.id.stm_log);
        this.ssid = getArguments().getString("ssid");

        TextView mainTxtLabel = ((MainActivity)context).findViewById(R.id.main_txt_label);
        ImageButton mainBtnRescan = ((MainActivity)context).findViewById(R.id.main_btn_rescan);
        mainTxtLabel.setText("Stm log: " + ssid);
        mainBtnRescan.setVisibility(View.GONE);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Logger.d(Logger.CONTENT_LOG, "on view created");
        super.onViewCreated(view, savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity(), new CustomViewModel.ModelFactory()).get(CustomViewModel.class);
        viewModel.getTransivers().observe(getViewLifecycleOwner(), this::updateUI);
//        transiver = viewModel.getTransiverBySsid(ssid);



    }


    private void updateUI(List<Transiver> transivers){
        updateBtnsState();
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
        String command = result.getString(PostInfo.COMMAND);
        String ip = result.getString(PostInfo.IP);
        String response = result.getString(PostInfo.RESPONSE);
        Logger.d(Logger.TRANSIVER_STM_LOG_LOG, "on task completed, result: " + result);
        Logger.d(Logger.TRANSIVER_STM_LOG_LOG, "command: " + command);
        Logger.d(Logger.TRANSIVER_STM_LOG_LOG, "ip: " + ip);
        Logger.d(Logger.TRANSIVER_STM_LOG_LOG, "response: " + response);
        if(command != null) {
            switch (command) {
                case PostCommand.STM_UPDATE_LOG:
                    if(response.isEmpty()){
                        response = LOG_IS_EMPTY;
                    }
                    updateStmLogText(response);
                    break;
                case PostCommand.STM_UPDATE_LOG_CLEAR:
                    utils.showMessage("Stm log file was cleared");
                    updateStmLogText(LOG_IS_EMPTY);
                    break;
            }
        }
    }

    protected void updateUI() {
        stmLog.setText(text);
    }

    final Runnable updateRunnable = new Runnable() {
        public void run() {
            updateUI();
        }
    };

    public void updateStmLogText(String text){
        this.text = text;
        myHandler.post(updateRunnable);
    }

//    private void stopScan(){}



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
    public void onStart() {
        super.onStart();
        Logger.d(Logger.CONTENT_LOG, "contentFragment onStart");
        this.ssid = getArguments().getString("ssid");
//        transiver = viewModel.getTransiverBySsid(ssid);
//        stopScan();
    }
}


