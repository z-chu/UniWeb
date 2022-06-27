package com.radiuswallet.uniweb

import android.webkit.JavascriptInterface
import com.radiuswallet.uniweb.jsbridge.JsBridgeDispatcher

class SupportJavascriptInterface {

    private val _jsBridgeDispatchers = ArrayList<JsBridgeDispatcher>()
    val jsBridgeDispatchers: List<JsBridgeDispatcher>
        get() = _jsBridgeDispatchers

    @JavascriptInterface
    fun webcall(string: String) {
        for (jsBridgeDispatcher in jsBridgeDispatchers) {
            if (jsBridgeDispatcher.dispatchWebCall(string)) {
                return
            }
        }
    }

    fun registerJsBridgeDispatcher(jsBridgeDispatcher: JsBridgeDispatcher) {
        _jsBridgeDispatchers.add(jsBridgeDispatcher)
    }

    fun registerJsBridgeDispatcher(jsBridgeDispatchers: List<JsBridgeDispatcher>) {
        _jsBridgeDispatchers.addAll(jsBridgeDispatchers)
    }

    fun unregisterJsBridgeDispatcher(jsBridgeDispatcher: JsBridgeDispatcher) {
        _jsBridgeDispatchers.remove(jsBridgeDispatcher)
    }


}