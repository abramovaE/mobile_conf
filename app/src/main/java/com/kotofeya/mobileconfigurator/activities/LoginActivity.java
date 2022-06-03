package com.kotofeya.mobileconfigurator.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.CheckUser;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, CheckUser.MyCustomCallBack, View.OnKeyListener {

    private EditText loginTxt;
    private EditText passwordTxt;
    private boolean isRemembered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        loginTxt = findViewById(R.id.login_txt_login);
        passwordTxt = findViewById(R.id.login_txt_password);
        Button signInBtn = findViewById(R.id.login_btn);
        CheckBox rememberMeChk = findViewById(R.id.rememberMeChk);

        signInBtn.setOnClickListener(this);
        loginTxt.setOnKeyListener(this);
        passwordTxt.setOnKeyListener(this);

        isRemembered = App.get().isRemembered();
        rememberMeChk.setChecked(isRemembered);
        if(isRemembered){
            loginTxt.setText(App.get().getLogin());
            passwordTxt.setText(App.get().getPassword());
        }

        rememberMeChk.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Logger.d(Logger.MAIN_LOG, "remember me chk isChecked: " + isChecked);
            isRemembered = isChecked;
        });
    }



    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
    }

    @Override
    public void onClick(View v) {
        String login = loginTxt.getText().toString();
        String password = passwordTxt.getText().toString();
        new CheckUser(this, login, password, this).execute();
    }

    @Override
    public void doIfUserValid() {
        Intent intent = new Intent(this, MainActivity.class);
        String login = loginTxt.getText().toString();
        String password = passwordTxt.getText().toString();

        startActivity(intent);
        App.get().setLogin(login);
        if(isRemembered){
            App.get().saveLoginInformation(login, password, true);
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