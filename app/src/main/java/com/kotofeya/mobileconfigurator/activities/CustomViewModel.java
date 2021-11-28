package com.kotofeya.mobileconfigurator.activities;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.kotofeya.mobileconfigurator.Logger;
import com.kotofeya.mobileconfigurator.network.post_response.TakeInfoFull;
import com.kotofeya.mobileconfigurator.newBleScanner.CustomScanResult;
import com.kotofeya.mobileconfigurator.newBleScanner.MySettings;
import com.kotofeya.mobileconfigurator.newBleScanner.TransiversFactory;
import com.kotofeya.mobileconfigurator.transivers.Transiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class CustomViewModel extends ViewModel {
    private  MutableLiveData<List<String>> clients = new MutableLiveData<>();
    public MutableLiveData<List<String>> getClients(){return clients;}
    public void setClients(List<String> clients){this.clients.postValue(clients);}

    private MutableLiveData<List<Transiver>> transivers = new MutableLiveData<>();
    private MutableLiveData<List<Transiver>> stationaryInformers = new MutableLiveData<>();
    private MutableLiveData<List<Transiver>> transpInformers = new MutableLiveData<>();
    public MutableLiveData<List<Transiver>> getTransivers(){return transivers;}
    public MutableLiveData<List<Transiver>> getStationaryInformers() { return stationaryInformers; }
    public MutableLiveData<List<Transiver>> getTranspInformers() {
        return transpInformers;
    }

    private static Map<String, String> ssidIpMap = new HashMap<>();
    private static Map<String, String> ssidVersionMap = new HashMap<>();

    private MutableLiveData<Transiver> currentStatInformer = new MutableLiveData<>();
    private MutableLiveData<Transiver> currentTranspInformer = new MutableLiveData<>();

    private MutableLiveData<Boolean> isGetTakeInfoFinished = new MutableLiveData<>();

    public MutableLiveData<Boolean> getIsGetTakeInfoFinished() {
        return isGetTakeInfoFinished;
    }

    public static class ModelFactory extends ViewModelProvider.NewInstanceFactory {
        public ModelFactory() {
            super();
        }
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass == CustomViewModel.class) {
                return (T) new CustomViewModel();
            }
            return null;
        }
    }

    public void setTakeInfoFinished(boolean b){
        isGetTakeInfoFinished.postValue(b);
    }

    public void addTakeInfoFull(String ip, String version, TakeInfoFull takeInfoFull, boolean createNew){
        Logger.d(Logger.VIEW_MODEL_LOG, "add take info full: " + takeInfoFull.getSerial() + ", version: " + version);
        List<Transiver> transiversValue = transivers.getValue();
        if(transiversValue == null){
            transiversValue = new CopyOnWriteArrayList<>();
        }
        Logger.d(Logger.VIEW_MODEL_LOG, "transivers value: " + transiversValue);

        boolean isExist = false;
        String ssid = takeInfoFull.getSerial() + "";
        ssid = Transiver.formatSsid(ssid);
        Logger.d(Logger.VIEW_MODEL_LOG, "ssid: " + ssid);
        Transiver tr = getTransiverByIp(ip);
        Logger.d(Logger.VIEW_MODEL_LOG, "tr by ip: " + tr);
        if(tr == null){
            tr = getTransiverBySsid(ssid);
            Logger.d(Logger.VIEW_MODEL_LOG, "tr by ssid: " + tr);
        }
        if(tr != null){
            Logger.d(Logger.VIEW_MODEL_LOG, "update transiver, version: " + version);
            isExist = true;
            tr.setTakeInfoFull(takeInfoFull);
            tr.setIp(ip);
            tr.setVersion(version);
            tr.setSsid(ssid);
        }
        if(!isExist && createNew) {
            Logger.d(Logger.VIEW_MODEL_LOG, "add new transiver, version: " + version);
            Transiver transiver = new Transiver(ip);
            transiver.setTakeInfoFull(takeInfoFull);
            transiver.setVersion(version);
            transiver.setSsid(ssid);
            transiversValue.add(transiver);
            Logger.d(Logger.VIEW_MODEL_LOG, "transivers: " + transiversValue.size());
        }
        Logger.d(Logger.VIEW_MODEL_LOG, "put into ssidMap: " + ssid + " " + ip);
        ssidIpMap.put(ssid, ip);
        ssidVersionMap.put(ssid, version);
        postTransiversValueToAllLists(transiversValue);
        Logger.d(Logger.VIEW_MODEL_LOG, "post value: " + transivers.getValue());
    }

    public synchronized void addTakeInfo(String takeInfo, boolean createNew){
        Logger.d(Logger.VIEW_MODEL_LOG, "transivers: " + transivers.getValue());
        Logger.d(Logger.VIEW_MODEL_LOG, "add take info: " + takeInfo);
        List<Transiver> transiversValue = transivers.getValue();
        if(transiversValue == null){
            transiversValue = new CopyOnWriteArrayList<>();
        }
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
        for(Transiver t: transiversValue){
            Logger.d(Logger.VIEW_MODEL_LOG, "t.ssid: " + t.getSsid() + " , ssid: " + ssid );
            if(t.getSsid() != null && t.getSsid().equals(ssid)){
                Logger.d(Logger.VIEW_MODEL_LOG, "update transiver");
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
            Logger.d(Logger.VIEW_MODEL_LOG, "add new transiver, transivers: " + transiversValue.size());
            Transiver transiver = new Transiver(ssid, ip, macWifi, macBt, boardVersion, osVersion,
                    stmFirmware, stmBootloader, core, modem, incrementOfContent,
                    uptime, cpuTemp, load, tType);
            transiversValue.add(transiver);
            Logger.d(Logger.VIEW_MODEL_LOG, "transivers: " + transiversValue.size());
        }
        ssid = Transiver.formatSsid(ssid);
        ssidIpMap.put(ssid, ip);
        Logger.d(Logger.VIEW_MODEL_LOG, "transivers value: " + transivers.getValue());
        postTransiversValueToAllLists(transiversValue);
    }

    public Transiver getTransiverByIp(String ip){
        Logger.d(Logger.VIEW_MODEL_LOG, "get transiver by ip: " + ip);
        if(transivers.getValue() != null) {
            return transivers.getValue().stream().filter(it -> ip.equalsIgnoreCase(it.getIp())).findAny().orElse(null);
        } else {
            transivers.postValue(new CopyOnWriteArrayList<>());
        }
        return null;
    }
    public Transiver getTransiverBySsid(String ssid) {
        Logger.d(Logger.VIEW_MODEL_LOG, "get transiver by ssid: " + ssid);
        if (transivers.getValue() != null) {
            String serial = Transiver.formatSsid(ssid);
            return transivers.getValue().stream().filter(it -> serial.equalsIgnoreCase(it.getSsid())).findAny().orElse(null);
        }
        return null;
    }

    public void removeTransiver(Transiver transiver){
        List<Transiver> transiversValue = transivers.getValue();
        transiversValue.remove(transiver);
        postTransiversValueToAllLists(transiversValue);
    }

    public void clearTransivers(){
        Logger.d(Logger.VIEW_MODEL_LOG, "clear transivers");
        List<Transiver> transiversValue = transivers.getValue();
        if(transiversValue != null) {
            transiversValue.clear();
        } else {
            transiversValue = new CopyOnWriteArrayList<>();
        }
        postTransiversValueToAllLists(transiversValue);
    }

    public void updateResults(List<CustomScanResult> results){
        List<Transiver> statList = filterInformers(results, MySettings.STATIONARY);
        List<Transiver> transpList = filterInformers(results, MySettings.TRANSPORT);
        this.stationaryInformers.postValue(statList);
        this.transpInformers.postValue(transpList);
        if(currentStatInformer.getValue() !=  null){
            this.currentStatInformer.postValue(getTransiverBySsid(currentStatInformer.getValue().getSsid()));
        }
        if(currentTranspInformer.getValue() !=  null){
            this.currentTranspInformer.postValue(getTransiverBySsid(currentTranspInformer.getValue().getSsid()));
        }
        List<Transiver> transiversList = new CopyOnWriteArrayList<>();
        if(transiversList == null){
            transiversList = new CopyOnWriteArrayList<>();
        } else {
            transiversList.clear();
        }
        transiversList.addAll(statList);
        transiversList.addAll(transpList);
        for(Transiver transiver: transiversList){
            transiver.setIp(ssidIpMap.get(transiver.getSsid()));
            transiver.setVersion(ssidVersionMap.get(transiver.getSsid()));
            Logger.d(Logger.VIEW_MODEL_LOG, "ssidmap: " + ssidIpMap);
        }
        Logger.d(Logger.VIEW_MODEL_LOG, "update result transivers: " + transiversList);
        postTransiversValueToAllLists(transiversList);
    }

    private List<Transiver> filterInformers(List<CustomScanResult> results, int radioType){
        List<CustomScanResult> res = filter(results, radioType);
        List<Transiver> informers = new CopyOnWriteArrayList<>();
        for(CustomScanResult customScanResult: res){
            Transiver informer = createInformer(customScanResult);
            if(informer != null){
                informers.add(informer);
            }
        }
        ArrayList<Transiver> temp = new ArrayList<>();
        temp.addAll(informers);
        Collections.sort(temp, new Comparator<Transiver>(){
            @Override
            public int compare(Transiver o1, Transiver o2) {
                return o1.getSsid().compareTo(o2.getSsid());
            }
        });
        informers.clear();
        informers.addAll(temp);
        return informers;
    }

    private List<CustomScanResult> filter(List<CustomScanResult> results, int radioType){
        if(radioType == MySettings.STATIONARY){
            return  results.stream().filter(r-> (
                    r.getTransiverType() == radioType) ||
                    (r.getTransiverType() == MySettings.TRIOL)
            ).collect(Collectors.toList());
        }
        return  results.stream().filter(r-> r.getTransiverType() == radioType).collect(Collectors.toList());
    }

    private Transiver createInformer(CustomScanResult result) {
        TransiversFactory factory = new TransiversFactory();
        return factory.getInformer(result);
    }

    public String getIp(String ssid){
        ssid = Transiver.formatSsid(ssid);
        Logger.d(Logger.VIEW_MODEL_LOG, "get ip, ssid: " + ssid);
        Logger.d(Logger.VIEW_MODEL_LOG, "get ip, ssidipMap: " + ssidIpMap);
        Logger.d(Logger.VIEW_MODEL_LOG, "get ip, ssidipMap: " + ssidIpMap.get(ssid));
        return ssidIpMap.get(ssid);
    }

    public String getVersion(String ssid){
        Logger.d(Logger.VIEW_MODEL_LOG, "get version, map: " + ssidVersionMap);
        Logger.d(Logger.VIEW_MODEL_LOG, "get version, ssid: " + ssid);
        return ssidVersionMap.get(ssid);
    }

    public void clearMap(){ ssidIpMap.clear(); }

    public boolean needScanStationaryTransivers() {
        List<Transiver> transiverList = transivers.getValue();
        if(transiverList == null || transiverList.size() == 0){
            return true;
        }
        for(Transiver t: transiverList){
            if(t.isStationary() || !t.isTransport()){
                if(t.getIp() == null || getIp(t.getSsid()) == null){
                    Logger.d(Logger.VIEW_MODEL_LOG, "needScanStationaryTransivers: " + true);
                    return true;
                }
            }
        }
        Logger.d(Logger.VIEW_MODEL_LOG, "needScanStationaryTransivers: " + false);
        return false;
    }

    private void postTransiversValueToAllLists(List<Transiver> transiversList){
        transivers.postValue(transiversList);
        stationaryInformers.postValue(transiversList.stream().filter(it-> it.getTType().equals("stationary")).collect(Collectors.toList()));
        transpInformers.postValue(transiversList.stream().filter(it->it.getTType().equals("transport")).collect(Collectors.toList()));
    }
}