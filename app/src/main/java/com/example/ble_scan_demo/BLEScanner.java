package com.example.ble_scan_demo;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.os.Handler;


import com.example.ble_scan_demo.psermission.PermissionDispatcher;
import com.example.ble_scan_demo.psermission.PermissionGroup;

// BLEScannerクラス
// BLE機器のスキャンをするクラス
public class BLEScanner {
    // Bluetoothや権限関連のクラスを保持する変数
    private final PermissionDispatcher permissionDispatcher;
    private final BluetoothAdapter bluetoothAdapter;

    // スキャン関連の変数
    private BluetoothLeScanner scanner;
    private ScanCallback LeScanCallback;
    private boolean scanning = false;

    // スキャンを停止するためのハンドラ
    private final Handler handler = new Handler();

    // コンストラクタ
    public BLEScanner(BluetoothManager bluetoothManager,
                      BluetoothAdapter bluetoothAdapter,
                      PermissionDispatcher permissionDispatcher) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.permissionDispatcher = permissionDispatcher;

        init();
    }
    // 初期化メソッド
    private void init() {
        if (!isBluetoothEnable()) {
            return;
        }
        if (scanner == null) {
            scanner = bluetoothAdapter.getBluetoothLeScanner();
        }
    }
    public boolean isBluetoothEnable() {
        return bluetoothAdapter.isEnabled();
    }

    @SuppressLint("MissingPermission")
    public void startScan(ScanCallback scanCallback, int scanPeriod) {
        if (!permissionDispatcher.checkPermissions(PermissionGroup.BLUETOOTH)) {
            return;
        }

        if (scanning) {
            stopScan();
        }

        handler.postDelayed(() -> {
            scanning = false;
            scanner.stopScan(scanCallback);
        }, scanPeriod);

        scanning = true;
        scanner.startScan(scanCallback);

        this.LeScanCallback = scanCallback;
    }

    @SuppressLint("MissingPermission")
    public void stopScan() {
        if (!scanning) {
            return;
        }

        if (permissionDispatcher.checkPermissions(PermissionGroup.BLUETOOTH)) {
            scanner.stopScan(this.LeScanCallback);
        }

        scanning = false;
    }
}