package com.example.ble_scan_demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;


public class BLEConnector {
    private static final String TAG = "BLEConnector";

    private final Activity activity;
    private BluetoothGatt bluetoothGatt;

    public BLEConnector(Activity activity) {
        this.activity = activity;
    }

    @SuppressLint("MissingPermission")
    public void connectGattServer(BluetoothDevice device) {
        // コールバックの定義
        BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.i(TAG, "Connected to GATT server.");
                        // Exchange MTU Request
                        // 512Bまで拡張を要求
                        if (gatt.requestMtu(512)) {
                            Log.d(TAG, "Requested MTU successfully");
                            gatt.discoverServices();
                        } else {
                            Log.d(TAG, "Failed to request MTU");
                            gatt.disconnect();
                        }
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.i(TAG, "Disconnected from GATT server.");
                    }
                } else {
                    Log.w(TAG, "Error " + status + " encountered for " + newState + ". Disconnecting...");
                    gatt.disconnect();
                }
            }
            @SuppressLint("MissingPermission")
            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "MTU changed to " + mtu);
                } else {
                    Log.w(TAG, "Error " + status + " encountered while changing MTU");
                }
            }
            @SuppressLint("MissingPermission")
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // 特定のサービスを検索
                    BluetoothGattService service = gatt.getService(java.util.UUID.fromString(BLEConfig.PRIMARY_SERVICE_UUID));
                    if (service != null) {
                        // 特定のキャラクタリスティックを検索
                        BluetoothGattCharacteristic characteristic =
                                service.getCharacteristic(java.util.UUID.fromString(BLEConfig.FILTER_CHARACTERISTIC_UUID));
                        if (characteristic != null) {
                            // フィルターを取得
                            gatt.readCharacteristic(characteristic);
                        }
                    }
                }
            }

        };

        //   GATT接続の開始
        bluetoothGatt = device.connectGatt(
                activity,
                false,
                gattCallback,
                BluetoothDevice.TRANSPORT_LE);
    }

    private void compareAndRequestData(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().toString().equals(BLEConfig.FILTER_CHARACTERISTIC_UUID)) {
            // フィルターのデータを取得
            gatt.readCharacteristic(characteristic);
        }
    }
}
