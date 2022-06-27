package com.radiuswallet.uniweb.jsbridge.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.radiuswallet.uniweb.jsbridge.JsBridgeCallback
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandler
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandlerFactory
import com.radiuswallet.uniweb.jsbridge.common.owner.WebViewOwner
import com.radiuswallet.uniweb.jsbridge.createJsBridgeHandlerFactory
import org.json.JSONObject
import timber.log.Timber

class GoBrowserJsBridge private constructor(private val context: Context) : JsBridgeHandler {

    override fun handler(action: String, data: JSONObject, callBack: JsBridgeCallback): Boolean {
        //打开外部浏览器，防止后续的强更或者兼容问题，留给h5的后门
        val url = data.optString("url")
        if (url.isNotBlank()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        return true
    }

    companion object {
        private const val ACTION_GO_BROWSER = "goBrowser"

        @JvmStatic
        fun factory(): JsBridgeHandlerFactory<WebViewOwner> {
            return createJsBridgeHandlerFactory(ACTION_GO_BROWSER) {
                GoBrowserJsBridge(it.context)
            }
        }
    }
}