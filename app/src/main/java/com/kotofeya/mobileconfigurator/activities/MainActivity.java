package com.kotofeya.mobileconfigurator.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.FragmentHandler;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity  implements OnTaskCompleted {

    Utils utils;
    TextView label;
    Button mainBtnRescan;

    TextView loginTxt;
    TextView dateTxt;

    @Override
    public void onStart() {
        super.onStart();
//        mainBtnRescan.setVisibility(View.GONE);
//        label.setText(R.string.main_menu_main_label);
        utils.getBluetooth().stopScan(true);
//        utils.getTakeInfo(this);
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

        loginTxt = findViewById(R.id.main_txt_login);
        dateTxt = findViewById(R.id.main_txt_date);

        loginTxt.setText("Abramov");

        Runnable runnable = new CountDownRunner();
        Thread myThread= new Thread(runnable);
        myThread.start();

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

    @Override
    public void onTaskCompleted(Bundle result) {
        int resultCode = result.getInt("resultCode");
        String res = result.getString("result");
        Logger.d(Logger.BASIC_SCANNER_LOG, "resultCode: " + resultCode);
        if(resultCode == TaskCode.TAKE_CODE){
            utils.addTakeInfo(res, true);
//            scannerAdapter.notifyDataSetChanged();
//            myHandler.post(updateRunnable);
        }

        Logger.d(Logger.MAIN_LOG, "trans: " + utils.getTransivers());

    }

    @Override
    public void onProgressUpdate(Integer downloaded) {

    }

    public void doWork() {
        runOnUiThread(new Runnable() {
            public void run() {
                try{
                    TextView txtCurrentTime= (TextView)findViewById(R.id.main_txt_date);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy/HH:mm:ss", Locale.getDefault());
                    String currentDateandTime = sdf.format(new Date());
                    txtCurrentTime.setText(currentDateandTime);
                }catch (Exception e) {}
            }
        });
    }


    class CountDownRunner implements Runnable{
        // @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()){
                try {
                    doWork();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }catch(Exception e){
                }
            }
        }
    }
}
