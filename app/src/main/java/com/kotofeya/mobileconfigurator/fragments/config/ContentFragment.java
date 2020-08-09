package com.kotofeya.mobileconfigurator.fragments.config;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.WiFiLocalHotspot;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.fragments.update.UpdateOsFragment;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

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

    Transiver currentTransiver;

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


        btnRebootRasp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentTransiver = utils.getCurrentTransiver();
                if(currentTransiver.getIp() == null){
                    basicScan();
                }
                else {
                    Logger.d(Logger.CONTENT_LOG, "currentTransIp: " + currentTransiver.getIp());
                    SshConnection connection = new SshConnection(((ContentFragment)App.get().getFragmentHandler().getCurrentFragment()));
                    connection.execute(currentTransiver.getIp(), SshConnection.REBOOT_COMMAND);
                }
            }
        });
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

        else {
            String res = result.getString("result");
            if (res.split("\n").length > 10) {
                Transiver transiver = new Transiver(null, res);
                utils.addSshTransiver(transiver);
            }

            Logger.d(Logger.CONTENT_LOG, "currentTransSsid: " + currentTransiver.getSsid());


            if(res.contains(currentTransiver.getSsid())){
                Logger.d(Logger.CONTENT_LOG, "currentTransIp: " + currentTransiver.getIp());
                SshConnection connection = new SshConnection(((ContentFragment)App.get().getFragmentHandler().getCurrentFragment()));
                connection.execute(currentTransiver.getIp(), SshConnection.REBOOT_COMMAND);
            }
        }

    }




    private void basicScan(){
        List<String> clients = WiFiLocalHotspot.getInstance().getClientList();
        for(String s: clients){
//            Transiver transiver = new Transiver(s);
//            utils.addSshTransiver(transiver);
            SshConnection connection = new SshConnection(this);
//            utils.setCurrentTransiver(transiver);
            connection.execute(s, SshConnection.TAKE_COMMAND);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public abstract void stopScan();
}
