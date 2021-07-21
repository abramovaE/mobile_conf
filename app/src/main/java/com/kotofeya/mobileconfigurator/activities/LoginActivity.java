package com.kotofeya.mobileconfigurator.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.CheckUser;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;

import java.util.List;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener, CheckUser.MyCustomCallBack, View.OnKeyListener {

    private EditText loginTxt;
    private EditText passwordTxt;
    private Button signInBtn;
    private WifiManager.LocalOnlyHotspotReservation mReservation;
    private List<String> connectedTransivers;
    private CheckBox rememberMeChk;
    private boolean isRemembered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        loginTxt = findViewById(R.id.login_txt_login);
        passwordTxt = findViewById(R.id.login_txt_password);
        signInBtn = findViewById(R.id.login_btn);
        signInBtn.setOnClickListener(this);
        loginTxt.setOnKeyListener(this);
        passwordTxt.setOnKeyListener(this);
        rememberMeChk = findViewById(R.id.rememberMeChk);

        isRemembered = App.get().isRemembered();
        rememberMeChk.setChecked(isRemembered);
        if(isRemembered){
            loginTxt.setText(App.get().getLogin());
            passwordTxt.setText(App.get().getPassword());
        }

        rememberMeChk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Logger.d(Logger.MAIN_LOG, "remember me chk isChecked: " + isChecked);
                isRemembered = isChecked;
            }
        });
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
    }

    @Override
    public void onClick(View v) {
        String login = loginTxt.getText().toString();
        String password = passwordTxt.getText().toString();
        new CheckUser(this, login, password, this).execute();

//        AuthorizationApi authorizationApi = App.get().getRetrofit().create(AuthorizationApi.class);
//        Call<Boolean> isAuthorized = authorizationApi.autorization(login, password);
//
//        isAuthorized.enqueue(new Callback<Boolean>() {
//            @Override
//            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
//                Logger.d(Logger.APP_LOG, "response: " + response);
//            }
//
//            @Override
//            public void onFailure(Call<Boolean> call, Throwable t) {
//                Logger.d(Logger.APP_LOG, "failure: " + t.getMessage());
//            }
//        });

//        // TODO: 16.07.20 validate login and password with observer
//        if(true){
//
//        }
    }

    @Override
    public void doIfUserValid() {
        App.get().setContext(this);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        App.get().setLogin(loginTxt.getText().toString());
        if(isRemembered){
            App.get().saveLoginInformation(loginTxt.getText().toString(), passwordTxt.getText().toString(), true);
        } else {
            App.get().resetLoginInformation();
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        Logger.d(Logger.MAIN_LOG, "key pressed: " + keyCode);
        if(keyCode == KeyEvent.KEYCODE_ENTER){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            return true;
        }
        return false;
    }
}