package com.kotofeya.mobileconfigurator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;


import com.kotofeya.mobileconfigurator.transivers.StatTransiver;
import com.kotofeya.mobileconfigurator.transivers.Transiver;
import com.kotofeya.mobileconfigurator.transivers.TransportTransiver;

import java.util.List;

class BTScannerCB extends ScanCallback {

    private Utils utils;
    private BluetoothAdapter mBluetoothAdapter;

    BTScannerCB(Utils utils, BluetoothAdapter adapter) {
        this.utils = utils;
        mBluetoothAdapter = adapter;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        addResult(result);
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        for (ScanResult result : results) {
            addResult(result);
        }
    }

    @Override
    public void onScanFailed(int errorCode) {
        if (errorCode == 2) {
            utils.getBluetooth().stopScan(true);
            mBluetoothAdapter.disable();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mBluetoothAdapter.enable();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            utils.getBluetooth().startScan(true);
        }
        Logger.d(2, "Error: " + errorCode);
    }


    private void addResult(ScanResult result) {
        if (utils.getFilter().filter(result)) {
            Transiver transiver;
                if (utils.getRadioType() == Utils.TRANSP_RADIO_TYPE) {
                        transiver = new TransportTransiver(result);
                    }
                    else if(utils.getRadioType() == Utils.STAT_RADIO_TYPE) {
                        transiver = new StatTransiver(result);
                    }
                    else {
                        transiver = new Transiver(result);
                        if(transiver.isTransport()){
                            transiver = new TransportTransiver(result);
                        }
                        else if (transiver.isStationary()){
                            transiver = new StatTransiver(result);
                        }
                    }
                utils.addToSsidRunTimeSet(transiver.getSsid());
                utils.addTransiver(transiver);
        } else {
            utils.addTransiver(null);
        }
    }
}
