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
    private static final int DEFAULT_SCAN_PERIOD = 5;
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

    public void scanAndFilterDevice() {
        HashMap<String, BluetoothDevice> scanDeviceMap;

        try {
            scanDeviceMap = scanner.scanDevice(DEFAULT_SCAN_PERIOD, null, null);
            Log.i("CLIENT", scanDeviceMap.size() + " devices found!");
        } catch (InterruptedException e) {
            Log.e("BLE-SCAN-DBG", "Failed to scan devices");
            return;
        }

        for (String mac : scanDeviceMap.keySet()) {
            if (filterDevice(mac,scanDeviceMap.get(mac))) {
                Log.i("BLE-SCAN-DBG",scanDeviceMap.get(mac).getName() + " was added to target list");
                targetDevices.put(mac, scanDeviceMap.get(mac));
            }
        }

        // 順番に接続を行う
        for (String mac : targetDevices.keySet()) {
            BluetoothDevice device = targetDevices.get(mac);
            // ここで接続処理を行う
            if (device != null) {
                Log.i("BLEConnector","connecting to device: " + device.getName() + " (" + device.getAddress() + ")");
                connector.connectGattServer(device);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private boolean filterDevice(String mac, BluetoothDevice device) {
        if (device.getName() == null) {
            return  false;
        }
        if (device.getName().equals("A11")) {
            Log.i("device", "A11 found!");
            return true;
        } else {
            Log.d("device","device Name: " + device.getName() + " == A11: false" );

        }

        return false;
    }
}
