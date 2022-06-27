package com.radiuswallet.uniweb.jsbridge.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.radiuswallet.uniweb.jsbridge.JsBridgeCallback
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandler
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandlerFactory
import com.radiuswallet.uniweb.jsbridge.common.owner.WebViewOwner
import com.radiuswallet.uniweb.jsbridge.createJsBridgeHandlerFactory
import org.json.JSONObject

class ClipboardJsBridge(private val context: Context) : JsBridgeHandler {

    override fun handler(action: String, data: JSONObject, callBack: JsBridgeCallback): Boolean {
        if (action == PASTE) {
            val optString = data.optString("content")
            if (optString.isNotBlank()) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
                if (clipboard is ClipboardManager) {
                    val clip = ClipData.newPlainText("simple text copy", optString)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, R.string.message_copied, Toast.LENGTH_SHORT).show()
                    return true
                }
            }
        }
        return false
    }

    companion object {
        private const val PASTE = "paste"

        @JvmStatic
        fun <T> factory(context: Context): JsBridgeHandlerFactory<T> {
            return createJsBridgeHandlerFactory(PASTE) {
                ClipboardJsBridge(context)
            }
        }

        @JvmStatic
        fun factory(): JsBridgeHandlerFactory<WebViewOwner> {
            return createJsBridgeHandlerFactory(PASTE) { owner ->
                ClipboardJsBridge(owner.context)
            }
        }

    }
}