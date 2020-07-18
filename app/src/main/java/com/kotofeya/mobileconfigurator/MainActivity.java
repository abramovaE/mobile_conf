package com.kotofeya.mobileconfigurator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PatternMatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

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
            Intent intent = new Intent(this, MainMenu.class);
            startActivity(intent);
        }
    }
}
