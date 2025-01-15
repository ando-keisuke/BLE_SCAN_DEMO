package com.example.ble_scan_demo;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.util.Log;

import com.example.ble_scan_demo.psermission.PermissionDispatcher;
import com.example.ble_scan_demo.psermission.PermissionGroup;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

// BLEScannerクラス
// BLEデバイスをスキャンし、スキャン結果を返すクラス
// 非同期で行われるスキャンを同期的に行うためのクラス
public class BLEScanner {
    private final String TAG = "BLEScanner";

    // bluetooth関連のインスタンス
    private final BluetoothAdapter bluetoothAdapter;
    private final PermissionDispatcher permissionDispatcher;

    // Scannerは毎回生成するとよくないので、保持する
    private BluetoothLeScanner scanner;

    private boolean scanning = false;

    // コンストラクタ
    public BLEScanner(
            BluetoothAdapter bluetoothAdapter,
            PermissionDispatcher permissionDispatcher

    ) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.permissionDispatcher = permissionDispatcher;

        // ログ出力
        Log.d(TAG, TAG + ": initialized");
    }

    // デバイスをスキャンする
    // スキャン結果をHashMap<macアドレス、device>として返す
    // timeoutの時間が過ぎるまで同期的にスキャンを行う
    // 完了するまでスレッドをブロックするので注意
    @SuppressLint("MissingPermission")
    public HashMap<String, BluetoothDevice> scanDevice(int scanPeriod, ScanSettings scanSettings, ScanFilter scanFilter) throws InterruptedException {
        Log.i(TAG, TAG + ": checking Bluetooth LE Scanner availability");

        // Scannerがnullの場合は生成する
        if (scanner == null) {
            scanner = bluetoothAdapter.getBluetoothLeScanner();
        }

        // 各種チェック
        // Scannerがnullの場合は例外を投げる
        if (scanner == null) {
            throw new IllegalStateException("Bluetooth LE Scanner is not available");
        }
        // 権限チェック
        if (!permissionDispatcher.checkPermissions(PermissionGroup.BLUETOOTH)) {
            throw new IllegalStateException("Bluetooth permission not granted");
        }

        if (scanning) {
            Log.d(TAG, TAG + ": Already scanning...");
            return new HashMap<>();
        }
        scanning = true;

        // スキャン結果を格納するHashMap
        HashMap<String, BluetoothDevice> deviceMap = new HashMap<>();

        // スキャン結果を受け取るためのCountDownLatch
        CountDownLatch latch = new CountDownLatch(1);

        // スキャンコールバック
        ScanCallback scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();

                if (device != null && device.getAddress() != null) {
                    deviceMap.put(device.getAddress(), device);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                latch.countDown();
                Log.e(TAG, TAG + ": scan failed with error code: " + errorCode);
                scanning = false;
                throw new RuntimeException("Scan failed with error code: " + errorCode);
            }
        };

        Log.i(TAG, TAG + ": start scanning for " + scanPeriod + " seconds");
        // スキャン開始
        try {
            scanner.startScan(
                    List.of(scanFilter != null ? scanFilter : new ScanFilter.Builder().build()),
                    scanSettings != null ? scanSettings : new ScanSettings.Builder().build(),
                    scanCallback
            );
            latch.await(scanPeriod, TimeUnit.SECONDS);
        } finally {
            scanner.stopScan(scanCallback);
            latch.countDown();  // latchの解放は確実に行う
        }

        scanning = false;
        // ログ出力
        Log.i(TAG, TAG + ": scan finished!! Found " + deviceMap.size() + " devices");
        return deviceMap;
    }
}
