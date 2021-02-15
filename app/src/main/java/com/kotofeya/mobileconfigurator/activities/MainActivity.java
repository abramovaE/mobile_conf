package com.kotofeya.mobileconfigurator.activities;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.City;
import com.kotofeya.mobileconfigurator.Downloader;
import com.kotofeya.mobileconfigurator.FragmentHandler;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.SshConnection;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.fragments.update.UpdateOsFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity  implements OnTaskCompleted {

    Utils utils;
    TextView label;
    Button mainBtnRescan;

    TextView loginTxt;
    TextView dateTxt;

    public static City cities[];

    @Override
    public void onStart() {
        super.onStart();
//        mainBtnRescan.setVisibility(View.GONE);
//        label.setText(R.string.main_menu_main_label);
        utils.getBluetooth().stopScan(true);
//        utils.getTakeInfo(this);
//        utils.clearTransivers();


    }


    private static final int TETHER_REQUEST_CODE = 1;
    private static final String HOTSPOT_DIALOG_TAG = "HOTSPOT_DIALOG";

    private void launchHotspotSettings(){
        Intent tetherSettings = new Intent();
        tetherSettings.setClassName("com.android.settings", "com.android.settings.TetherSettings");
        startActivityForResult(tetherSettings, TETHER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case TETHER_REQUEST_CODE:

                break;
        }
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
        Thread timerThread= new Thread(runnable);
        timerThread.start();

        Downloader cityDownloader = new Downloader(this);
        cityDownloader.execute(Downloader.CITY_URL);


        HotSpotSettingsDialog dialog = new HotSpotSettingsDialog();
        dialog.show(fragmentHandler.getFragmentManager(), HOTSPOT_DIALOG_TAG);

    }


    public static class HotSpotSettingsDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getResources().getString(R.string.hotspot_dialog_title));
            builder.setMessage(getResources().getString(R.string.hotspot_dialog_message));
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ((MainActivity)getContext()).launchHotspotSettings();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            builder.setCancelable(true);
            return builder.create();
        }
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

        else if(resultCode == TaskCode.DOWNLOAD_CITIES_CODE){
            getCities(res);
        }

        Logger.d(Logger.MAIN_LOG, "trans: " + utils.getTransivers());

    }


    private void getCities(String res){
        try {
            JSONObject jObject = new JSONObject(res);
            this.cities = new Gson().fromJson(jObject.get("city").toString(), City[].class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onProgressUpdate(Integer downloaded) {

    }

    public void doWork() {
        runOnUiThread(new Runnable() {
            public void run() {
                try{
                    TextView txtCurrentTime = findViewById(R.id.main_txt_date);
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
