package com.kotofeya.mobileconfigurator.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.google.gson.Gson;
import com.kotofeya.mobileconfigurator.App;
import com.kotofeya.mobileconfigurator.City;
import com.kotofeya.mobileconfigurator.Downloader;
import com.kotofeya.mobileconfigurator.FragmentHandler;
import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.OnTaskCompleted;
import com.kotofeya.mobileconfigurator.R;
import com.kotofeya.mobileconfigurator.SendLogToServer;
import com.kotofeya.mobileconfigurator.TaskCode;
import com.kotofeya.mobileconfigurator.Utils;
import com.kotofeya.mobileconfigurator.network.PostCommand;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity  implements OnTaskCompleted {

    Utils utils;
    TextView label;
    ImageButton mainBtnRescan;
    TextView loginTxt;
    TextView dateTxt;
    public static City cities[];
    private static final int TETHER_REQUEST_CODE = 1;
    private static final String HOTSPOT_DIALOG_TAG = "HOTSPOT_DIALOG";
//    private boolean isNeedDestroy = false;
//    private boolean isReadyToDestroy = false;

    private CustomViewModel viewModel;

    @Override
    public void onStart() {
        super.onStart();
        utils.getBluetooth().stopScan(true);
    }

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
        fragmentHandler.changeFragment(FragmentHandler.MAIN_FRAGMENT_TAG, false);

        label = findViewById(R.id.main_txt_label);
        mainBtnRescan = findViewById(R.id.main_btn_rescan);
        mainBtnRescan.setVisibility(View.GONE);
        label.setText(R.string.main_menu_main_label);
        loginTxt = findViewById(R.id.main_txt_login);
        dateTxt = findViewById(R.id.main_txt_date);
        loginTxt.setText(App.get().getLogin());

        Runnable runnable = new CountDownRunner();
        Thread timerThread= new Thread(runnable);
        timerThread.start();

        Downloader cityDownloader = new Downloader(this);
        cityDownloader.execute(Downloader.CITY_URL);

        if(App.get().isAskForTeneth()) {
            HotSpotSettingsDialog dialog = new HotSpotSettingsDialog();
            dialog.show(fragmentHandler.getFragmentManager(), HOTSPOT_DIALOG_TAG);
        }
        boolean isInternetEnabled = utils.getInternetConnection().hasInternetConnection();
        if(isInternetEnabled){
//            Intent intent = new Intent(MainActivity.this,
//                    SendLogToServer.class);
//            intent.putExtra("log", Logger.getServiceLogString());
//            startService(intent);
            new Thread(new SendLogToServer(Logger.getServiceLogString(), this)).start();
        }

        viewModel = ViewModelProviders.of(this, new CustomViewModel.ModelFactory()).get(CustomViewModel.class);

    }




    public static class HotSpotSettingsDialog extends DialogFragment implements CompoundButton.OnCheckedChangeListener {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getResources().getString(R.string.hotspot_dialog_title));
            View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.access_point_dialog, null);
            builder.setView(dialogView);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ((MainActivity)getContext()).launchHotspotSettings();
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            builder.setCancelable(true);
            CheckBox doNotAskAgain = dialogView.findViewById(R.id.doNotAskCheckbox);
            doNotAskAgain.setOnCheckedChangeListener(this);
            return builder.create();
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Logger.d(Logger.MAIN_LOG, "on checked changed, is checked: " + isChecked);
            App.get().setAskForTeneth(!isChecked);
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
        Logger.d(Logger.MAIN_LOG, "resultCode: " + resultCode);
        if(resultCode == TaskCode.TAKE_CODE){
            utils.addTakeInfo(res, true);
        } else if(resultCode == PostCommand.getResponseCode(PostCommand.TAKE_INFO_FULL)){
            utils.addTakeInfoFull(result.getString("ip"), result.getParcelable("takeInfoFull"), true);
        } else if(resultCode == TaskCode.SEND_LOG_TO_SERVER_CODE){
//            utils.addTakeInfo(res, true);
        } else if(resultCode == TaskCode.DOWNLOAD_CITIES_CODE){
            getCities(res);
        } else if(resultCode == TaskCode.SEND_LOG_TO_SERVER_CODE){
            int code = result.getInt("code");
            Logger.d(Logger.MAIN_LOG, "send log to server code: " + resultCode);
            if(code == 1){
//                App.get().setLogReport("");
                Logger.clearLogReport();
            }
//            else {
//                App.get().setLogReport(App.get().getLogReport() + "\n"+ Logger.getServiceLogString());
//                Logger.clearLogReport();
//            }
//            if(isNeedDestroy) {
//                isReadyToDestroy = true;
//            }
        }
        Logger.d(Logger.MAIN_LOG, "transivers: " + utils.getTransivers());
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

    @Override
    protected void onDestroy() {
        Logger.d(Logger.MAIN_LOG, "main activity on destroy");
//        boolean isInternetEnabled = utils.getInternetConnection().hasInternetConnection();
//        Thread setnToServer = null;
//        if(isInternetEnabled){
//            setnToServer = new Thread(new SendLogToServer(Logger.getServiceLogString(), this));
//            setnToServer.start();
//        }
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        Logger.d(Logger.MAIN_LOG, "main activity on stop");
        boolean isInternetEnabled = utils.getInternetConnection().hasInternetConnection();
        Thread setnToServer = null;
        if(isInternetEnabled){
            setnToServer = new Thread(new SendLogToServer(Logger.getServiceLogString(), this));
            setnToServer.start();
        }
        super.onStop();
    }
}