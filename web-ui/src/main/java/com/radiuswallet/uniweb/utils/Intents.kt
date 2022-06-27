package com.radiuswallet.uniweb.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

/**
 * 检查 Intent 是否能正常打开 Activity
 */
@SuppressLint("QueryPermissionsNeeded")
fun Intent.isAvailableActivityIntent(packageManager: PackageManager): Boolean? {
    return try {
        packageManager.queryIntentActivities(this, 0).isNotEmpty()
    } catch (e: Exception) {
        null
    }
}

/**
 * 检查 Intent 是否能正常打开 Activity
 */
fun Intent.isAvailableActivityIntent(context: Context): Boolean? {
    return this.isAvailableActivityIntent(context.packageManager)
}

/**
 * 在检查 Intent 能正常打开 Activity 后，再启动 Intent
 */
fun Context.startActivityCheckAvailability(intent: Intent): Boolean {
    if ((intent.isAvailableActivityIntent(this) != false)) {
        try {
            startActivity(intent)
            return true
        } catch (e: Exception) {
        }
    }
    return false
}

/**
 * 在浏览器中打开
 */
fun Context.openInBrowser(uri: Uri): Boolean {
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.addCategory(Intent.CATEGORY_BROWSABLE)
    if (this !is Activity) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return startActivityCheckAvailability(intent)
}

/**
 * 在浏览器中打开
 */
fun Context.openInBrowser(url: String): Boolean {
    return this.openInBrowser(Uri.parse(url))
}

fun Context.openActionView(uri: Uri): Boolean {
    val intent = Intent(Intent.ACTION_VIEW, uri)
    if (this !is Activity) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        startActivity(intent)
        return true
    } catch (e: Exception) {
    }

    return false
}

fun Context.openActionView(url: String): Boolean {
    return this.openActionView(Uri.parse(url))
}