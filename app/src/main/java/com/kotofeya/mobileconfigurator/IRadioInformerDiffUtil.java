package com.kotofeya.mobileconfigurator;


import android.os.Bundle;

import androidx.recyclerview.widget.DiffUtil;

import com.kotofeya.mobileconfigurator.newBleScanner.MySettings;
import com.kotofeya.mobileconfigurator.transivers.Transiver;
import com.kotofeya.mobileconfigurator.transivers.TriolInformer;


import java.util.List;

public class IRadioInformerDiffUtil extends DiffUtil.Callback {
    private final List<Transiver> oldResults;
    private final List<Transiver> newResults;

    public IRadioInformerDiffUtil(List<Transiver> oldResults, List<Transiver> newResults) {
        this.oldResults = oldResults;
        this.newResults = newResults;
    }

    public List<Transiver> getOldResults() {
        return oldResults;
    }
    public List<Transiver> getNewResults() {
        return newResults;
    }

    @Override
    public int getOldListSize() {
        return oldResults.size();
    }

    @Override
    public int getNewListSize() {
        return newResults.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        Transiver oldCustomScanResult = oldResults.get(oldItemPosition);
        Transiver newCustonScanResult = newResults.get(newItemPosition);
        return oldCustomScanResult.getSsid().equals(newCustonScanResult.getSsid());
    }

//    сравниваем только то, что видно в gui
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Transiver oldCustomScanResult = oldResults.get(oldItemPosition);
        Transiver newCustonScanResult = newResults.get(newItemPosition);
        if(oldCustomScanResult.getTransiverType() == MySettings.TRIOL && newCustonScanResult.getTransiverType() == MySettings.TRIOL){
            int oldCounter = ((TriolInformer) oldCustomScanResult).getCounterStatus();
            int newCounter = ((TriolInformer) newCustonScanResult).getCounterStatus();
            if(newCounter > oldCounter){
                ((TriolInformer) newCustonScanResult).setWorking(true);
            } else if(newCounter <= oldCounter){
                ((TriolInformer) newCustonScanResult).setWorking(false);
            }
        }
        return (oldCustomScanResult.getBleExpText()
                .equals(newCustonScanResult.getBleExpText())
                && oldCustomScanResult.getFloorOrDoorStatus() == newCustonScanResult.getFloorOrDoorStatus()
        );
    }

    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        Transiver oldCustomScanResult = oldResults.get(oldItemPosition);
        Transiver newCustonScanResult = newResults.get(newItemPosition);

        Bundle diff = new Bundle();
        if (!oldCustomScanResult.getBleExpText()
                .equals(newCustonScanResult.getBleExpText())) {
            diff.putString("content", newCustonScanResult.getSsid());
            Logger.d(Logger.MAIN_LOG, "get change payload: content");
        }

        if (oldCustomScanResult.getFloorOrDoorStatus() != newCustonScanResult.getFloorOrDoorStatus()) {
            diff.putString("door status", newCustonScanResult.getSsid());
            Logger.d(Logger.MAIN_LOG, "get change payload: doorstatus");

        }
        if (diff.size() == 0) {
            return null;
        }
        return diff;
    }
}
