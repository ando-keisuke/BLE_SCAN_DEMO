package com.example.ble_scan_demo;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
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

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;

    private PermissionDispatcher permissionDispatcher;
    private BLEScanner scanner;

    private boolean scanning = false;

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

//        InfoFilter filter = new InfoFilter();
//        filter.test();


        // すべての権限をリクエストする
        permissionDispatcher = new PermissionDispatcher(this);

        for (PermissionGroup group: PermissionGroup.values()) {
            if (!permissionDispatcher.checkPermissions(group)) {
                permissionDispatcher.requestPermissions(group);
            }
        }

        initializeBluetooth();

        BLEClient client = new BLEClient(this,bluetoothManager,bluetoothAdapter,permissionDispatcher);

        findViewById(R.id.scan_button).setOnClickListener(v -> {
            scanning = true;

            new Thread(() -> {
                client.scanAndFilterDevice();
                scanning = false;

            }).start();
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

        scanner = new BLEScanner(bluetoothAdapter, permissionDispatcher);
    }

    @SuppressLint("MissingPermission")
    private void scanBLEDevice() {

        try {
            HashMap<String, BluetoothDevice> deviceMap = scanner.scanDevice(5, null, null);

            // 見つけたデバイスの数を出力
            Log.i("BLE-SCAN", "Found " + deviceMap.size() + " devices");
            for (Map.Entry<String, BluetoothDevice> entry: deviceMap.entrySet()) {
                Log.i("BLE-SCAN", "Device: " + entry.getValue().getName() + " (" + entry.getKey() + ")");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}