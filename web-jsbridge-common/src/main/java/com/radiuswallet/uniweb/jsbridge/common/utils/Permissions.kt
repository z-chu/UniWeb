package com.radiuswallet.uniweb.jsbridge.common.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

fun IntArray.isPermissionGrantedAll(): Boolean {
    for (i in this) {
        if (i != PackageManager.PERMISSION_GRANTED) {
            return false
        }
    }
    return true
}

fun IntArray.filterGrantedPermissions(): List<Int> {
    return this.filter {
        it == PackageManager.PERMISSION_GRANTED
    }
}

fun Context.checkPermissionGranted(permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(
        this,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}