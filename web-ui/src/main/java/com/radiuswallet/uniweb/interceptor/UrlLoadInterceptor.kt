package com.radiuswallet.uniweb.interceptor


interface UrlLoadInterceptor {

    fun intercept(
        urlLoader: UrlLoader,
        originalUrl: String,
        headers: Map<String, String>,
    ): Boolean

    interface UrlLoader {

        fun loadUrl(url: String, headers: Map<String, String>)

    }
}