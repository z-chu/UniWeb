package com.radiuswallet.uniweb.jsbridge.common

import com.radiuswallet.uniweb.jsbridge.JsBridgeCallback
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandler
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandlerFactory
import com.radiuswallet.uniweb.jsbridge.createJsBridgeHandlerFactory
import org.json.JSONObject

class OpenWebViewJsBridge(private val opener: (url: String, title: String?) -> Unit) :
    JsBridgeHandler {

    override fun handler(action: String, data: JSONObject, callBack: JsBridgeCallback): Boolean {
        if (action == ACTION_OPEN_NEW_WEB_VIEW) {
            val url = data.optString("url")
            if (url.isNotBlank()) {
                val title = data.optString("title")
                if (title.isNotBlank()) {
                    opener.invoke(url, title)
                } else {
                    opener.invoke(url, null)
                }
            }
            return true
        }
        return false
    }

    companion object {

        private const val ACTION_OPEN_NEW_WEB_VIEW = "openNewWebView"

        @JvmStatic
        fun <T> factory(opener: (owner: T, url: String, title: String?) -> Unit): JsBridgeHandlerFactory<T> {
            return createJsBridgeHandlerFactory(
                ACTION_OPEN_NEW_WEB_VIEW
            ) { owner ->
                OpenWebViewJsBridge { url, title ->
                    opener.invoke(owner, url, title)
                }
            }
        }
    }
}