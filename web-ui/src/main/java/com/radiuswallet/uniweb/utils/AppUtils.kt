package com.radiuswallet.uniweb.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

/**
 * 仅供内部使用
 */
internal object AppUtils {
    /**
     * get app version name
     */
    fun getVersionName(context: Context): String {
        var verName = ""
        try {
            val packageName = context.packageName
            verName = context.packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e)
        }
        return verName
    }

    /**
     * get app version code
     */
    fun getVersionCode(context: Context): Int {
        var verCode = -1
        try {
            val packageName = context.packageName
            verCode = context.packageManager.getPackageInfo(packageName, 0).versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e)
        }
        return verCode
    }


    @SuppressLint("SimpleDateFormat")
    fun getStartUuid(): String {
        var random = java.lang.StringBuilder()
        var i = 0
        while (i < 10) {
            random.append((Math.random() * 10).toInt())
            if (random.toString() == "0") {
                random = java.lang.StringBuilder()
                i--
            }
            i++
        }
        val format = SimpleDateFormat("yyMMddHHmmssSSS")
        val time = format.format(Date(System.currentTimeMillis()))
        return time + "1000000" + random
    }
}