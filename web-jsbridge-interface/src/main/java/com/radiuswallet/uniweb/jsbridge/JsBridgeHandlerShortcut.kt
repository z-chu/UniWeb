package com.radiuswallet.uniweb.jsbridge

import org.json.JSONObject

abstract class JsBridgeHandlerShortcut<T> : JsBridgeHandler, JsBridgeHandlerFactory<T> {
    private var webViewOwner: Any? = null

    final override fun handler(
        action: String,
        data: JSONObject,
        callBack: JsBridgeCallback,
    ): Boolean {
        return handler(webViewOwner as T, action, data, callBack)
    }

    abstract fun handler(
        webViewOwner: T,
        action: String,
        data: JSONObject,
        callBack: JsBridgeCallback,
    ): Boolean


    final override fun create(webViewOwner: T): JsBridgeHandler {
        this.webViewOwner = webViewOwner
        return this
    }

}