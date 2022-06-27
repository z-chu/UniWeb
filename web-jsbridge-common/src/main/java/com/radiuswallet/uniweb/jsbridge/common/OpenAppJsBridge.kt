package com.radiuswallet.uniweb.jsbridge.common

import com.radiuswallet.uniweb.jsbridge.JsBridgeCallback
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandler
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandlerFactory
import com.radiuswallet.uniweb.jsbridge.createJsBridgeHandlerFactory
import org.json.JSONObject

class OpenAppJsBridge(private val starter: (uri: String) -> Unit) : JsBridgeHandler {

    override fun handler(action: String, data: JSONObject, callBack: JsBridgeCallback): Boolean {
        if (action == ACTION_OPEN_APP) {
            val uri = data.optString("uri")
            if (uri.isNotBlank()) {
                starter.invoke(uri)
            }
            return true
        }
        return false
    }

    companion object {

        private const val ACTION_OPEN_APP = "openApp"

        @JvmStatic
        fun <T> factory(starter: (owner: T, uri: String) -> Unit): JsBridgeHandlerFactory<T> {
            return createJsBridgeHandlerFactory(listOf(ACTION_OPEN_APP)) { owner ->
                OpenAppJsBridge {
                    starter.invoke(owner, it)
                }
            }
        }

    }

}