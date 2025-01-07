package com.example.ble_scan_demo.psermission;

import android.Manifest;

public enum PermissionGroup {
    BLUETOOTH(new String[]{
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_SCAN
    }),
    LOCATION(new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    });

    private final String[] permissions;

    PermissionGroup(String[] permissions) {
        this.permissions = permissions;
    }

    public String[] getPermissions() {
        return permissions;
    }
}
