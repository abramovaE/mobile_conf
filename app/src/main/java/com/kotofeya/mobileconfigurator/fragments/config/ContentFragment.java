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
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.SshConnectionRunnable;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.WiFiLocalHotspot;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.transivers.Transiver;


public abstract class ContentFragment extends Fragment implements OnTaskCompleted {

    public Context context;
    public Utils utils;
    public ImageButton mainBtnRescan;

    private final Handler myHandler = new Handler();
    private void updateUI() {
        btnRebootRasp.setEnabled(true);
        btnRebootStm.setEnabled(true);
        btnClearRasp.setEnabled(true);
        btnContntSend.setEnabled(true);
        updateFields();
    }

    final Runnable updateRunnable = new Runnable() {
        public void run() {
            updateUI();
        }
    };

    protected View.OnKeyListener onKeyListener;
    protected AdapterView.OnItemSelectedListener onItemSelectedListener;
    protected TextWatcher textWatcher;
    TextView mainTxtLabel;

    protected Button btnContntSend;

    Button btnRebootRasp;
    Button btnRebootStm;
    Button btnClearRasp;
    Button spoilerBtn;

    Transiver currentTransiver;

    ContentClickListener contentClickListener;

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
        String ssid = getArguments().getString("ssid");
        currentTransiver = utils.getBySsid(ssid);
        contentClickListener = new ContentClickListener(currentTransiver, utils);
        btnRebootRasp.setOnClickListener(contentClickListener);
        btnRebootStm.setOnClickListener(contentClickListener);
        btnClearRasp.setOnClickListener(contentClickListener);
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
        return view;
    }

    protected abstract void updateBtnCotentSendState();


    @Override
    public void onTaskCompleted(Bundle result) {
        int resultCode = result.getInt("resultCode");
        String resultStr = result.getString("result");
        Logger.d(Logger.CONTENT_LOG, "resultCode: " + resultCode);

        if(resultCode != 0) {
            Logger.d(Logger.CONTENT_LOG, "result: " + result);
        }

        if(resultCode == TaskCode.REBOOT_STM_CODE && resultStr.contains("Tested")){
            Logger.d(Logger.CONTENT_LOG, "stm rebooted");
            utils.showMessage(getString(R.string.stm_rebooted));
        }

        else if(resultCode == TaskCode.REBOOT_CODE){
            ((MainActivity)context).onBackPressed();
        }

        else if(resultCode == TaskCode.TAKE_CODE){
            Logger.d(Logger.CONTENT_LOG, "update wifi scan info");
            utils.addTakeInfo(resultStr, true);
            updateFields();
        }


        else if(result.getInt("resultCode") == PostCommand.getResponseCode(PostCommand.TAKE_INFO_FULL)){
            utils.addTakeInfoFull(result.getString("ip"), result.getParcelable("takeInfoFull"), true);
            updateFields();
        } else if(result.getInt("resultCode") == PostCommand.getResponseCode(PostCommand.TAKE_INFO_FULL_ERROR)){
            if(resultStr.contains("Connection refused") || resultStr.contains("Auth fail")){
                utils.removeClient(result.getString("ip"));
            }
            else {
            utils.showMessage("Error: " + result);
            }
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
        utils.getTakeInfo(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.d(Logger.CONTENT_LOG, "contentFragment onStart");
        String ssid = getArguments().getString("ssid");
        currentTransiver = utils.getBySsid(ssid);
        if(!refreshButtons()){
            basicScan();
        }
        stopScan();
        mainBtnRescan.setVisibility(View.GONE);
    }

    public boolean refreshButtons(){
        Logger.d(Logger.CONTENT_LOG, "currentTransiverIp: " +
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
            String rebootType = getArguments().getString("rebootType");
            String ip = getArguments().getString("ip");
            Logger.d(Logger.CONTENT_LOG, "show reboot/clear confirmation dialog: type - " + rebootType + ", ip: " + ip);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.confirmation_is_required);

            if(rebootType.equals("rasp") || rebootType.equals("stm")) {
                builder.setMessage(getString(R.string.confirm_reboot_of) + " " + rebootType);
            }
            else if(rebootType.equals("clear")){
                builder.setMessage(R.string.ask_clear_the_transiver);
            }

            builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    SshConnection connection = new SshConnection(((ContentFragment)App.get().getFragmentHandler().getCurrentFragment()));
                    if(rebootType.equals("raspberry")){
                        connection.execute(ip, SshConnection.REBOOT_CODE);
                    }
                    else if(rebootType.equals("stm")){
                        connection.execute(ip, SshConnection.REBOOT_STM_CODE);
                    }
                    else if(rebootType.equals("clear")){
                        connection.execute(ip, SshConnection.CLEAR_RASP_CODE);
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
        Logger.d(Logger.CONTENT_LOG, "currentTransIp: " + ip);
        Bundle bundle = new Bundle();
        bundle.putString("ip", ip);
        DialogFragment dialog = null;
        switch (v.getId()) {
            case R.id.content_btn_rasp:
                Logger.d(Logger.CONTENT_LOG, "reboot raspberry btn was pressed");
                bundle.putString("rebootType", "raspberry");
                dialog = new ContentFragment.RebootConfDialog();
                break;
            case R.id.content_btn_stm:
                Logger.d(Logger.CONTENT_LOG, "reboot stm btn was pressed");
                bundle.putString("rebootType", "stm");
                dialog = new ContentFragment.RebootConfDialog();
                break;
            case R.id.content_btn_clear:
                Logger.d(Logger.CONTENT_LOG, "clear btn was pressed");
                bundle.putString("rebootType", "clear");
                dialog = new ContentFragment.RebootConfDialog();
                break;
        }
        dialog.setArguments(bundle);
        dialog.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().CONFIRMATION_DIALOG_TAG);
    }
}
