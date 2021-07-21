package com.kotofeya.mobileconfigurator.newBleScanner;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.kotofeya.mobileconfigurator.activities.MainActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CustomBluetooth {
    public static final int REQUEST_BT_ENABLE = 4;
    public static final int REQUEST_FINE_LOCATION = 2;
    public static final int REQUEST_GPS_ENABLE = 3;
    public static final int ADV_TIMEOUT = 5; //seconds
    private static final int SCANNING_TIME_IN_MINUTES = 20;
    private static final int MAX_APPEARANCE_TIME = 3000;
    private Map<String, CustomScanResult> results;

    private BluetoothLeScanner bleScanner;
    private Context context;
    private boolean isPermissionRequested = false;
    private boolean isGpsRequested = false;
    private boolean isBtRequested = false;
    private long startScanningTime;
    private ScanCallback scanCallback;
    private Thread checkScanningTimeThread;
    private boolean isScanning;
    private LocationManager locationManager;
    private BluetoothAdapter bluetoothAdapter;
    private boolean isAdvSupporting;

    public boolean isScanning() {
        return isScanning;
    }

    public CustomBluetooth(Context context){
        this.context = context;
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if(bluetoothManager != null){
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
        results = new ConcurrentHashMap<>();
//        rssis = new ConcurrentHashMap<>();
        isScanning = false;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        checkScanningTimeThread = new Thread(new CheckScannerTime());
        checkScanningTimeThread.start();
        if(bluetoothAdapter != null) {
            isAdvSupporting = bluetoothAdapter.isMultipleAdvertisementSupported();
        }
    }

    private boolean hasPermission(){
        boolean hasLocationPermissions = context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        Log.d("TAG", "Has location permissions: " + hasLocationPermissions);
        return hasLocationPermissions;
    }

    private void requirePermission(){
        isPermissionRequested = true;
        ((MainActivity)context).requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}
        , REQUEST_FINE_LOCATION);
        Log.d("TAG", "Requested user Location Permission. Try start scan again.");
    }

    private boolean isGpsEnsbled(){
        if(locationManager != null) {
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            Log.d("TAG", "Is gps enabled: " + isGpsEnabled);
            return isGpsEnabled;
        }
        return false;
    }

    private boolean isBtEnabled(){
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void requireGps(){
        if(!isGpsRequested){
            LocationRequest locationRequest = createLocationRequest();
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest);
            SettingsClient client = LocationServices.getSettingsClient(context);
            Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
            task.addOnSuccessListener((Activity) context, new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    // All location settings are satisfied. The client can initialize
                    // location requests here.
                }
            });

            task.addOnFailureListener((MainActivity) context, new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    if (e instanceof ResolvableApiException) {
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult((MainActivity) context, REQUEST_GPS_ENABLE);

                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                    }
                }
            });
            isGpsRequested = true;
        }
    }

    private void requireBT(){
        if(!isBtRequested) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((MainActivity) context).startActivityForResult(enableBtIntent, REQUEST_BT_ENABLE);
            isBtRequested = true;
        }
    }

    public void startScan(){
        Log.d("TAG", "start scanning, bleScanner: " + bleScanner);
        if(isBtEnabled()) {
            bleScanner = bluetoothAdapter.getBluetoothLeScanner();
            isAdvSupporting = bluetoothAdapter.isMultipleAdvertisementSupported();
            if (hasPermission()) {
                if(isGpsEnsbled()) {
                    if(!isScanning) {
                        Log.d("TAG", "all permissions enabled");
                        List<ScanFilter> filters = new ArrayList<>();
                        if (!Build.BRAND.equalsIgnoreCase("google")) {
                            filters.add(new ScanFilter.Builder()
                                    .setManufacturerData(0xffff, new byte[0])
                                    .setDeviceName("stp")
                                    .build());
                        }
                        ScanSettings settings = new ScanSettings.Builder()
                                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
//                                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
                                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
//                                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
//                                .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
                                .build();

                        scanCallback = new ScanCallback() {
                            @Override
                            public void onScanResult(int callbackType, ScanResult result) {
                                String serial = getSerial(result);
                                if(serial != null) {
//                                Logger.d(Logger.BT_LOG, "serial: " + serial);
                                    results.put(serial, new CustomScanResult(result));
                                }
                            }
                        };

                        bleScanner.startScan(filters, settings, scanCallback);
                        startScanningTime = System.currentTimeMillis();
                        isScanning = true;
                    }
                } else {
                    if(!isGpsRequested) {
                        requireGps();
                    }

                }
            } else {
                if(!isPermissionRequested) {
                    requirePermission();
                }
            }
        } else {
            if (!isBtRequested) {
                requireBT();
            }
        }
    }

    public void stopScan(){
        Log.d("TAG", "stop scanning");
        if(bleScanner != null) {
            bleScanner.stopScan(scanCallback);
            isScanning = false;
        }
    }


    private String getSerial(ScanResult result){
        String name = result.getScanRecord().getDeviceName();;
        byte[] data;
        if (name != null && name.startsWith("stp")) {
            byte[] pack1;
            byte[] pack2 = new byte[0];
            byte[] allBytes = result.getScanRecord().getBytes();
            pack1 = Arrays.copyOfRange(allBytes, 9, 31);
            if (allBytes.length > 35) {
                pack2 = Arrays.copyOfRange(allBytes, 35, allBytes.length);
            }
            data = new byte[pack1.length + pack2.length];
            System.arraycopy(pack1, 0, data, 0, pack1.length);
            System.arraycopy(pack2, 0, data, pack1.length, pack2.length);
            String deviceName = result.getScanRecord().getDeviceName();
            if(name.equals("stp")){
                return  (((data[2] & 0xFF) << 16) + ((data[3] & 0xFF) << 8) + (data[4] & 0xFF)) + "";
//                return  "stp" + String.format("%6s", i).replace(' ', '0');
            }
            else {
                return deviceName;
            }
        }
        return null;
    }

    public List<CustomScanResult> getResults(){
//        results.values().stream().forEach(it->Logger.d(Logger.OTHER_LOG, getSerial(it.getScanResult()) + ", time: " + (System.currentTimeMillis() - it.getAppearanceTime())));

        List<CustomScanResult> res = results.values().stream().filter(it ->
                        (((it.getScanResult().getDevice().getName() != null
                                && it.getScanResult().getDevice().getName().equals("stp"))
                                && (System.currentTimeMillis() - it.getAppearanceTime()) < MAX_APPEARANCE_TIME
                                )
                        )).collect(Collectors.toList());
        return res;
    }

    private class CheckScannerTime implements Runnable {
        @Override
        public void run() {
            while (!checkScanningTimeThread.isInterrupted()) {
                if (isScanning && System.currentTimeMillis() - startScanningTime > SCANNING_TIME_IN_MINUTES * 60000) {
                    stopScan();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                    startScan();
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
            }
        }
    }

//    public void call(int buzzerNumber, int Type, IRadioInformer informer, IUtilsStruct utilsStruct) {
//        utilsStruct.setCalledInformer(informer);
//        Logger.d(Logger.BT_LOG, "Calling buzzer: " + buzzerNumber);
////        if (!hasPermissions()){
////            Logger.d(Logger.BT_LOG,"App has not all needed permissions");
////            return;
////        }
////
////        if (!isGpsEnsbled()) {
////            requestGpsEnable();
////        }
//
//        Logger.d(Logger.BT_LOG, "inf is call ready: " + informer.getSsid() + " " + informer.isCallReady(buzzerNumber));
//        if (informer != null && informer.getRawData().length > 10 && informer.isCallReady(buzzerNumber) && !utilsStruct.isAnyTranspCalled()) {
//
//            int callAttempt = informer.getIncrement();
//            Logger.d(Logger.BT_LOG, "callAttempt: " + callAttempt);
//            Logger.d(Logger.BT_LOG, "rawdata[6] " + informer.getRawData()[6]);
//            if(informer.getTransiverType() == MySettings.TRANSPORT){
//                if(buzzerNumber == 0){
//                    utilsStruct.getTextHandler().setCallText(utilsStruct.getInformer(informer.getSsid()).getFloorOrDoorStatus());
//                }
//            }
//            String data = informer.getSsid().substring(4, 10) + "call " + (buzzerNumber + 1) + " " + Type;
//            AdvertiseCallback advertisingCallback = getAdvertisingCallBack();
//            bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
//            bluetoothLeAdvertiser.startAdvertising(getAdvertiseSettings(), getAdvertiseData(data), advertisingCallback);
//
//            if (utilsStruct.getInformer(informer.getSsid()) != null &&
//                    utilsStruct.getInformer(informer.getSsid()).isCalled(buzzerNumber)) {
//                Logger.d(Logger.BT_LOG, "called");
//                int radioType = App.get().getRadioType();
//
//                if(radioType == MySettings.TRANSPORT){
//                    utilsStruct.getSpeaker().sayText(utilsStruct.getContext().getResources().getString(R.string.called_doorsOpened), SpeakerType.CALL_TYPE, true);
//                }
//                else {
//                    utilsStruct.getSpeaker().sayText(utilsStruct.getContext().getResources().getString(R.string.called), SpeakerType.CALL_TYPE, true);
//                }
//            }
//
//
//            if (callThread != null) {
//                callThread.interrupt();
//            }
//            final  int buzzer = buzzerNumber;
//            callThread = new CallThread(utilsStruct, bluetoothLeAdvertiser, advertisingCallback, informer, callAttempt, buzzer);
//            callThread.start();
//        }
//
//        else {
//            Logger.d(Logger.BT_LOG, "inf is null: " + (informer == null) + ", rawData.length: " + informer.getRawData().length + ", informer.isCallReady: " + informer.isCallReady(buzzerNumber) +
//                    ", isAnyCalled " + utilsStruct.isAnyTranspCalled());
//            utilsStruct.getSpeaker().sayText(
//                    utilsStruct.getContext().getResources().getString(R.string.waitABit),
//                    SpeakerType.CALL_TYPE, true);
//            utilsStruct.setCalledInformer(null);
//        }
//    }
//
//    public void open(int doorNumber, int Type, IRadioInformer informer, IUtilsStruct utilsStruct) {
//        Logger.d(Logger.BT_LOG, "Opening door: " + doorNumber);
////        if (!hasPermissions()){
////            Logger.d(Logger.BT_LOG,"App has not all needed permissions");
////            return;
////        }
////        if (!isGpsEnsbled()) {
////            requestGpsEnable();
////        }
//        if (informer != null && informer.getRawData().length > 10) {
//            if(isAdvSupporting){
//                String data = utilsStruct.getInformer(informer.getSsid()).getSsid().substring(4, 10) +
//                        "call " + doorNumber + " " + Type;
//                AdvertiseCallback advertisingCallback = getAdvertisingCallBack();
//                bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
//                bluetoothLeAdvertiser.startAdvertising(getAdvertiseSettings(), getAdvertiseData(data), advertisingCallback);
//                utilsStruct.getSpeaker().sayText(utilsStruct.getContext().getResources().getString(R.string.called), SpeakerType.CALL_TYPE, true);
//                if (callThread != null) {
//                    callThread.interrupt();
//                }
//                callThread = new OpenDoorThread(utilsStruct, bluetoothLeAdvertiser, advertisingCallback, informer);
//                callThread.start();
//            }
//        }
//    }
//
//
//
//    private AdvertiseCallback getAdvertisingCallBack(){
//        AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
//            @Override
//            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
//                Logger.d(Logger.BT_LOG, "Advertising onStartSuccess ");
//                super.onStartSuccess(settingsInEffect);
//            }
//            @Override
//            public void onStartFailure(int errorCode) {
//                Logger.d(Logger.BT_LOG, "Advertising onStartFailure: " + errorCode);
//                super.onStartFailure(errorCode);
//                return;
//            }
//        };
//        return advertisingCallback;
//    }
//
//    private AdvertiseSettings getAdvertiseSettings(){
//        AdvertiseSettings settings = new AdvertiseSettings.Builder()
//                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
//                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
//                .setConnectable(false)
//                .build();
//        return settings;
//    }
//
//    private AdvertiseData getAdvertiseData(String data){
//        byte[] byteData = data.getBytes(Charset.forName("US-ASCII"));
//        byte[] sentData = new byte[byteData.length + 1];
//        System.arraycopy(byteData, 0, sentData, 0, byteData.length);
//        sentData[byteData.length] = incrementalFlag++;
//
//        Logger.d(Logger.BT_LOG, "RawData: " + Arrays.toString(sentData));
//        AdvertiseData advData = new AdvertiseData.Builder()
//                .setIncludeDeviceName(false)
//                .addManufacturerData(0xFEDC, sentData)
//                .build();
//        return advData;
//    }
//    public boolean isMultipleAdvSupported() {
//        return isAdvSupporting;
//    }
//
//    class OpenDoorThread extends Thread {
//        private IUtilsStruct utilsStruct;
//        private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
//        private AdvertiseCallback advertisingCallback;
//        private IRadioInformer informer;
//
//        public OpenDoorThread(IUtilsStruct utilsStruct, BluetoothLeAdvertiser mBluetoothLeAdvertiser, AdvertiseCallback advertiseCallback, IRadioInformer informer){
//            this.utilsStruct = utilsStruct;
//            this.mBluetoothLeAdvertiser = mBluetoothLeAdvertiser;
//            this.advertisingCallback = advertiseCallback;
//            this.informer = informer;
//        }
//
//        @Override
//        public void run() {
//            int counter = 0;
//            final int sleepTime = 50;
//            final int normalizedTimeout = (ADV_TIMEOUT * 1000)/sleepTime;
//            try {
//                while (counter++ <= normalizedTimeout) {
//                    if(utilsStruct.getInformer(informer.getSsid()) != null){
//                        Thread.sleep(sleepTime);
//                    }
//                    else {
//                        Logger.d(Logger.UTILS_LOG, "connection is lost: informer is null ");
//                        utilsStruct.getSpeaker().sayText(App.get().getResources()
//                                .getString(R.string.connectionLost), null, true);
//                        mBluetoothLeAdvertiser.stopAdvertising(advertisingCallback);
//                        return;
//                    }
//                }
//                mBluetoothLeAdvertiser.stopAdvertising(advertisingCallback);
//            } catch (InterruptedException e) {
//                Logger.d(Logger.BT_LOG, "Call interrupted");
//                mBluetoothLeAdvertiser.stopAdvertising(advertisingCallback);
//            }
//        }
//    }
//
//    class CallThread extends Thread {
//
//        private IUtilsStruct utilsStruct;
//        private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
//        private AdvertiseCallback advertisingCallback;
//        private IRadioInformer informer;
//        private int callAttempt;
//        private int buzzer;
//
//        public CallThread(IUtilsStruct utilsStruct,
//                          BluetoothLeAdvertiser mBluetoothLeAdvertiser, AdvertiseCallback advertiseCallback,
//                          IRadioInformer informer, int callAttempt, int buzzer) {
//            this.utilsStruct = utilsStruct;
//            this.mBluetoothLeAdvertiser = mBluetoothLeAdvertiser;
//            this.advertisingCallback = advertiseCallback;
//            this.informer = informer;
//            this.callAttempt = callAttempt;
//            this.buzzer = buzzer;
//
//        }
//
//        @Override
//        public void run() {
//            int counter = 0;
//            final int sleepTime = 50;
//            final int normalizedTimeout = (ADV_TIMEOUT * 1000) / sleepTime;
//            IRadioInformer iRadioInformer;
//            try {
//                while (counter++ <= normalizedTimeout) {
//                    iRadioInformer = utilsStruct.getInformer(informer.getSsid());
//                    Logger.d(Logger.BT_LOG, "informer: " + informer + ", buzzer: " + buzzer);
//                    if (iRadioInformer != null) {
//                        Logger.d(Logger.BT_LOG, "informer: " + informer.getSsid());
//                        int ca = iRadioInformer.getIncrement();
//                        if (ca != callAttempt) {
//                            Logger.d(Logger.BT_LOG, "increment old/new: " + ca + ", callAttempt  " + callAttempt);
//                            if (informer instanceof StationaryInformer) {
//                                utilsStruct.getSpeaker().sayText(utilsStruct.getContext().getResources().getString(R.string.called),
//                                        SpeakerType.CALL_TYPE, true);
//                            } else if (informer instanceof TransportInformer) {
//                                if(buzzer == 1){
//                                    Logger.d(Logger.UTILS_LOG, "btn exit was pressed");
//                                    utilsStruct.getSpeaker().sayText(App.get().getResources().getString(R.string.exit), SpeakerType.CALL_TYPE, true);
//                                    mBluetoothLeAdvertiser.stopAdvertising(advertisingCallback);
//                                    return;
//                                }
//
//
//                                if (utilsStruct.getInformer(informer.getSsid()).isCalled(buzzer)) {
//                                    Logger.d(Logger.BT_LOG, "called");
//                                    utilsStruct.getSpeaker().sayText(utilsStruct.getContext().getResources().getString(R.string.called_doorsOpened), SpeakerType.CALL_TYPE, true);
//                                }
//                            }
//                            mBluetoothLeAdvertiser.stopAdvertising(advertisingCallback);
//                            return;
//                        } else if (informer != null && iRadioInformer != null && iRadioInformer.isCallBusy(buzzer)) {
//                            break;
//                        }
//                        Thread.sleep(sleepTime);
//                    } else {
//                        Logger.d(Logger.UTILS_LOG, "connection is lost: informer is null ");
//                        utilsStruct.getSpeaker().sayText(App.get().getResources().getString(R.string.connectionLost), null, true);
//                        mBluetoothLeAdvertiser.stopAdvertising(advertisingCallback);
//                        return;
//                    }
//                }
//
//                Logger.d(Logger.UTILS_LOG, "doors status: " + informer.getFloorOrDoorStatus());
//
//                mBluetoothLeAdvertiser.stopAdvertising(advertisingCallback);
//                if (informer.getFloorOrDoorStatus() != 2 && informer.getFloorOrDoorStatus() != 0) {
//                    Logger.d(Logger.UTILS_LOG, "connection is lost: timeout ");
//                    utilsStruct.getSpeaker().sayText(App.get().getResources().getString(R.string.connectionLost), SpeakerType.SWAP_TYPE, true);
//                }
//                utilsStruct.setCalledInformer(null);
//
//
//
//            } catch (InterruptedException e) {
//                Logger.d(Logger.BT_LOG, "Call interrupted");
//                mBluetoothLeAdvertiser.stopAdvertising(advertisingCallback);
//            }
//        }
//    }
}
