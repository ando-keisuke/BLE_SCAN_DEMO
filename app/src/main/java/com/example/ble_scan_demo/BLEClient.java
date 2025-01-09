package com.example.ble_scan_demo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import com.example.ble_scan_demo.psermission.PermissionDispatcher;

import java.util.HashMap;

// BLEClientクラス
// BLEデバイスをスキャンし、スキャン結果を返すクラス
// BLEScannerクラスを利用してスキャンを行う
// スキャン結果は、HashMapで管理する
public class BLEClient {
    // 実際のスキャン処理を行うBLEScannerクラス
    private BLEScanner scanner;

    // スキャン結果を受け取るためのコールバック
    private ScanCallback scanCallback;
    private ScanFilter scanFilter;

    private final int DEFAULT_SCAN_PERIOD = 5000;

    // スキャン結果を管理するHashMap
    private final HashMap<String,ScanResult> scanResults = new HashMap<>();

    public BLEClient(BluetoothManager bluetoothManager, BluetoothAdapter bluetoothAdapter, PermissionDispatcher permissionDispatcher) {
        this.scanner = new BLEScanner(
            bluetoothManager,
            bluetoothAdapter,
            permissionDispatcher
        );

        init();
    }

    private void init() {
        this.scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                scanResults.put(result.getDevice().getAddress(),result);
                Log.i("BLE-SCAN","result" + result);
            }
            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);

                String erroMessage = "";
                if (errorCode == ScanCallback.SCAN_FAILED_ALREADY_STARTED) {
                    erroMessage = "SCAN_FAILED_ALREADY_STARTED";
                } else if (errorCode == ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED) {
                    erroMessage = "SCAN_FAILED_APPLICATION_REGISTRATION_FAILED";
                } else if (errorCode == ScanCallback.SCAN_FAILED_INTERNAL_ERROR) {
                    erroMessage = "SCAN_FAILED_INTERNAL_ERROR";
                } else if (errorCode == ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED) {
                    erroMessage = "SCAN_FAILED_FEATURE_UNSUPPORTED";
                } else if (errorCode == ScanCallback.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES) {
                    erroMessage = "SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES";
                } else if (errorCode == ScanCallback.SCAN_FAILED_SCANNING_TOO_FREQUENTLY) {
                    erroMessage = "SCAN_FAILED_SCANNING_TOO_FREQUENTLY";
                } else {
                    erroMessage = "NO_ERROR";
                }
                Log.e("BLE-SCAN","errorCode" + errorCode + " : " + erroMessage);
            }
        };
        // スキャンフィルターを設定
        this.scanFilter = new ScanFilter.Builder()
                .setDeviceName("Fellow_Map")
                .build();
    }

    public void startScan(BLEScanCallback callback) {
        scanner.startScan(scanFilter,null,scanCallback,DEFAULT_SCAN_PERIOD);

        callback.onScanFinish(scanResults);
    }

    public void stopScan() {
        scanner.stopScan();
    }
}
