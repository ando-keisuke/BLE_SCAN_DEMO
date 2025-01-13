package com.example.ble_scan_demo.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;

import com.example.ble_scan_demo.psermission.PermissionDispatcher;
import com.example.ble_scan_demo.psermission.PermissionGroup;

public class BluetoothManager {
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private PermissionDispatcher permissionDispatcher;
    private BluetoothLeScanner scanner;
    private BluetoothLeAdvertiser advertiser;

    public BluetoothManager(BluetoothManager bluetoothManager, PermissionDispatcher permissionDispatcher) {
        this.bluetoothManager = bluetoothManager;
        this.bluetoothAdapter = bluetoothManager.getAdapter();
        this.permissionDispatcher = permissionDispatcher;
    }

    // getter
    public BluetoothLeScanner getScanner() {
        return scanner;
    }
    public BluetoothLeAdvertiser getAdvertiser() {
        return advertiser;
    }
    public BluetoothAdapter getAdapter() {
        return bluetoothAdapter;
    }
    public BluetoothManager getManager() {
        return bluetoothManager;
    }
    public PermissionDispatcher getPermissionDispatcher() {
        return permissionDispatcher;
    }
    public boolean isBLEAdvertiseEnable() {
        return bluetoothAdapter.isMultipleAdvertisementSupported();
    }

    public boolean isBLEScanEnable() {
        return bluetoothAdapter.isOffloadedScanBatchingSupported();
    }

    public boolean isBluetoothEnable() {
        if (bluetoothAdapter == null) {
            return false;
        }

        if (permissionDispatcher.checkPermissions(PermissionGroup.BLUETOOTH)) {
            return false;
        }

        return true;
    }
    public boolean initializeBluetooth() {
        if (!isBLEAdvertiseEnable() || !isBLEScanEnable()) {
            return false;
        }
        scanner = bluetoothAdapter.getBluetoothLeScanner();
        advertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        return true;
    }
}
