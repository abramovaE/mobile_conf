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

import com.kotofeya.mobileconfigurator.transivers.Transiver;

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




    public List<String> getClients() {
        return clients;
    }

    public void setClients(List<String> clients) {
        this.clients = clients;
    }


//    public void getTakeInfo(OnTaskCompleted listener){
//        clients = WiFiLocalHotspot.getInstance().getClientList();
//        ExecutorService executorService = Executors.newFixedThreadPool(clients.size());
//        CompletableFuture<Void>[] futures = new CompletableFuture[clients.size()];
//
//        for (int i = 0; i < clients.size(); i++) {
//            futures[i] = CompletableFuture.runAsync(new SshConnectionRunnable(listener, clients.get(i), SshConnection.TAKE_CODE), executorService);
//        }
//        CompletableFuture.allOf(futures).thenRun(() -> {
//                        Logger.d(Logger.WIFI_LOG, "clients res: " + clients);
//                        executorService.shutdown();
//                        return;
//        });
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
            if(transiver.getIp().equalsIgnoreCase(ip)){
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
                            ((ScannerAdapter)transiversLv.getAdapter()).notifyDataSetChanged();
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
                    transivers.add(transiver);
                    ((ScannerAdapter)transiversLv.getAdapter()).notifyDataSetChanged();
                }
                else {
                    updateTransiver(transiver);
                }
            }
        }
    }


    public void addTakeInfo(String takeInfo, boolean createNew){
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
                isExist = true;
            }
        }
        if(!isExist && createNew) {
            Logger.d(Logger.UTILS_LOG, "add new transiver");
            Transiver transiver = new Transiver(ssid, ip, macWifi, macBt, boardVersion, osVersion,
                        stmFirmware, stmBootloader, core, modem, incrementOfContent,
                        uptime, cpuTemp, load);
            transivers.add(transiver);
            Logger.d(Logger.UTILS_LOG, "transivers: " + transivers);

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
            informerFromList.setRawData(transiver.getRawData());
            ((ScannerAdapter)transiversLv.getAdapter()).notifyDataSetChanged();
        }
        if((Math.abs(informerFromList.getRssi() - transiver.getRssi())) > 20){
            informerFromList.setDelFlag(true);
        }
    }

    public void stopLVTimer() {
        Logger.d(Logger.UTILS_LOG, "updateLvTimer: " + updateLv);
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
