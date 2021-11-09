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
import com.kotofeya.mobileconfigurator.Logger;
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
                                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                                .build();

                        scanCallback = new ScanCallback() {
                            @Override
                            public void onScanResult(int callbackType, ScanResult result) {
                                String serial = getSerial(result);
                                if(serial != null) {
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

    public static byte[] getFullData(ScanResult result) {
        String name = result.getScanRecord().getDeviceName();
        byte[] data = null;
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
        }
        return data;
    }

    private String getSerial(ScanResult result){
        String name = result.getScanRecord().getDeviceName();;
        byte[] data = getFullData(result);
        if(data != null){
            if(name.equals("stp")){
                return  (((data[2] & 0xFF) << 16) + ((data[3] & 0xFF) << 8) + (data[4] & 0xFF)) + "";
            }
        }
        return name;
    }

    public List<CustomScanResult> getResults(){
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
                        Logger.d(Logger.BLE_SCANNER_LOG, "try sleep 500");
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
}