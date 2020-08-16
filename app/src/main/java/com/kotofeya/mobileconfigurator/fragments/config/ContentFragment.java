package com.kotofeya.mobileconfigurator.fragments.config;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.ScannerAdapter;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.WiFiLocalHotspot;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.fragments.update.UpdateOsFragment;
import com.kotofeya.mobileconfigurator.transivers.Transiver;
import com.kotofeya.mobileconfigurator.transivers.TransportTransiver;

import java.util.List;

public abstract class ContentFragment extends Fragment implements OnTaskCompleted {

    protected Context context;
    protected Utils utils;
    protected View.OnKeyListener onKeyListener;
    protected AdapterView.OnItemSelectedListener onItemSelectedListener;
    protected TextWatcher textWatcher;
    TextView mainTxtLabel;

    protected Button btnContntSend;

    Button btnRebootRasp;
    Button btnRebootStm;
    Button btnClearRasp;

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
        Button mainBtnRescan = ((MainActivity)context).findViewById(R.id.main_btn_rescan);
        mainTxtLabel = ((MainActivity)context).findViewById(R.id.main_txt_label);
        btnRebootRasp = view.findViewById(R.id.content_btn_rasp);
        btnRebootStm = view.findViewById(R.id.content_btn_stm);
        btnContntSend = view.findViewById(R.id.content_btn_send);
        btnClearRasp = view.findViewById(R.id.content_btn_clear);
        contentClickListener = new ContentClickListener(currentTransiver);
        btnRebootRasp.setOnClickListener(contentClickListener);
        btnRebootStm.setOnClickListener(contentClickListener);
        btnClearRasp.setOnClickListener(contentClickListener);
        return view;
    }

    protected abstract void updateBtnCotentSendState();


    @Override
    public void onTaskCompleted(Bundle result) {
        Logger.d(Logger.CONTENT_LOG, "result: " + result);
        Logger.d(Logger.CONTENT_LOG, "currentTransiver: " + currentTransiver);
        if(result.getString("result").contains("reboot")){
            ((MainActivity)context).onBackPressed();
        }

        else if(result.getString("result").contains("Tested")){
            Toast toast = Toast.makeText(context, "Stm rebooted", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            LinearLayout toastContainer = (LinearLayout) toast.getView();
            toastContainer.setBackgroundColor(Color.WHITE);
            toast.show();
        }

        else {
            String res = result.getString("result");
            if (res.split("\n").length > 10) {
                Transiver transiver = new Transiver(null, res);
                utils.addSshTransiver(transiver);
            }
            Logger.d(Logger.CONTENT_LOG, "currentTransSsid: " + currentTransiver.getSsid());
            if(res.contains(currentTransiver.getSsid())){
//                Logger.d(Logger.CONTENT_LOG, "currentTransIp: " + currentTransiver.getIp());
//                SshConnection connection = new SshConnection(((ContentFragment)App.get().getFragmentHandler().getCurrentFragment()));
//                connection.execute(currentTransiver.getIp(), SshConnection.REBOOT_COMMAND);
            }
        }

        refreshButtons();
    }

    private void basicScan(){
        List<String> clients = WiFiLocalHotspot.getInstance().getClientList();
        for(String s: clients){
            SshConnection connection = new SshConnection(this);
            connection.execute(s, SshConnection.TAKE_COMMAND);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        String ssid = getArguments().getString("ssid");
        currentTransiver = utils.getBySsid(ssid);
        if(!refreshButtons()){
            basicScan();
        }
    }

    public boolean refreshButtons(){
        if(currentTransiver.getIp() != null){
            btnRebootRasp.setEnabled(true);
            btnRebootStm.setEnabled(true);
            btnClearRasp.setEnabled(true);
            return true;
        }
        return false;
    }


    public abstract void stopScan();

    public static class RebootConfDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String rebootType = getArguments().getString("rebootType");
            String ip = getArguments().getString("ip");

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Confirmation is required");

            if(rebootType.equals("rasp") || rebootType.equals("stm")) {
                builder.setMessage("Confirm the reboot of " + rebootType);
            }
            else if(rebootType.equals("clear")){
                builder.setMessage("Clear this transiver?");
            }

            builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    SshConnection connection = new SshConnection(((ContentFragment)App.get().getFragmentHandler().getCurrentFragment()));
                    if(rebootType.equals("raspberry")){
                        connection.execute(ip, SshConnection.REBOOT_COMMAND);
                    }
                    else if(rebootType.equals("stm")){
                        connection.execute(ip, SshConnection.REBOOT_STM_COMMAND);
                    }
                    else if(rebootType.equals("clear")){
                        connection.execute(ip, SshConnection.CLEAR_RASP_COMMAND);
                    }
                }
            });

            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
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
    public ContentClickListener(Transiver transiver){
        this.transiver = transiver;
    }

    @Override
    public void onClick(View v) {
        Logger.d(Logger.CONTENT_LOG, "currentTransIp: " + transiver.getIp());
        Bundle bundle = new Bundle();
        bundle.putString("ip", transiver.getIp());
        DialogFragment dialog = null;
        switch (v.getId()) {
            case R.id.content_btn_rasp:
                bundle.putString("rebootType", "raspberry");
                dialog = new ContentFragment.RebootConfDialog();
                break;
            case R.id.content_btn_stm:
                bundle.putString("rebootType", "stm");
                dialog = new ContentFragment.RebootConfDialog();
                break;
            case R.id.content_btn_clear:
                bundle.putString("rebootType", "clear");
                dialog = new ContentFragment.RebootConfDialog();
                break;
        }
        dialog.setArguments(bundle);
        dialog.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().CONFIRMATION_DIALOG_TAG);
    }
}
