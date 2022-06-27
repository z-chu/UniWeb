package com.radiuswallet.uniweb.interceptor

import android.util.Patterns
import com.radiuswallet.uniweb.utils.openActionView
import com.tencent.smtt.sdk.WebView

/**
 * 外部 app 的 Url Scheme 处理
 */
class OthersSchemeInterceptor : UrlInterceptor {

    override fun intercept(view: WebView, requestCompat: WebResourceRequestCompat): Boolean {
        val url = requestCompat.url
        if (url.startsWith(HTTP_SCHEME) || url.startsWith(HTTPS_SCHEME)) {
            return false
        }
        if (!Patterns.WEB_URL.matcher(url).matches()) {
            val context = view.context
            if (!context.openActionView(url)) {
                //Toast.makeText(context, "找不到能打开该链接的应用", Toast.LENGTH_SHORT).show()
            }
            return true
        }
        return false
    }

    companion object {
        const val HTTP_SCHEME = "http://"
        const val HTTPS_SCHEME = "https://"
    }

}