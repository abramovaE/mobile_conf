package com.kotofeya.mobileconfigurator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class MainMenu extends AppCompatActivity {

    Utils utils;
    TextView label;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        utils = new Utils();

        FragmentHandler fragmentHandler = new FragmentHandler(this);
        App.get().setFragmentHandler(fragmentHandler);
        fragmentHandler.changeFragment(FragmentHandler.MAIN_FRAGMENT_TAG);

        label = findViewById(R.id.main_txt_label);
        label.setText(R.string.main_menu_main_label);

    }

    @Override
    public void onBackPressed() {
        label.setText(R.string.main_menu_main_label);
        Button mainBtnRescan = findViewById(R.id.main_btn_rescan);
        mainBtnRescan.setVisibility(View.GONE);

//
//        Fragment fragment = App.get().getFragmentHandler().getCurrentFragment();
//        Logger.d(Logger.MAIN_LOG, "onBackPressed, " + fragment.getTag());
//
//        if(fragment.getTag() != null){
//            switch (fragment.getTag()){
//                case FragmentHandler.BASIC_SCANNER_FRAGMENT:
//                    label.setText(R.string.main_menu_main_label);
//
//                    break;
//                case FragmentHandler.BLE_SCANNER_FRAGMENT:
//                    label.setText(R.string.main_menu_main_label);
//                    break;
//                case FragmentHandler.CONFIG_STATION_FRAGMENT:
//                    label.setText(R.string.main_menu_main_label);
//                    break;
//                case FragmentHandler.CONFIG_TRANSPORT_FRAGMENT:
//                    label.setText(R.string.main_menu_main_label);
//                    break;
//                case FragmentHandler.STATION_CONTENT_FRAGMENT:
//                    label.setText(utils.getCurrentTransiver().getSsid() + " (" + ((StatTransiver)utils.getCurrentTransiver()).getType() + ")");
//                    break;
//                case FragmentHandler.TRANSPORT_CONTENT_FRAGMENT:
//                    label.setText(utils.getCurrentTransiver().getSsid() + "\n (" + ((TransportTransiver)utils.getCurrentTransiver()).getTransportType() + "/" + ((TransportTransiver)utils.getCurrentTransiver()).getFullNumber() + "/" + ((TransportTransiver)utils.getCurrentTransiver()).getDirection() + ")");
//                    break;
//                case FragmentHandler.UPDATE_CONTENT_FRAGMENT:
//                    label.setText(R.string.main_menu_main_label);
//                    break;
//                case FragmentHandler.UPDATE_OS_FRAGMENT:
//                    label.setText(R.string.main_menu_main_label);
//                    break;
//                case FragmentHandler.UPDATE_STM_FRAGMENT:
//                    label.setText(R.string.main_menu_main_label);
//                    break;
//                case FragmentHandler.MAIN_FRAGMENT_TAG:
//                    label.setText(R.string.main_menu_main_label);
//                    break;
//            }
//        }
//
//
//        else {
//            label.setText(R.string.main_menu_main_label);
//            mainBtnRescan.setVisibility(View.VISIBLE);
//
//        }
        super.onBackPressed();
    }

    public Utils getUtils(){
        return utils;
    }

}
