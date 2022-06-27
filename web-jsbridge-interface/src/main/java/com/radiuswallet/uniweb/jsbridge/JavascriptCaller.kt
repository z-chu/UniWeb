package com.radiuswallet.uniweb.jsbridge

import android.webkit.ValueCallback
import androidx.annotation.MainThread

interface JavascriptCaller {

    @MainThread
    fun callJs(js: String, callback: ValueCallback<String>?)

}