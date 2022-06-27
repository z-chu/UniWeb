package com.radiuswallet.uniweb.jsbridge.common

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.radiuswallet.uniweb.jsbridge.JsBridgeCallback
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandler
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandlerFactory
import com.radiuswallet.uniweb.jsbridge.common.owner.WebViewOwner
import com.radiuswallet.uniweb.jsbridge.createJsBridgeHandlerFactory
import org.json.JSONObject

class OpenWifiSettingJsBridge(private val context: Context) : JsBridgeHandler {

    override fun handler(action: String, data: JSONObject, callBack: JsBridgeCallback): Boolean {
        if (action == ACTION_OPEN_WIFI_SETTING) {
            try {
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    return true
                }
            } catch (e: Exception) {
            }

        }
        return false
    }

    companion object {
        const val ACTION_OPEN_WIFI_SETTING = "openWifiSetting"

        @JvmStatic
        fun factory(): JsBridgeHandlerFactory<WebViewOwner> {
            return createJsBridgeHandlerFactory(ACTION_OPEN_WIFI_SETTING) {
                OpenWifiSettingJsBridge(it.context)
            }
        }
    }
}