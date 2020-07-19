package com.kotofeya.mobileconfigurator;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class BTHandler {

    private static final int REQUEST_FINE_LOCATION = 2;
    public static final int REQUEST_ENABLE_BT = 1;

    public static final int ADV_TIMEOUT = 5; //seconds

    private Utils utils;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;

    private ScanCallback scanCallback;
    private AtomicBoolean mScanning;
    private AtomicBoolean mRequested;
    private AtomicBoolean mRescan;
    private Timer rescanTimer;
    private AtomicBoolean IsTextFromBT;

    public BTHandler(Utils utils) {
        Random r = new Random();
        this.utils = utils;
        BluetoothManager bluetoothManager = (BluetoothManager) App.get().getContext().getSystemService(Context.BLUETOOTH_SERVICE);

        if (bluetoothManager != null){
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }
        mScanning = new AtomicBoolean(false);
        mRequested = new AtomicBoolean(false);
        mRescan = new AtomicBoolean(false);
        IsTextFromBT = new AtomicBoolean(false);
    }



    public AtomicBoolean getmScanning() {
        return mScanning;
    }

    public boolean startScan(boolean force) {


        if (!hasPermissions()){
            Logger.d(Logger.BT_HANDLER_LOG, "no permissions");

            mScanning.set(false);
            return false;
        }

        Logger.d(Logger.BT_HANDLER_LOG, "!mScanning.get(): " + !mScanning.get());
        Logger.d(Logger.BT_HANDLER_LOG, "force: " + force);
        if (!mScanning.get() || force) {
            mScanning.set(true);
            ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
            scanCallback = new BTScannerCB(utils, mBluetoothAdapter);
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            List<ScanFilter> filters = new ArrayList<>();
            filters.add(new ScanFilter.Builder().setManufacturerData(0xffff, new byte[0]).build());
            mBluetoothLeScanner.startScan(filters, settings, scanCallback);
            Logger.d(Logger.BT_HANDLER_LOG,"Start scanning...");
            if (!mRescan.get()){
                setRescan(true);
            }
            utils.startLVTimer();
        }
        return true;
    }



    private void setRescan(boolean set) {
        mRescan.set(set);
        if (set) {
            rescanTimer = new Timer();
            rescanTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (mScanning.get()) {
                        int cnt = 0;
                        Logger.d(Logger.BT_HANDLER_LOG,
                                "Rescan..." + Thread.currentThread().getName());
                        while (true) {
                            if (mRescan.get()) {
                                stopScan(false);
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (cnt++ >= 20) {
                                    startScan(false);
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                    }
                }
            }, 300000, 300000);
            Logger.d(Logger.BT_HANDLER_LOG, "Rescan activated");
        } else {
            if (rescanTimer != null)
            {
                rescanTimer.cancel();
                rescanTimer.purge();
                rescanTimer = null;
                Logger.d(Logger.BT_HANDLER_LOG, "Rescan deactivated");
            }
        }
    }

    public void stopScan(boolean disableRescan) {
        utils.stopLVTimer();
        if (mScanning.get()) {
            if (disableRescan) {
                setRescan(false);
            }
            mScanning.set(false);
            mBluetoothLeScanner.stopScan(scanCallback);
            Logger.d(Logger.BT_HANDLER_LOG, "Stop scanning...");
        }
    }



    private boolean hasPermissions() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            requestBluetoothEnable();
            //mBluetoothAdapter.enable();
            return false;
        } else if (!hasLocationPermissions()) {
            requestLocationPermission();
            return false;
        }
        return true;
    }

    private void requestBluetoothEnable() {
        if (mRequested.get()){
            return;
        }
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        ((AppCompatActivity)App.get().getContext()).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        Logger.d(Logger.BT_HANDLER_LOG,
                "Requested user enables Bluetooth. Try start scanning again.");
        mRequested.set(true);
    }

    private boolean hasLocationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return App.get().getContext()
                    .checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }
    private void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ((AppCompatActivity) App.get().getContext()).requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_FINE_LOCATION);
            Logger.d(Logger.BT_HANDLER_LOG,
                    "Requested user Location Permission. Try start scan again.");
        }
    }
}

