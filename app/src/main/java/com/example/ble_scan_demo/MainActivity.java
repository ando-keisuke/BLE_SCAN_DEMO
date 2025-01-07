package com.example.ble_scan_demo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ble_scan_demo.psermission.PermissionDispatcher;
import com.example.ble_scan_demo.psermission.PermissionGroup;

public class MainActivity extends AppCompatActivity {
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    boolean scanning = false;

    private PermissionDispatcher permissionDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // すべての権限をリクエストする
        permissionDispatcher = new PermissionDispatcher(this);

        for (PermissionGroup group: PermissionGroup.values()) {
            if (!permissionDispatcher.checkPermissions(group)) {
                permissionDispatcher.requestPermissions(group);
            }
        }

        findViewById(R.id.scan_button).setOnClickListener(v -> {
            scanBLEDevice();
        });
    }

    // Bluetoothの初期化
    private void initializeBluetooth() {
        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

    }
    private void scanBLEDevice() {
        if (scanning) {
            return;
        }

        // Bluetoothがサポートされているか確認
        if (bluetoothAdapter == null) {
            Log.d("BLE_DBG", "Bluetooth was not supported in on device.");
            Toast.makeText(this, "Bluetoothがサポートされていません", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bluetoothが有効か確認
        if (!bluetoothAdapter.isEnabled()) {
            Log.d("BLE_DBG", "please activate bluetooth");
            Toast.makeText(this, "Bluetoothを有効にしてください", Toast.LENGTH_SHORT).show();
            return;
        }

        BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();

        // デバイスがBluetooth LE scannerをサポートしているか確認
        if (scanner == null) {
            Log.d("BLD_DBG", "BLE Scanning is not supported on this device.");
            Toast.makeText(this, "このデバイスはBluetooth LE Scannerをサポートしていません", Toast.LENGTH_SHORT).show();
            return;
        }

        // スキャンの開始
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        ScanCallback scanCallback = new ScanCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                if (!permissionDispatcher.checkPermissions(PermissionGroup.BLUETOOTH)) {
                    Log.e("BLE-SCAN-DBG", "ScanCallback: Bluetooth Permission denied!");
                }

                super.onScanResult(callbackType, result);
                if (result == null) {
                    Log.e("BLE_SCAN", "onScanResult: result is null");
                    return;
                }
                Log.d("BLE_SCAN", "onScanResult: " + result.getDevice().getName() + " : " + result.getRssi());
            }
        };

        scanning = true;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                permissionDispatcher.checkPermissions(PermissionGroup.BLUETOOTH);
                scanner.stopScan(scanCallback);
                scanning = false;
            }
            }, 10000);

        scanner.startScan(scanCallback);

    }

}