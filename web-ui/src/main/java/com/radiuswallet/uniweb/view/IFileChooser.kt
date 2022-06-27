package com.radiuswallet.uniweb.view

import android.net.Uri
import com.tencent.smtt.sdk.ValueCallback
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebView

interface IFileChooser {

    /**
     * Android  >= 4.1
     */
    fun openFileChooser(valueCallback: ValueCallback<Uri>?, acceptType: String?, capture: String?)

    /**
     * Android  >= 5.0
     */
    fun onShowFileChooser(
        webView: WebView?,
        valueCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: WebChromeClient.FileChooserParams?,
    ): Boolean
}