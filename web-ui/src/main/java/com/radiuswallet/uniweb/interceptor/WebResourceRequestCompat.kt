package com.radiuswallet.uniweb.interceptor

import com.tencent.smtt.export.external.interfaces.WebResourceRequest

class WebResourceRequestCompat {
    val url: String
    val request: WebResourceRequest?

    constructor(url: String) {
        this.url = url
        this.request = null
    }

    constructor(request: WebResourceRequest) {
        this.url = request.url.toString()
        this.request = request
    }

}