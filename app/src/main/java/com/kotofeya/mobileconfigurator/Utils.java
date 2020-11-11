package com.kotofeya.mobileconfigurator;


import android.app.Dialog;
import android.bluetooth.le.ScanResult;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

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


public class Utils {

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

    private Map<String, String> ssidIpMap;

    public void getTakeInfo(OnTaskCompleted listener){
        clients = WiFiLocalHotspot.getInstance().getClientList();
        Logger.d(Logger.UTILS_LOG, "getClientList: " + clients);
        if(clients.size() > 0){
            ExecutorService executorService = Executors.newFixedThreadPool(clients.size());
            CompletableFuture<Void>[] futures = new CompletableFuture[clients.size()];
            for (int i = 0; i < clients.size(); i++) {
                futures[i] = CompletableFuture.runAsync(new SshConnectionRunnable(listener, clients.get(i), SshConnection.TAKE_CODE), executorService);
            }
            if(futures != null){
                CompletableFuture.allOf(futures).thenRun(() -> {
                    executorService.shutdown();
                    return;
                });
            }

        }
    }


//    public void getTakeInfo(OnTaskCompleted listener){
//        clients = WiFiLocalHotspot.getInstance().getClientList();
//
//        for(String s: clients){
////            new SshConnectionRunnable(this, s, SshConnection.TAKE_CODE).run();
//            SshConnection connection = new SshConnection(listener);
//            connection.execute(s, SshConnection.TAKE_CODE);
//        }
//    }


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
            if(transiver != null && transiver.getIp().equalsIgnoreCase(ip)){
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
                boolean isContains = false;
//                Logger.d(Logger.UTILS_LOG, "transivers: " + transivers);
                for(Transiver t: transivers){
                    if(t.getSsid() != null && t.getSsid().equals(transiver.getSsid())){
                        isContains = true;
                    }
                }
                if (!isContains) {
                    Logger.d(Logger.UTILS_LOG, "add transiver: ");
                    transivers.add(transiver);
                    if(transiversLv != null && transiversLv.getAdapter() != null){
                        ((ScannerAdapter)transiversLv.getAdapter()).notifyDataSetChanged();
                    }
                }
                else {
                    updateTransiver(transiver);
                }
            }
        }
    }


    public synchronized void addTakeInfo(String takeInfo, boolean createNew){
        boolean isExist = false;

        Logger.d(Logger.UTILS_LOG, "takeInfo: " + takeInfo);

        String[] info = takeInfo.split("\n");

        for(String s: info){
            Logger.d(Logger.UTILS_LOG, "info s: " + s);
        }


        Logger.d(Logger.UTILS_LOG, "info s[1]: " + info[1]);



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
//        Logger.d(Logger.UTILS_LOG, "getBySsid: " + ssid);
//        Logger.d(Logger.UTILS_LOG, "transivers: " + transivers);

        for(Transiver transiver: transivers){
            if(transiver.getSsid() != null && transiver.getSsid().equals(ssid)){
//                Logger.d(Logger.UTILS_LOG, "getByssid: " + transiver);
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

    public void updateBleInfo(Transiver transiver, ScanResult result) {
        Integer rssi = result.getRssi();
        String address = result.getDevice().getAddress();
        String ssid;
        byte[] rawData = result.getScanRecord().getManufacturerSpecificData(0xffff);
        transiver.setRawData(rawData);
        if(result.getScanRecord().getDeviceName().equals("stp")){
            int i = (((rawData[2] & 0xFF) << 16) + ((rawData[3] & 0xFF) << 8) + (rawData[4] & 0xFF));
            ssid = String.valueOf(i);
            transiver.setTransVersion(Transiver.VERSION_NEW);
        }
        else {
            ssid = result.getScanRecord().getDeviceName();
            transiver.setTransVersion(Transiver.VERSION_OLD);
        }
        transiver.setSsid(ssid);
        transiver.setDelFlag(false);
        updateTransiver(transiver);
    }

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
}
