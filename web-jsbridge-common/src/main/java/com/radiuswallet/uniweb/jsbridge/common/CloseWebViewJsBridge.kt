package com.radiuswallet.uniweb.jsbridge.common

import com.radiuswallet.uniweb.jsbridge.JsBridgeCallback
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandler
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandlerFactory
import com.radiuswallet.uniweb.jsbridge.common.owner.WebViewOwner
import com.radiuswallet.uniweb.jsbridge.createJsBridgeHandlerFactory
import org.json.JSONObject

class CloseWebViewJsBridge(private val webViewOwner: WebViewOwner) : JsBridgeHandler {

    override fun handler(action: String, data: JSONObject, callBack: JsBridgeCallback): Boolean {
        if (action == ACTION_CLOSE_WEB_VIEW) {
            webViewOwner.closeCurrentWebView()
            return true
        }
        return false
    }

    companion object {

        private const val ACTION_CLOSE_WEB_VIEW = "closeWebView"

        @JvmStatic
        fun factory(): JsBridgeHandlerFactory<WebViewOwner> {
            return createJsBridgeHandlerFactory(
                ACTION_CLOSE_WEB_VIEW
            ) { owner ->
                CloseWebViewJsBridge(owner)
            }
        }
    }
}