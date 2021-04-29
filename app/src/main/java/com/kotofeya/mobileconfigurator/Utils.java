package com.kotofeya.mobileconfigurator;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.kotofeya.mobileconfigurator.bluetooth.BTHandler;
import com.kotofeya.mobileconfigurator.network.PostCommand;
import com.kotofeya.mobileconfigurator.network.PostInfo;
import com.kotofeya.mobileconfigurator.network.post_response.TakeInfoFull;
import com.kotofeya.mobileconfigurator.transivers.StatTransiver;
import com.kotofeya.mobileconfigurator.transivers.Transiver;
import com.kotofeya.mobileconfigurator.transivers.TransportTransiver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Utils implements OnTaskCompleted{

    public static final int TRANSP_RADIO_TYPE = 0x80;
    public static final int STAT_RADIO_TYPE = 0x40;
    public static final int ALL_RADIO_TYPE = 0;

    private List<String> clients;
    private List<Transiver> transivers;
    private Set<String> ssidListRunTime;
    private List<Transiver> forDel;
    private BTHandler bluetooth;
    private int radioType;
    private InformerFilter filter;
    private Timer updateLv;
    private ListView transiversLv;
    private InternetConn internetConnection;

    private Map<String, String> ssidIpMap;

    public void getTakeInfo(OnTaskCompleted listener){
        String deviceIp = internetConnection.getDeviceIp();
        internetConnection.getDhcpAddr();
        if(deviceIp != null){
            clients = WiFiLocalHotspot.getInstance().getClientList(deviceIp);
            Logger.d(Logger.UTILS_LOG, "getClientList: " + clients);
            if (clients.size() > 0) {


                ExecutorService executorService = Executors.newFixedThreadPool(clients.size());
                CompletableFuture<Void>[] futures = new CompletableFuture[clients.size()];
                for (int i = 0; i < clients.size(); i++) {
                    String ip = clients.get(i);
                    new PostInfo(this, ip, PostCommand.VERSION).run();

                }
            }
        }
    }

    public List<Transiver> getTransivers() {
        return transivers;
    }

    public Utils() {
        bluetooth = new BTHandler(this);
        this.transivers = new ArrayList<>();
        filter = new InformerFilter(this);
        ssidListRunTime = new HashSet<>();
        ssidIpMap = new HashMap<>();
        clients = new ArrayList<>();
        internetConnection = new InternetConn();
    }

    public ListView getTransiversLv() {
        return transiversLv;
    }

    public void setTransiversLv(ListView transiversLv) {
        this.transiversLv = transiversLv;
    }

    public int getRadioType() {
        return radioType;
    }

    public void setRadioType(int radioType) {
        this.radioType = radioType;
    }

    public Transiver getTransiverByIp(String ip){
        for(Transiver transiver: transivers){
            if(transiver != null && transiver.getIp() != null && transiver.getIp().equalsIgnoreCase(ip)){
                return transiver;
            }
        }
        return null;
    }


    public BTHandler getBluetooth() {
        return bluetooth;
    }

    public InformerFilter getFilter() {
        return filter;
    }

    @SuppressWarnings("unchecked")
    public void startLVTimer() {
        Logger.d(Logger.UTILS_LOG, "start rv timer");
        if(updateLv != null){
            updateLv.cancel();
        }
        updateLv = new Timer();
        updateLv.schedule(new TimerTask() {

            @Override
            public void run() {
                if(bluetooth.getmScanning().get()) {

                    Set<String> ssidSetRunTimeClone = (Set<String>) cloneObject(ssidListRunTime);
                    ssidListRunTime.clear();
                    forDel = new ArrayList<>();
                    for(Transiver transiver: transivers){
                        int sec = transiver.getDelCount();
                        if (!ssidSetRunTimeClone.contains(transiver.getSsid())) {
                            sec ++;
                            transiver.setDelCount(sec);
                            if(transiver.isDelFlag() || transiver.getDelCount() >= 2){
                                forDel.add(transiver);
                            }
                        }
                        else {
                            transiver.setDelCount(0);
                        }
                    }
                    if(!forDel.isEmpty()){
                        Logger.d(Logger.UTILS_LOG, "remove informers " + forDel);
                            transivers.removeAll(forDel);
                            forDel.clear();
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if(transiversLv != null && transiversLv.getAdapter() != null) {
                                ((ScannerAdapter) transiversLv.getAdapter()).notifyDataSetChanged();
                            }
                        });
                    }
                }

            }
//            }
        }, 0, 1000);
    }

    @SuppressWarnings("unchecked")
    public void addTransiver(Transiver transiver) {
        if(bluetooth.getmScanning().get()){
            if(transiver != null) {
                boolean isContains = transivers.stream().anyMatch(trans -> trans.getSsid().equals(transiver.getSsid()));
                if (!isContains) {
                    Logger.d(Logger.UTILS_LOG, "add transiver: ");
                    transivers.add(transiver);
                    if(transiversLv != null && transiversLv.getAdapter() != null){
                        ((ScannerAdapter)transiversLv.getAdapter()).notifyDataSetChanged();
                    }
                } else {
                    updateTransiver(transiver);
                }
            }
        }
    }

    public synchronized void addTakeInfo(String takeInfo, boolean createNew){
        boolean isExist = false;
        String[] info = takeInfo.split("\n");
        String ip = info[2].trim();
        String ssid = info[1].trim();
        String macWifi = info[3].trim();
        String macBt = info[4].trim();
        String boardVersion = info[5].trim();
        String osVersion = info[6].trim();
        String stmFirmware = info[7].trim();
        String stmBootloader = info[8].trim();
        String core = info[9].trim();
        String modem = info[10].trim();
        String incrementOfContent = info[11].trim();
        String uptime = info[12].trim();
        String cpuTemp = info[13].trim();
        String load = info[14].trim();
        String tType = info[17].trim();

        for(Transiver t: transivers){
            if(t.getSsid() != null && t.getSsid().equals(ssid)){
                Logger.d(Logger.UTILS_LOG, "update transiver");
                t.setIp(ip);
                t.setMacWifi(macWifi);
                t.setMacBt(macBt);
                t.setBoardVersion(boardVersion);
                t.setOsVersion(osVersion);
                t.setStmFirmware(stmFirmware);
                t.setStmBootloader(stmBootloader);
                t.setCore(core);
                t.setModem(modem);
                t.setIncrementOfContent(incrementOfContent);
                t.setUptime(uptime);
                t.setCpuTemp(cpuTemp);
                t.setLoad(load);
                t.setTType(tType);
                isExist = true;
            }
        }
        if(!isExist && createNew) {
            Logger.d(Logger.UTILS_LOG, "add new transiver, transivers: " + transivers.size());
            Transiver transiver = new Transiver(ssid, ip, macWifi, macBt, boardVersion, osVersion,
                        stmFirmware, stmBootloader, core, modem, incrementOfContent,
                        uptime, cpuTemp, load, tType);
            transivers.add(transiver);
            Logger.d(Logger.UTILS_LOG, "transivers: " + transivers.size());
        }
        ssidIpMap.put(ssid, ip);
    }

    public synchronized void addTakeInfoFull(String ip, TakeInfoFull takeInfoFull, boolean createNew){
        boolean isExist = false;
        String ssid = takeInfoFull.getSerial() + "";
        for(Transiver t: transivers){
            if(t.getSsid() != null && t.getSsid().equals(ssid)){
                Logger.d(Logger.UTILS_LOG, "update transiver");
                isExist = true;
                t.setTakeInfoFull(takeInfoFull);
            }
        }
        if(!isExist && createNew) {
            Logger.d(Logger.UTILS_LOG, "add new transiver, transivers: " + transivers.size());
            Transiver transiver = new Transiver(ssid, ip);
            transiver.setTakeInfoFull(takeInfoFull);
            transivers.add(transiver);
            Logger.d(Logger.UTILS_LOG, "transivers: " + transivers.size());
        }
        ssidIpMap.put(ssid, ip);
    }

//    public synchronized void addPostTakeInfo(String takeInfo, boolean createNew){
//        boolean isExist = false;


//        Пример ответа на info-full:
//
//        Type = stationary
//        Serial = 6523
//        Follow server = 95.161.142.79
//        Reply interval = 30
//        Have ping = YES
//
//        System time = 12:40:42
//        Uptime = 35
//        Load 1min = 0.14,
//                Load 5min = 0.08,
//                Load 15min = 0.06
//
//        CPU freq = 700
//        CPU Temperature = 38
//        Free RAM = 339152
//
//        Interface BLE:
//        MAC address = b8:27:eb:94:99:6b
//        PID hci0 = 111
//
//        Interfase - wlan0:
//        MAC address = b8:27:eb:6b:66:94
//        IP address = 10.42.5.13/24
//        Rent address = 598sec
//
//        SC_UART ver. = v2.0.0-core_test
//        SC_UART PID = 294
//        Board version = 3.2
//        STM firmware = 5.50
//        STM bootload = 3.5
//        CORE Linux = stp--g3088c80aeb-dirty
//        Compile date = 2021-04-12 16:23:45
//
//        Incriment CITY = 12
//        STOP Transiver locate = spb
//
//        Date of content = 2021-04-16 09:24:33
//        Locate marsh list = spb
//        Incriment Marsh list = 119
//
//        Content ru.json
//        Date of content = 2020-12-15 17:40:45
//        Short info = Мебельная улица. Автобусная остановка.
//
//
//        Path of log = /overlay/update/logLife
//        Time for daily Reboot = 12 hour
//        Critical CPU load = 3
//        Critical RAM free = 50000 Kb
//        Crontab tasks = * * * * * /usr/local/bin/log_script.sh * * * * * /usr/local/bin/wifiPriority.sh
//        Last reboot = 2021-04-16_09:39:00 - Restarted system at 12 hours of work


//        String[] info = takeInfo.split("\n");
//        String ip = info[2].trim();
//        String ssid = info[1].trim();
//        String macWifi = info[3].trim();
//        String macBt = info[4].trim();
//        String boardVersion = info[5].trim();
//        String osVersion = info[6].trim();
//        String stmFirmware = info[7].trim();
//        String stmBootloader = info[8].trim();
//        String core = info[9].trim();
//        String modem = info[10].trim();
//        String incrementOfContent = info[11].trim();
//        String uptime = info[12].trim();
//        String cpuTemp = info[13].trim();
//        String load = info[14].trim();
//        String tType = info[17].trim();
//
//        for(Transiver t: transivers){
//            if(t.getSsid() != null && t.getSsid().equals(ssid)){
//                Logger.d(Logger.UTILS_LOG, "update transiver");
//                t.setIp(ip);
//                t.setMacWifi(macWifi);
//                t.setMacBt(macBt);
//                t.setBoardVersion(boardVersion);
//                t.setOsVersion(osVersion);
//                t.setStmFirmware(stmFirmware);
//                t.setStmBootloader(stmBootloader);
//                t.setCore(core);
//                t.setModem(modem);
//                t.setIncrementOfContent(incrementOfContent);
//                t.setUptime(uptime);
//                t.setCpuTemp(cpuTemp);
//                t.setLoad(load);
//                t.setTType(tType);
//                isExist = true;
//            }
//        }
//        if(!isExist && createNew) {
//            Logger.d(Logger.UTILS_LOG, "add new transiver, transivers: " + transivers.size());
//            Transiver transiver = new Transiver(ssid, ip, macWifi, macBt, boardVersion, osVersion,
//                    stmFirmware, stmBootloader, core, modem, incrementOfContent,
//                    uptime, cpuTemp, load, tType);
//            transivers.add(transiver);
//            Logger.d(Logger.UTILS_LOG, "transivers: " + transivers.size());
//        }
//        ssidIpMap.put(ssid, ip);
//    }

    public void removeTransiver(Transiver transiver){
        transivers.remove(transiver);
    }

    private synchronized Object cloneObject(Object object){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream ous = null;
        try {
            ous = new ObjectOutputStream(baos);
            ous.writeObject(object);
            ous.close();
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateTransiver(Transiver transiver){

        Transiver informerFromList = getBySsid(transiver.getSsid());
        if(!Arrays.equals(informerFromList.getRawData(), transiver.getRawData())){
            Logger.d(Logger.UTILS_LOG, "update transiver: ");
            informerFromList.setRawData(transiver.getRawData());
            if(informerFromList.isTransport()){
                try{
                    TransportTransiver t = (TransportTransiver) informerFromList;
                }
                catch (ClassCastException e){
                    TransportTransiver transportTransiver = new TransportTransiver(informerFromList.getSsid(),
                            informerFromList.getIp(), informerFromList.getMacWifi(), informerFromList.getMacBt(),
                            informerFromList.getBoardVersion(), informerFromList.getOsVersion(),
                            informerFromList.getStmFirmware(), informerFromList.getStmBootloader(),
                            informerFromList.getCore(), informerFromList.getModem(), informerFromList.getIncrementOfContent(),
                            informerFromList.getUptime(), informerFromList.getCpuTemp(), informerFromList.getLoad(), informerFromList.getTType());

                    transportTransiver.setRawData(transiver.getRawData());
                    transportTransiver.setTransVersion(transiver.getTransVersion());
                    transivers.remove(informerFromList);
                    transivers.add(transportTransiver);
                }

            }

            else if(informerFromList.isStationary()){
                try{
                    StatTransiver s = (StatTransiver) informerFromList;
                }
                catch (ClassCastException e){

                    StatTransiver s  = new StatTransiver(informerFromList.getSsid(),
                            informerFromList.getIp(), informerFromList.getMacWifi(), informerFromList.getMacBt(),
                            informerFromList.getBoardVersion(), informerFromList.getOsVersion(),
                            informerFromList.getStmFirmware(), informerFromList.getStmBootloader(),
                            informerFromList.getCore(), informerFromList.getModem(), informerFromList.getIncrementOfContent(),
                            informerFromList.getUptime(), informerFromList.getCpuTemp(), informerFromList.getLoad(), informerFromList.getTType());

                    s.setRawData(transiver.getRawData());
                    s.setTransVersion(transiver.getTransVersion());
                    transivers.remove(informerFromList);
                    transivers.add(s);
                }
            }
            if(transiversLv != null && transiversLv.getAdapter() != null) {
                ((ScannerAdapter) transiversLv.getAdapter()).notifyDataSetChanged();
            }
        }
        if((Math.abs(informerFromList.getRssi() - transiver.getRssi())) > 20){
            informerFromList.setDelFlag(true);
        }
    }

    public void stopLVTimer() {
        Logger.d(Logger.UTILS_LOG, "stopLvTimer: " + updateLv);
        if(updateLv != null) {
            updateLv.cancel();
        }
    }

    public Transiver getBySsid(String ssid) {
        for(Transiver transiver: transivers){
            if(transiver.getSsid() != null && transiver.getSsid().equals(ssid)){
                return transiver;
            }
        }
        return null;
    }


    public void removeClient(String ip){
        Logger.d(Logger.UTILS_LOG, "remove: " + ip);
        clients.remove(ip);
    }

    public void clearTransivers(){
        transivers.clear();
    }
    public void clearClients(){
        clients.clear();
    }
    public void clearMap(){ ssidIpMap.clear(); }


    public static String byteArrayToBitString(byte b) {
        return String.format("%8s", Integer.toBinaryString(b)).replace(' ', '0');
    }

    public void addToSsidRunTimeSet(String ssid) {
        ssidListRunTime.add(ssid);
    }

    public String getIp(String ssid){
        return ssidIpMap.get(ssid);
    }

//    public void updateBleInfo(Transiver transiver, ScanResult result) {
//        Integer rssi = result.getRssi();
//        String address = result.getDevice().getAddress();
//        String ssid;
//        byte[] rawData = result.getScanRecord().getManufacturerSpecificData(0xffff);
//
//        transiver.setRawData(rawData);
//        if(result.getScanRecord().getDeviceName().equals("stp")){
//            int i = (((rawData[2] & 0xFF) << 16) + ((rawData[3] & 0xFF) << 8) + (rawData[4] & 0xFF));
//            ssid = String.valueOf(i);
//            transiver.setTransVersion(Transiver.VERSION_NEW);
//        }
//        else {
//            ssid = result.getScanRecord().getDeviceName();
//            transiver.setTransVersion(Transiver.VERSION_OLD);
//        }
//        transiver.setSsid(ssid);
//        transiver.setDelFlag(false);
//        updateTransiver(transiver);
//    }

    public boolean needScanStationaryTransivers() {
        Logger.d(Logger.UTILS_LOG, "needScanStationaryTransivers: " + transivers);
        if(transivers.size() == 0){
            return true;
        }
        for(Transiver t: transivers){
            Logger.d(Logger.UTILS_LOG, "t is transport: " + t.isTransport() + ", t is stat: " + t.isStationary());
            Logger.d(Logger.UTILS_LOG, "t: " + t + ", t.getIp(): " + t.getIp() + ", map.getIp(): " + ssidIpMap.get(t.getSsid()));
            if(t.isStationary() || !t.isTransport()){
                if(t.getIp() == null || ssidIpMap.get(t.getSsid()) == null){
                    Logger.d(Logger.UTILS_LOG, "needScanStationaryTransivers: " + true);
                    return true;
                }
            }
        }
        Logger.d(Logger.UTILS_LOG, "needScanStationaryTransivers: " + false);
        return false;
    }

    @Override
    public void onTaskCompleted(Bundle result) {
        String command = result.getString(PostInfo.COMMAND);
        String ip = result.getString(PostInfo.IP);
        String response = result.getString(PostInfo.RESPONSE);

        switch (command){
            case PostCommand.VERSION:
                // TODO: 29.04.2021 проверка версии
                if(response != null){
                    futures[i] = CompletableFuture.runAsync(new PostInfo(listener, ip, PostCommand.TAKE_INFO_FULL), executorService);
                } else {
                    
                }

                break;



        }
    }

    @Override
    public void onProgressUpdate(Integer downloaded) {

    }

    public static class MessageDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            String message = getArguments().getString("message");
            builder.setMessage(message);
            builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            builder.setCancelable(true);
            return builder.create();
        }
    }


    public void showMessage(String message){
        Bundle bundle = new Bundle();
        bundle.putString("message", message);
        Utils.MessageDialog dialog = new Utils.MessageDialog();
        dialog.setArguments(bundle);
        dialog.show(App.get().getFragmentHandler().getFragmentManager(), App.get().getFragmentHandler().CONFIRMATION_DIALOG_TAG);
    }

    public InternetConn getInternetConnection() {
        return internetConnection;
    }




}
