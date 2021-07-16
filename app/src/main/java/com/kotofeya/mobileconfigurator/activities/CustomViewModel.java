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
import com.kotofeya.mobileconfigurator.transivers.StatTransiver;
import com.kotofeya.mobileconfigurator.transivers.Transiver;
import com.kotofeya.mobileconfigurator.transivers.TransportTransiver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


public class CustomViewModel extends ViewModel {
    private MutableLiveData<List<Transiver>> transivers = new MutableLiveData<>();
    public MutableLiveData<List<Transiver>> getTransivers(){return transivers;}

    private Map<String, String> ssidIpMap = new HashMap<>();
    private Map<String, String> ssidVersionMap = new HashMap<>();


    private MutableLiveData<Transiver> currentStatInformer = new MutableLiveData<>();
    public MutableLiveData<Transiver> getCurrentStatInformer() { return currentStatInformer; };
    public void setCurrentStatInformer(Transiver currentStatInformer) {this.currentStatInformer.postValue(currentStatInformer);}

    private MutableLiveData<Transiver> currentTranspInformer = new MutableLiveData<>();
    public MutableLiveData<Transiver> getCurrentTranspInformer() { return currentTranspInformer; };
    public void setCurrentTranspInformer(Transiver currentTranspInformer) {this.currentTranspInformer.postValue(currentTranspInformer);}

    private MutableLiveData<List<Transiver>> stationaryInformers = new MutableLiveData<>();
    public MutableLiveData<List<Transiver>> getStationaryInformers() { return stationaryInformers; }
    public void setStationaryInformers(List<Transiver> informers){
        this.stationaryInformers.postValue(informers);
    }

    private MutableLiveData<List<Transiver>> transpInformers = new MutableLiveData<>();
    public MutableLiveData<List<Transiver>> getTranspInformers() {
        return transpInformers;
    }
    public void setTranspInformers(List<Transiver> informers){
        this.transpInformers.postValue(informers);
    }


    public void clearWifiTransivers(){
        List<Transiver> transiverList = transivers.getValue();
        transiverList.clear();
        transivers.postValue(transiverList);
    }

    public void addWifiTransiver(Transiver transiver){
        List<Transiver> transiverList = transivers.getValue();
        transiverList.add(transiver);
        transivers.postValue(transiverList);
    }


    public void updateWifiTransiver(String phoneIp, Transiver transiver){
        List<Transiver> transiverList = transivers.getValue();
        Transiver t = transiverList.stream().filter(it->it.getSsid().equals(transiver.getSsid())).collect(Collectors.toList()).get(0);
    }


//    public void addTransiver(Transiver transiver) {
//        if(transiver != null) {
//            List<Transiver> transiverList = transivers.getValue();
//            boolean isContains = transiverList.stream().anyMatch(trans -> trans.getSsid().equals(transiver.getSsid()));
//            if (!isContains) {
//                Logger.d(Logger.UTILS_LOG, "add transiver: ");
//                transiverList.add(transiver);
//            } else {
//                updateTransiver(transiver);
//            }
//        }
//    }



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


    public void addTakeInfoFull(String ip, String version, TakeInfoFull takeInfoFull, boolean createNew){
        Logger.d(Logger.VIEW_MODEL_LOG, "add take info full: " + takeInfoFull.getSerial() + ", version: " + version);
        List<Transiver> transiversValue = transivers.getValue();
        if(transiversValue == null){
            transiversValue = new ArrayList<>();
        }
        Logger.d(Logger.VIEW_MODEL_LOG, "transivers value: " + transiversValue);


        boolean isExist = false;
        String ssid = takeInfoFull.getSerial() + "";
        if(!ssid.startsWith("stp")) {
            ssid = "stp" + String.format("%6s", ssid).replace(' ', '0');
        }

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

//        ssidVersionMap.put(ssid, version);

        Logger.d(Logger.VIEW_MODEL_LOG, "put into ssidMap: " + ssid + " " + ip);
        ssidIpMap.put(ssid, ip);
        ssidVersionMap.put(ssid, version);
        transivers.postValue(transiversValue);
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

        if(!ssid.startsWith("stp")) {
            ssid = "stp" + String.format("%6s", ssid).replace(' ', '0');
        }
        ssidIpMap.put(ssid, ip);
        Logger.d(Logger.VIEW_MODEL_LOG, "transivers: " + transivers.getValue());
        transivers.postValue(transiversValue);
    }


    public Transiver getTransiverByIp(String ip){
        Logger.d(Logger.VIEW_MODEL_LOG, "get transiver by ip: " + ip);
//        Logger.d(Logger.VIEW_MODEL_LOG, "transivers: " + transivers.getValue());
        if(transivers.getValue() != null) {
            return transivers.getValue().stream().filter(it -> ip.equalsIgnoreCase(it.getIp())).findAny().orElse(null);
        }
        else {
            transivers.postValue(new ArrayList<>());
        }
        return null;
    }
    public Transiver getTransiverBySsid(String ssid) {
        Logger.d(Logger.VIEW_MODEL_LOG, "get transiver by ssid: " + ssid);
        if (transivers.getValue() != null) {
            if(!ssid.startsWith("stp")) {
                String serial = "stp" + String.format("%6s", ssid).replace(' ', '0');
                return transivers.getValue().stream().filter(it -> serial.equalsIgnoreCase(it.getSsid())).findAny().orElse(null);
            } else {
                return transivers.getValue().stream().filter(it -> ssid.equalsIgnoreCase(it.getSsid())).findAny().orElse(null);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public void addTransiver(Transiver transiver) {
        Logger.d(Logger.VIEW_MODEL_LOG, "add transiver: " + transiver);
        List<Transiver> transiversValue = transivers.getValue();
        if(transiversValue == null){
            transiversValue = new ArrayList<>();
        }
        if(transiver != null) {
            boolean isContains = transiversValue.stream().anyMatch(trans -> trans.getSsid().equals(transiver.getSsid()));
            if (!isContains) {
                Logger.d(Logger.VIEW_MODEL_LOG, "add transiver: ");
                transiversValue.add(transiver);
                transivers.postValue(transiversValue);
            } else {
                updateTransiver(transiver);
            }
        }
    }

    private void updateTransiver(Transiver transiver){
        List<Transiver> transiversValue = transivers.getValue();
        Transiver informerFromList = getTransiverByIp(transiver.getIp());
        Logger.d(Logger.VIEW_MODEL_LOG, "inf from list by ip: " + informerFromList);

        if(!Arrays.equals(informerFromList.getRawData(), transiver.getRawData())){
            Logger.d(Logger.VIEW_MODEL_LOG, "update transiver: ");
            informerFromList.setRawData(transiver.getRawData());
            TransportTransiver t;
            if(informerFromList.isTransport()){
                try{
                    t = (TransportTransiver) informerFromList;
                } catch (ClassCastException e) {
                    t = new TransportTransiver(informerFromList.getSsid(),
                            informerFromList.getIp(), informerFromList.getMacWifi(), informerFromList.getMacBt(),
                            informerFromList.getBoardVersion(), informerFromList.getOsVersion(),
                            informerFromList.getStmFirmware(), informerFromList.getStmBootloader(),
                            informerFromList.getCore(), informerFromList.getModem(), informerFromList.getIncrementOfContent(),
                            informerFromList.getUptime(), informerFromList.getCpuTemp(), informerFromList.getLoad(), informerFromList.getTType());
                }
                t.setRawData(transiver.getRawData());
                t.setTransVersion(transiver.getTransVersion());
                transiversValue.remove(informerFromList);
                transiversValue.add(t);
            } else if(informerFromList.isStationary()){
                StatTransiver s;
                try{
                    s = (StatTransiver) informerFromList;
                }
                catch (ClassCastException e) {
                    s = new StatTransiver(informerFromList.getSsid(),
                            informerFromList.getIp(), informerFromList.getMacWifi(), informerFromList.getMacBt(),
                            informerFromList.getBoardVersion(), informerFromList.getOsVersion(),
                            informerFromList.getStmFirmware(), informerFromList.getStmBootloader(),
                            informerFromList.getCore(), informerFromList.getModem(), informerFromList.getIncrementOfContent(),
                            informerFromList.getUptime(), informerFromList.getCpuTemp(), informerFromList.getLoad(), informerFromList.getTType());
                }
                s.setRawData(transiver.getRawData());
                s.setTransVersion(transiver.getTransVersion());
                transiversValue.remove(informerFromList);
                transiversValue.add(s);
                }
        }
        transivers.postValue(transiversValue);
    }

    public void removeTransiver(Transiver transiver){
        List<Transiver> transiversValue = transivers.getValue();
        transiversValue.remove(transiver);
        transivers.postValue(transiversValue);
    }

    public void removeTransivers(List<Transiver> transList){
        List<Transiver> transiversValue = transivers.getValue();
        transiversValue.removeAll(transList);
        transivers.postValue(transiversValue);
    }

    public void clearTransivers(){
        List<Transiver> transiversValue = transivers.getValue();
        if(transiversValue != null) {
            transiversValue.clear();
        }
        else {
            transiversValue = new ArrayList<>();
        }
        transivers.postValue(transiversValue);
    }

    public void updateResults(List<CustomScanResult> results){
        List<Transiver> statList = filterInformers(results, MySettings.STATIONARY);
        List<Transiver> transpList = filterInformers(results, MySettings.TRANSPORT);
//        List<Transiver> statValue = stationaryInformers.getValue();

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


        //7706-104

        transiversList.addAll(statList);
        transiversList.addAll(transpList);
        for(Transiver transiver: transiversList){
            transiver.setIp(ssidIpMap.get(transiver.getSsid()));
            transiver.setVersion(ssidVersionMap.get(transiver.getSsid()));
            Logger.d(Logger.VIEW_MODEL_LOG, "ssidmap: " + ssidIpMap);

        }
        Logger.d(Logger.VIEW_MODEL_LOG, "update result transivers: " + transiversList);
        transivers.postValue(transiversList);
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

        if(!ssid.contains("stp")){
            StringBuffer stringBuffer = new StringBuffer(ssid);
            while (stringBuffer.length() < 6){
                stringBuffer.insert(0, "0");
            }
            ssid = "stp" + stringBuffer.toString();
        }

        Logger.d(Logger.VIEW_MODEL_LOG, "get ip, ssid: " + ssid);
        Logger.d(Logger.VIEW_MODEL_LOG, "get ip, ssidipMap: " + ssidIpMap);
        Logger.d(Logger.VIEW_MODEL_LOG, "get ip, ssidipMap: " + ssidIpMap.get(ssid));
        return ssidIpMap.get(ssid);
    }

    public void addVersion(String ssid, String version){
        this.ssidVersionMap.put(ssid, version);
    }

    public String getVersion(String ssid){
        Logger.d(Logger.VIEW_MODEL_LOG, "get version, map: " + ssidVersionMap);
        Logger.d(Logger.VIEW_MODEL_LOG, "get version, ssid: " + ssid);
        return ssidVersionMap.get(ssid);
    }

    public void clearMap(){ ssidIpMap.clear(); }

}