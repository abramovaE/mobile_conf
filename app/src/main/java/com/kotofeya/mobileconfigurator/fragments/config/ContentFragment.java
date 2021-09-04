package com.kotofeya.mobileconfigurator.fragments.config;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.BundleKeys;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.activities.CustomViewModel;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.List;

public abstract class ContentFragment extends Fragment implements OnTaskCompleted, PostCommand {
    public Context context;
    public Utils utils;
    public ImageButton mainBtnRescan;
    private final Handler myHandler = new Handler();
    protected CustomViewModel viewModel;
    protected String ssid;
    protected View.OnKeyListener onKeyListener;
    protected AdapterView.OnItemSelectedListener onItemSelectedListener;
    protected TextWatcher textWatcher;
    TextView mainTxtLabel;

    protected Button btnContntSend;

    Button btnRebootRasp;
    Button btnRebootStm;
    Button btnRebootAll;
    Button btnClearRasp;
    Button spoilerBtn;

    protected Transiver currentTransiver;

    ContentClickListener contentClickListener;

    public static final String REBOOT_TYPE="rebootType";
    public static final String REBOOT_RASP="rasp";
    public static final String REBOOT_STM="stm";
    public static final String REBOOT_ALL="all";

    public static final String REBOOT_CLEAR="clear";



    protected void updateUI() {
        Logger.d(Logger.CONTENT_LOG, "update ui, ssid " + ssid + " " + currentTransiver.getSsid());
        if(utils.getVersion(ssid) != null) {
            btnRebootRasp.setEnabled(true);
            btnRebootStm.setEnabled(true);
            btnClearRasp.setEnabled(true);
            btnContntSend.setEnabled(true);
            if(!utils.getVersion(ssid).equals("ssh_conn")){
                btnRebootAll.setVisibility(View.VISIBLE);
                btnRebootAll.setEnabled(true);
            }
        }
        updateFields();
    }

    final Runnable updateRunnable = new Runnable() {
        public void run() {
            updateUI();
        }
    };




    @Override
    public void onAttach(Context context) {
        this.context = context;
        this.utils = ((MainActivity) context).getUtils();
        super.onAttach(context);

        onKeyListener = new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) App.get().getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(v.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    return true;
                }
                return false;
            }
        };

        onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateBtnCotentSendState();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateBtnCotentSendState();
            }
        };

        textWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                updateBtnCotentSendState();
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_fragment, container, false);
        mainBtnRescan = ((MainActivity)context).findViewById(R.id.main_btn_rescan);
        mainTxtLabel = ((MainActivity)context).findViewById(R.id.main_txt_label);
        btnRebootRasp = view.findViewById(R.id.content_btn_rasp);
        btnRebootStm = view.findViewById(R.id.content_btn_stm);
        btnContntSend = view.findViewById(R.id.content_btn_send);
        btnClearRasp = view.findViewById(R.id.content_btn_clear);
        btnRebootAll = view.findViewById(R.id.content_btn_all);
        this.ssid = getArguments().getString("ssid");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Logger.d(Logger.CONTENT_LOG, "on view created");
        super.onViewCreated(view, savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity(), new CustomViewModel.ModelFactory()).get(CustomViewModel.class);
        viewModel.getTransivers().observe(getViewLifecycleOwner(), this::updateUI);

        currentTransiver = viewModel.getTransiverBySsid(ssid);

        contentClickListener = new ContentClickListener(currentTransiver, utils);
        btnRebootRasp.setOnClickListener(contentClickListener);
        btnRebootStm.setOnClickListener(contentClickListener);
        btnClearRasp.setOnClickListener(contentClickListener);
        btnRebootAll.setOnClickListener(contentClickListener);
        spoilerBtn = view.findViewById(R.id.spoilerBtn);
        spoilerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.spoilerBtn) {
                    TextView contentLabel = view.findViewById(R.id.content_label);
                    LinearLayout rebootLl = view.findViewById(R.id.reboot_ll);
                    contentLabel.setVisibility(contentLabel.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                    rebootLl.setVisibility(rebootLl.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                    btnClearRasp.setVisibility(btnClearRasp.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                }
            }
        });
        viewModel.getTransivers().observe(getViewLifecycleOwner(), this::updateInformers);

        setFields();


    }


    protected abstract void setFields();

    private void updateInformers(List<Transiver> transivers){
        Logger.d(Logger.CONTENT_LOG, "update informers, current ssid: " + ssid);
        Logger.d(Logger.CONTENT_LOG, "update informers, transivers: " + transivers);
        Transiver transiver = transivers.stream().filter(it->it.getSsid().equals(ssid)).findAny().orElse(null);
        if(transiver != null){
            Logger.d(Logger.CONTENT_LOG, "transiver: " + transiver);
            if(utils.getIp(ssid) != null){
                currentTransiver.setIp(transiver.getIp());
                currentTransiver.setVersion(transiver.getVersion());
                Logger.d(Logger.CONTENT_LOG, "update ip: " + currentTransiver.getIp());
                updateBtnCotentSendState();
                updateUI();
            }
        }
    }

    private void updateUI(List<Transiver> transivers){
        updateFields();
    }


    protected abstract void updateBtnCotentSendState();


    @Override
    public void onTaskCompleted(Bundle result) {
        String command = result.getString(BundleKeys.COMMAND_KEY);
        String ip = result.getString(BundleKeys.IP_KEY);
        String response = result.getString(BundleKeys.RESPONSE_KEY);


        int resultCode = result.getInt("resultCode");
        String resultStr = result.getString("result");
        Logger.d(Logger.CONTENT_LOG, "on task completed, resultCode: " + resultCode);

        if(resultCode == TaskCode.REBOOT_STM_CODE && resultStr.contains("Tested")){
            Logger.d(Logger.CONTENT_LOG, "stm rebooted");
            utils.showMessage(getString(R.string.stm_rebooted));
        }
        else if(resultCode == TaskCode.REBOOT_CODE){
            ((MainActivity)context).onBackPressed();
        }


        else if(resultCode == TaskCode.CLEAR_RASP_CODE){
            Logger.d(Logger.CONTENT_LOG, "rasp was cleared");
            utils.showMessage(getString(R.string.rasp_was_cleared));
        }

        else if(resultCode == TaskCode.SSH_ERROR_CODE || resultCode == TaskCode.DOWNLOADER_ERROR_CODE){
            if(!resultStr.contains("Connection refused")) {
                Logger.d(Logger.CONTENT_LOG, "Connection refused, error");
                utils.showMessage("Error");
            }
        }

        else if(resultCode == TaskCode.SEND_TRANSPORT_CONTENT_CODE && resultStr.contains("Tested")){
            Logger.d(Logger.CONTENT_LOG, "transport content updated");
            utils.showMessage(getString(R.string.content_updated));
            ((MainActivity)context).onBackPressed();

        }

        else if(resultCode == TaskCode.SEND_STATION_CONTENT_CODE && resultStr.contains("Tested")){
            Logger.d(Logger.CONTENT_LOG, "station content updated");
            utils.showMessage(getString(R.string.content_updated));
            ((MainActivity)context).onBackPressed();
        }
        refreshButtons();
    }

    private void basicScan(){
        Logger.d(Logger.CONTENT_LOG, "wifi scan");
        utils.getTakeInfo();
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.d(Logger.CONTENT_LOG, "contentFragment onStart");
        String ssid = getArguments().getString("ssid");
        currentTransiver = viewModel.getTransiverBySsid(ssid);
        if(!refreshButtons()){
            basicScan();
        }
        stopScan();
        mainBtnRescan.setVisibility(View.GONE);
    }

    public boolean refreshButtons(){
        Logger.d(Logger.CONTENT_LOG, "refresh buttons, currentTransiverIp: " +
                currentTransiver.getIp() +" " + utils.getIp(currentTransiver.getSsid()));
        if(currentTransiver.getIp() != null || utils.getIp(currentTransiver.getSsid()) != null){
            myHandler.post(updateRunnable);
            return true;
        }
        return false;
    }

    public abstract void updateFields();

    public abstract void stopScan();

    public static class RebootConfDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String rebootType = getArguments().getString(REBOOT_TYPE);
            String ip = getArguments().getString("ip");
            String version = getArguments().getString("version");

            Logger.d(Logger.CONTENT_LOG, "show reboot/clear confirmation dialog: type - " + rebootType + ", ip: " + ip);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.confirmation_is_required);

            if(rebootType.equals(REBOOT_RASP) || rebootType.equals(REBOOT_STM) || rebootType.equals(REBOOT_ALL)) {
                builder.setMessage(getString(R.string.confirm_reboot_of) + " " + rebootType);
            }
            else if(rebootType.equals(REBOOT_CLEAR)){
                builder.setMessage(R.string.ask_clear_the_transiver);
            }

            builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Logger.d(Logger.CONTENT_LOG, "on click, version: " + version + ", reboot type: " + rebootType);
                    if(version != null && !version.equals("ssh_conn")){
                        if(rebootType.equals(REBOOT_STM) || rebootType.equals(REBOOT_RASP) || rebootType.equals(REBOOT_ALL)) {
                            Thread thread = new Thread(new PostInfo((ContentFragment) App.get().getFragmentHandler().getCurrentFragment(), ip,
                                    PostCommand.REBOOT + "_" + rebootType));
                            thread.start();
                        }
                        else if(rebootType.equals(REBOOT_CLEAR)){
                            Thread thread = new Thread(new PostInfo((ContentFragment) App.get().getFragmentHandler().getCurrentFragment(), ip,
                                    PostCommand.ERASE_CONTENT));
                            thread.start();
                        }
                    } else if(version != null && version.equals("ssh_conn")){
                        SshConnection connection = new SshConnection(((ContentFragment)App.get().getFragmentHandler().getCurrentFragment()));
                        if(rebootType.equals(REBOOT_RASP)){
                            connection.execute(ip, SshConnection.REBOOT_CODE);
                        }
                        else if(rebootType.equals(REBOOT_STM)){
                            connection.execute(ip, SshConnection.REBOOT_STM_CODE);
                        }
                        else if(rebootType.equals(REBOOT_CLEAR)){
                            connection.execute(ip, SshConnection.CLEAR_RASP_CODE);
                        }
                    }

                }
            });

            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });

            builder.setCancelable(true);
            return builder.create();
        }
    }
}


class ContentClickListener implements View.OnClickListener{
    private Transiver transiver;
    private Utils utils;
    public ContentClickListener(Transiver transiver, Utils utils){
        this.transiver = transiver;
        this.utils = utils;
    }

    @Override
    public void onClick(View v) {
        String ip = transiver.getIp();
        if(ip == null){
            ip = utils.getIp(transiver.getSsid());
        }

        String version = utils.getVersion(transiver.getSsid());
        Logger.d(Logger.CONTENT_LOG, "currentTransIp: " + ip + ", version: " + version);
        Bundle bundle = new Bundle();
        bundle.putString("ip", ip);
        bundle.putString("version", version);
        DialogFragment dialog = null;
        switch (v.getId()) {
            case R.id.content_btn_rasp:
                Logger.d(Logger.CONTENT_LOG, "reboot raspberry btn was pressed");
                bundle.putString(ContentFragment.REBOOT_TYPE, ContentFragment.REBOOT_RASP);
                dialog = new ContentFragment.RebootConfDialog();
                break;
            case R.id.content_btn_stm:
                Logger.d(Logger.CONTENT_LOG, "reboot stm btn was pressed");
                bundle.putString(ContentFragment.REBOOT_TYPE, ContentFragment.REBOOT_STM);
                dialog = new ContentFragment.RebootConfDialog();
                break;
            case R.id.content_btn_clear:
                Logger.d(Logger.CONTENT_LOG, "clear btn was pressed");
                bundle.putString(ContentFragment.REBOOT_TYPE, ContentFragment.REBOOT_CLEAR);
                dialog = new ContentFragment.RebootConfDialog();
                break;

            case R.id.content_btn_all:
                Logger.d(Logger.CONTENT_LOG, "reboot all btn was pressed");
                bundle.putString(ContentFragment.REBOOT_TYPE, ContentFragment.REBOOT_ALL);
                dialog = new ContentFragment.RebootConfDialog();
                break;
        }
        dialog.setArguments(bundle);
        dialog.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().CONFIRMATION_DIALOG_TAG);
    }



}
