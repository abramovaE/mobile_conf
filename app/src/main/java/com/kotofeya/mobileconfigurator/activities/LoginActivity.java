package com.kotofeya.mobileconfigurator.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.R;

import java.util.List;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText loginTxt;
    private EditText passwordTxt;
    private Button signInBtn;

    private WifiManager.LocalOnlyHotspotReservation mReservation;

    private List<String> connectedTransivers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        loginTxt = findViewById(R.id.login_txt_login);
        passwordTxt = findViewById(R.id.login_txt_password);
        signInBtn = findViewById(R.id.login_btn);

        signInBtn.setOnClickListener(this);
    }


    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);


    }



    @Override
    public void onClick(View v) {
        String login = loginTxt.getText().toString();
        String password = passwordTxt.getText().toString();

        // TODO: 16.07.20 validate login and password with observer
        if(true){
            App.get().setContext(this);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
}
