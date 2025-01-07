package com.example.ble_scan_demo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
    private BLEScanner scanner;

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

        initializeBluetooth();

        findViewById(R.id.scan_button).setOnClickListener(v -> {
            scanBLEDevice();
        });

    }

    // Bluetoothの初期化
    private void initializeBluetooth() {
        if (scanner != null) {
            Log.d("BLE-SCAN","BLE scanner was already initialized!");
            return;
        }
        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth is not supported or not enabled", Toast.LENGTH_SHORT).show();
            return;
        }

        scanner = new BLEScanner(bluetoothManager, bluetoothAdapter, permissionDispatcher);

    }
    private void scanBLEDevice() {
        if (scanner == null) {
            initializeBluetooth();
            return;
        }

        ScanCallback callback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                Log.i("BLE-SCAN","result" + result);
            }
        };

        scanner.startScan(callback,5000);
    }

}