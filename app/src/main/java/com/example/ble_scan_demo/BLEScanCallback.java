package com.example.ble_scan_demo;


import android.bluetooth.le.ScanResult;

import java.util.HashMap;

public interface BLEScanCallback {
    public void onScanFinish(HashMap<String, ScanResult> scanResults);
}
