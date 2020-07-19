package com.kotofeya.mobileconfigurator;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.ListView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.VIBRATOR_SERVICE;

public class Utils {

    public static final int TRANSP_RADIO_TYPE = 0x80;
    public static final int STAT_RADIO_TYPE = 0x40;

    public static final int ALL_RADIO_TYPE = 0;


    private Transiver currentTransiver;
    private List<Transiver> transivers;
    private Set<String> ssidListRunTime;
    private List<Transiver> forDel;
    private BTHandler bluetooth;


    private int radioType;

//    private Context mContext;
//    private ContentFormer contentFormer;
//    private MySettings settings;

//    private IFragmentHandler fragmentHandler;
//    private ITextHandler textHandler;
    private InformerFilter filter;
//    private IDownloader downloader;
    private Timer updateLv;
//    private IPlayer player;
//    private Translit translit;

    private ListView transiversLv;



    private int callingBuzzer;

    boolean isConnecting;

    private int currentLevel;

    public List<Transiver> getTransivers() {
        return transivers;
    }

    public Utils() {
        bluetooth = new BTHandler(this);
        this.transivers = new ArrayList<>();

//        this.mContext = context;
//        contentFormer = new ContentFormer(this);
//        this.settings = settings;
//        textHandler = new TextHandler(this);
        filter = new InformerFilter(this);

        ssidListRunTime = new HashSet<>();

//        isConnecting = false;
//        wifi.registerCallBack(this);
//        isNeedSaying = true;
//        this.translit = new Translit(this);

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

    public Transiver getCurrentTransiver() {
        return currentTransiver;
    }

    public void setCurrentTransiver(Transiver currentTransiver) {
        this.currentTransiver = currentTransiver;
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
//                            connectAndDownloadInfo(iRadioInf);
                        }
                    }

                    if(!forDel.isEmpty()){
                        Logger.d(Logger.UTILS_LOG, "remove informers " + forDel);

//                        new Handler(Looper.getMainLooper()).post(() -> {
//
//                            List<Transiver> infFromRVMainAdapter = (List<Transiver>) cloneObject(rvMainAdapter.getInformerArrayList());

                            transivers.removeAll(forDel);
                            forDel.clear();

                        new Handler(Looper.getMainLooper()).post(() -> {
                            ((ScannerAdapter)transiversLv.getAdapter()).notifyDataSetChanged();
                        });

//                            updateRvContent(infFromRVMainAdapter);
//                        });

                    }
                }
            }
//            }
        }, 0, 1000);
    }




    @SuppressWarnings("unchecked")
    public void addTransiver(Transiver transiver) {
//        Logger.d(Logger.UTILS_LOG, "adding transiver: " + transiver);
//        List<Transiver> infFromRVMainAdapter = (List<Transiver>) cloneObject(rvMainAdapter.getInformerArrayList());


        if(transiver != null) {

            boolean isContains = false;
            for(Transiver t: transivers){
                if(t.getSsid().equals(transiver.getSsid())){
                    isContains = true;
                }
            }

            if (!isContains) {
                transivers.add(transiver);
                ((ScannerAdapter)transiversLv.getAdapter()).notifyDataSetChanged();

            }
            else {
                updateTransiver(transiver);
//                ((ScannerAdapter)transiversLv.getAdapter()).notifyDataSetChanged();

            }
        }

//        Logger.d(Logger.UTILS_LOG, "transivers: " + transivers);
//        ((ScannerAdapter)transiversLv.getAdapter()).notifyDataSetChanged();


//        ScannerAdapter adapter = (ScannerAdapter) transiversLv.getAdapter();
//        adapter.notifyDataSetChanged();
//        updateRvContent(infFromRVMainAdapter);
    }


//
//    private void updateRvContent(List<IRadioInformer> infFromRVMainAdapter){
//        MyDiffUtilCallback myDiffUtilCallback = new MyDiffUtilCallback(iRadioInformerList, infFromRVMainAdapter, this);
//        DiffUtil.DiffResult productDiffResult = DiffUtil.calculateDiff(myDiffUtilCallback);
//        rvMainAdapter.setInformerArrayList(iRadioInformerList);
//        productDiffResult.dispatchUpdatesTo(rvMainAdapter);
//    }


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
        updateLv.cancel();
    }

    private Transiver getBySsid(String ssid) {
        for(Transiver transiver: transivers){
            if(transiver.getSsid().equals(ssid)){
                return transiver;
            }
        }
        return null;
    }





    public static String byteArrayToBitString(byte b) {
        return String.format("%8s", Integer.toBinaryString(b)).replace(' ', '0');
    }



    public void addToSsidRunTimeSet(String ssid) {
        ssidListRunTime.add(ssid);
    }




}
