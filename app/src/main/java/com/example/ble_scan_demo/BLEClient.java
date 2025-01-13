package com.example.ble_scan_demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import com.example.ble_scan_demo.psermission.PermissionDispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

// BLEClientクラス
// BLEデバイスをスキャンし、スキャン結果を返すクラス
// BLEScannerクラスを利用してスキャンを行う
// スキャン結果は、HashMapで管理する
public class BLEClient {
    // 実際のスキャン処理を行うBLEScannerクラス
    private BLEScanner scanner;
    private BLEConnector connector;

    // スキャンを行う間隔
    private static final int DEFAULT_SCAN_PERIOD = 5000;
    // 同じでデバイスに接続をする間隔
    private static final int CONNECT_INTERVAL = 60 * 5;

    // 接続対象のデバイスのリスト
    private final HashMap<String, BluetoothDevice> targetDevices = new HashMap<>();

    private final HashMap<String,Integer> knownDeviceMacList = new HashMap<>();

    private final ArrayList<String> rejectedMacAdressList = new ArrayList<>();

    public BLEClient(Activity activity,BluetoothManager bluetoothManager, BluetoothAdapter bluetoothAdapter, PermissionDispatcher permissionDispatcher) {
        this.scanner = new BLEScanner(
            bluetoothAdapter,
            permissionDispatcher
        );

        this.connector = new BLEConnector(activity);

    }

    public void ScanAndFilterDevice() {
        HashMap<String, BluetoothDevice> scanDeviceMap;

        try {
            scanDeviceMap = scanner.scanDevice(DEFAULT_SCAN_PERIOD, null, null);
        } catch (InterruptedException e) {
            Log.e("BLE-SCAN-DBG", "Failed to scan devices");
            return;
        }

        for (String mac : knownDeviceMacList.keySet()) {
            if (scanDeviceMap.containsKey(mac)) {
                targetDevices.put(mac, scanDeviceMap.get(mac));
            }
        }

        // 順番に接続を行う
        for (String mac : targetDevices.keySet()) {
            BluetoothDevice device = targetDevices.get(mac);
            // ここで接続処理を行う
            if (device != null) {
                connector.connectGattServer(device);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private boolean filterDevice(String mac, BluetoothDevice device) {
        if (rejectedMacAdressList.contains(mac)) {
            return false;
        }

        if (knownDeviceMacList.containsKey(mac)) {
            targetDevices.put(mac, device);
            return true;
        }

        if (device.getName() == "") {
            rejectedMacAdressList.add(mac);
            return false;
        }

        return false;
    }
}
