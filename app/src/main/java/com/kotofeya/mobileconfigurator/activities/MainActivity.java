package com.kotofeya.mobileconfigurator.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.FragmentHandler;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.Utils;

public class MainActivity extends AppCompatActivity {

    Utils utils;
    TextView label;
    Button mainBtnRescan;

    @Override
    public void onStart() {
        super.onStart();
//        mainBtnRescan.setVisibility(View.GONE);
//        label.setText(R.string.main_menu_main_label);
        utils.getBluetooth().stopScan(true);
//        utils.clearTransivers();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        utils = new Utils();

        FragmentHandler fragmentHandler = new FragmentHandler(this);
        App.get().setFragmentHandler(fragmentHandler);
        fragmentHandler.changeFragment(FragmentHandler.MAIN_FRAGMENT_TAG);

        label = findViewById(R.id.main_txt_label);


        mainBtnRescan = findViewById(R.id.main_btn_rescan);
        mainBtnRescan.setVisibility(View.GONE);
        label.setText(R.string.main_menu_main_label);


    }

    @Override
    public void onBackPressed() {
        label.setText(R.string.main_menu_main_label);
        mainBtnRescan.setVisibility(View.GONE);
        super.onBackPressed();
    }

    public Utils getUtils(){
        return utils;
    }

}
