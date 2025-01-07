package com.example.ble_scan_demo.psermission;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

// PermissionDispatcherクラス
// 権限の確認と取得を担当する
// 権限ごとではなく、機能ごとに必要な権限をまとめて管理する
// 機能と権限の対応はPermissionGroupクラスで定義する
public class PermissionDispatcher {
    // Activityを保持する
    // 権限を取得するために必要
    private final Activity activity;

    // コンストラクタ
    // アクティビティを受け取る
    public PermissionDispatcher(Activity activity) {
        this.activity = activity;
        Log.d("PERM-DBG", "PermissionDispatcher: initialized!");
    }

    // 権限の確認
    public boolean checkPermissions(PermissionGroup group) {
        Log.d("PERM-DBG", "PermissionDispatcher: check permissions! group:" + group.name());
        // パーミッションを確認する
        for (String permission : group.getPermissions()) {
            if (ActivityCompat.checkSelfPermission(this.activity, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.e("PERM-DBG", "PermissionDispatcher: Permission denied! permission: " + permission);
                return false;
            }
        }
        Log.d("PERM-DBG", "PermissionDispatcher: Permission granted!");
        return true;
    }

    // パーミッションのリクエスト
    public void requestPermissions(PermissionGroup group) {
        Log.d("PERM-DBG", "PermissionDispatcher: request permissions! group: " + group.name());
        ActivityCompat.requestPermissions(this.activity, group.getPermissions(), 1);
    }
}
