package com.radiuswallet.uniweb.interceptor

import com.tencent.smtt.sdk.WebView

interface UrlInterceptor {

    fun intercept(
        view: WebView,
        requestCompat: WebResourceRequestCompat,
    ): Boolean

}