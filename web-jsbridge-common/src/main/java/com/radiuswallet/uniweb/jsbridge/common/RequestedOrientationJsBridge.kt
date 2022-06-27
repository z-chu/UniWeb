package com.radiuswallet.uniweb.jsbridge.common

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import com.radiuswallet.uniweb.jsbridge.JsBridgeCallback
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandler
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandlerFactory
import com.radiuswallet.uniweb.jsbridge.common.owner.WebViewOwner
import com.radiuswallet.uniweb.jsbridge.createJsBridgeHandlerFactory
import org.json.JSONObject

class RequestedOrientationJsBridge(private val webViewOwner: WebViewOwner) : JsBridgeHandler {

    @SuppressLint("SourceLockedOrientationActivity")
    override fun handler(action: String, data: JSONObject, callBack: JsBridgeCallback): Boolean {
        when (action) {
            SET_HORIZONTAL -> {
                webViewOwner.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            }
            SET_VERTICAL -> {
                webViewOwner.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            }
            else -> {
                return false
            }
        }
        return true
    }

    companion object {

        private const val SET_HORIZONTAL = "setHorizontal"
        private const val SET_VERTICAL = "setVertical"

        @JvmStatic
        fun factory(): JsBridgeHandlerFactory<WebViewOwner> {
            return createJsBridgeHandlerFactory(listOf(SET_HORIZONTAL, SET_VERTICAL)) { owner ->
                RequestedOrientationJsBridge(owner)
            }
        }
    }

}