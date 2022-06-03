package com.kotofeya.mobileconfigurator;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;

import com.kotofeya.mobileconfigurator.activities.CustomViewModel;
import com.kotofeya.mobileconfigurator.activities.InterfaceUpdateListener;
import com.kotofeya.mobileconfigurator.activities.MainActivity;
import com.kotofeya.mobileconfigurator.clientsHandler.ClientsHandler;
import com.kotofeya.mobileconfigurator.newBleScanner.CustomBluetooth;
import com.kotofeya.mobileconfigurator.newBleScanner.CustomScanResult;
import com.kotofeya.mobileconfigurator.user.UserFactory;
import com.kotofeya.mobileconfigurator.user.UserType;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils  {
    public static final int TRANSP_RADIO_TYPE = 0x80;
    public static final int STAT_RADIO_TYPE = 0x40;
    public static final int ALL_RADIO_TYPE = 0;

    public static final String TITLE_SCAN_CLIENTS = "";
    public static final String MESSAGE_SCAN_CLIENTS = "Поиск подключенных клиентов";

    public static final String MESSAGE_TAKE_INFO = "Опрос подключенных трансиверов";

    private final ClientsHandler clientsHandler;

    private final Context context;
    private final CustomViewModel viewModel;
    private final CustomBluetooth newBleScanner;
    private Thread thread;

    private int radioType;

    public void setRadioType(int radioType) {
        this.radioType = radioType;
    }

    public Utils(Context context, CustomBluetooth newBleScanner) {
        this.context = context;
        this.viewModel = ViewModelProviders.of((MainActivity)context, new CustomViewModel.ModelFactory()).get(CustomViewModel.class);
        this.newBleScanner = newBleScanner;
        this.clientsHandler = ClientsHandler.getInstance(viewModel);
    }

    public void clearClients(){
        clientsHandler.clearClients();
    }
    public void clearMap(){ viewModel.clearMap(); }

    public String getIp(String ssid){
        return viewModel.getIp(ssid);
    }
    public String getVersion(String ssid){
        return viewModel.getVersion(ssid);
    }

    public void stopClientsHandler(){
        clientsHandler.stopScanning();
    }

    public void updateClients(InterfaceUpdateListener interfaceUpdateListener){
        clientsHandler.updateClients(interfaceUpdateListener);
//        clientsHandler.pingIp()
    }

    public void getTakeInfo(){
        Logger.d(Logger.UTILS_LOG, "getTakeInfo()");
         clientsHandler.getTakeInfo();
    }





    public InternetConn getInternetConnection() {
        return clientsHandler.getInternetConnection();
    }
    public void removeClient(String ip){
        Logger.d(Logger.UTILS_LOG, "remove: " + ip);
        clientsHandler.removeClient(ip);
    }

//    public void showMessage(String message){
//        Bundle bundle = new Bundle();
//        bundle.putString("message", message);
//        MessageDialog dialog = new MessageDialog();
//        dialog.setArguments(bundle);
//        dialog.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().CONFIRMATION_DIALOG_TAG);
//    }

    public void startRvTimer(){
        if(thread != null){
            thread.interrupt();
        } thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if(newBleScanner.isScanning()) {
                        List<CustomScanResult> results = newBleScanner.getResults();
                        Logger.d(Logger.UTILS_LOG, "results: " + results.size());
                        viewModel.updateResults(results);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        });
        thread.start();
    }

    public CustomBluetooth getNewBleScanner() {
        return newBleScanner;
    }




    private Map<String, String> addToTransportContent(Map<String, String> transportContent,
                                                             String key, String value){
        UserType userType = UserFactory.getUser().getUserType();
        if(userType.equals(UserType.USER_FULL)) {
            transportContent.put(key, value);
        } else if(userType.equals(UserType.USER_TRANSPORT)){
            String login = App.get().getLogin();
            String region = login.substring(login.lastIndexOf("_") + 1);
            if(value.contains(region) || value.contains("zzz")) {
                transportContent.put(key, value);
            }
        }
        return transportContent;
    }

    private String getTransportFileKey(String s, boolean isInternetEnabled){
        if(isInternetEnabled){
            return s.substring(0, s.indexOf("/"));
        } else {
            return s.substring(s.lastIndexOf("/") + 1).split("_")[0];
        }
    }

    public Map<String, String> getTransportContent(){
        Map<String, String> transportContent = new HashMap<>();
        boolean isInternetEnabled = getInternetConnection().hasInternetConnection();
        Collection<String> collection = (isInternetEnabled) ? Downloader.tempUpdateTransportContentFiles :
                App.get().getUpdateContentFilePaths();
        for (String s : collection) {
            String key = getTransportFileKey(s, isInternetEnabled);
            transportContent = addToTransportContent(transportContent, key, s);
        }
        return transportContent;
    }

    public AlertDialog getScanClientsDialog(){
        return new AlertDialog.Builder(context)
                .setTitle(TITLE_SCAN_CLIENTS)
                .setMessage(MESSAGE_SCAN_CLIENTS)
                .setCancelable(false)
                .create();
    }

    public boolean hasClients(){
        Logger.d(Logger.UTILS_LOG, "has clients");
        return clientsHandler.getClients().size() > 0;
    }
}
