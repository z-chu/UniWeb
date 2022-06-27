package com.radiuswallet.uniweb.utils

import android.content.Context
import android.os.Build
import android.text.TextUtils
import com.tencent.smtt.sdk.CookieManager
import com.tencent.smtt.sdk.CookieSyncManager
import com.tencent.smtt.sdk.QbSdk
import com.tencent.smtt.sdk.WebView

fun WebView.clearAllData() {
    this.context.clearAllWebViewCache(true)
}

fun Context.clearAllWebViewCache(isClearCookie: Boolean) {
    QbSdk.clearAllWebViewCache(this, isClearCookie)
}

fun Context.setCookie(cookieMap: Map<String, List<String>>) {
    if (cookieMap.isEmpty()) {
        return
    }
    CookieSyncManager.createInstance(this)
    val cookieManager = CookieManager.getInstance()
    cookieManager.setAcceptCookie(true)
    for (entry in cookieMap.entries) {
        if (TextUtils.isEmpty(entry.key) || entry.value.isEmpty()) {
            continue
        }
        val domain = entry.key
        for (cookie in entry.value) {
            if (!TextUtils.isEmpty(cookie)) {
                cookieManager.setCookie(domain, "$cookie;Domain=$domain;Path=/")
            }
        }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        cookieManager.flush()
    } else {
        CookieSyncManager.getInstance().sync()
    }
}

fun WebView.setCookie(cookieMap: Map<String, List<String>>) {
    this.context.setCookie(cookieMap)
}