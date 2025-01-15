package com.example.ble_scan_demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.net.wifi.aware.Characteristics;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;


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
                            Log.d(TAG, "request exchange mtu size 512");
                        } else {
                            Log.d(TAG, "Failed to request MTU exchange");
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
                    Log.i(TAG, "Successfully changed MTU to " + mtu);

                    Log.i(TAG,"Searching for services....");
                    gatt.discoverServices();
                } else {
                    Log.w(TAG, "Error " + status + " encountered while changing MTU");
                }
            }
            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
            @SuppressLint("MissingPermission")
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // 特定のサービスを検索
                    BluetoothGattService service = gatt.getService(java.util.UUID.fromString(BLEConfig.PRIMARY_SERVICE_UUID));
                    if (service != null) {
                        // 特定のキャラクタリスティックを検索
                        BluetoothGattCharacteristic characteristic =
                                service.getCharacteristic(java.util.UUID.fromString(BLEConfig.INFO_CHARACTERISTIC_UUID));
                        if (characteristic != null) {
                            int index = 0;

                            Log.i(TAG, "get Information...");

                            // Characteristicの書き込みモードを指定する
                            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

                            // データをセットする
                            characteristic.setValue("1".getBytes());

                            Log.i("書き込み","書き込み中... index: " + index);
                            boolean success = gatt.writeCharacteristic(characteristic);

                            Log.i("書き込み","Successfully write index!");

                            if (success) {
                                boolean success_read = gatt.readCharacteristic(characteristic);
                                Log.i("success", "Successfully read Data");

                            }
                        } else {
                            Log.i(TAG,"characteristic was not found");
                        }
                    }
                }
            }
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    switch (characteristic.getUuid().toString()) {
                        case BLEConfig.GREETING_CHARACTERISTIC_UUID:
                            // 読み取ったデータを処理
                            byte[] message = characteristic.getValue();
                            Log.d(TAG, "greeting from gatt server : " + new String(message));
                            break;
                        case BLEConfig.INFO_CHARACTERISTIC_UUID:
                            byte[] value = characteristic.getValue();
                            Log.d(TAG, "greeting from gatt server : " + new String(value));
                    }
                } else {
                    Log.e(TAG, "Failed to read characteristic.");
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

    @SuppressLint("MissingPermission")
    private void compareAndRequestData(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().toString().equals(BLEConfig.FILTER_CHARACTERISTIC_UUID)) {
            // フィルターのデータを取得
            gatt.readCharacteristic(characteristic);
        }
    }
}
