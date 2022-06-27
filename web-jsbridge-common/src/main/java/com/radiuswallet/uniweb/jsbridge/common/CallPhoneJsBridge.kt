package com.radiuswallet.uniweb.jsbridge.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.TelephonyManager
import android.widget.Toast
import com.radiuswallet.uniweb.jsbridge.JsBridgeCallback
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandler
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandlerFactory
import com.radiuswallet.uniweb.jsbridge.common.owner.WebViewOwner
import com.radiuswallet.uniweb.jsbridge.createJsBridgeHandlerFactory
import org.json.JSONObject

class CallPhoneJsBridge(private val context: Context) : JsBridgeHandler {
    override fun handler(action: String, data: JSONObject, callBack: JsBridgeCallback): Boolean {
        if (action == CALL_PHONE) {
            val optString = data.optString("phone_no").trim()
            if (optString.isNotBlank()) {
                val tm = context.getSystemService(Context.TELEPHONY_SERVICE)
                if (tm is TelephonyManager) {
                    if (tm.simState != TelephonyManager.SIM_STATE_ABSENT) {
                        //不需要额外任何权限
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$optString"))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, R.string.phone_call_fail, Toast.LENGTH_SHORT).show()
                    }
                    return true
                }
            }
        }
        return false
    }

    companion object {
        private const val CALL_PHONE = "callPhone"

        @JvmStatic
        fun <T> factory(context: Context): JsBridgeHandlerFactory<T> {
            return createJsBridgeHandlerFactory(CALL_PHONE) {
                com.radiuswallet.uniweb.jsbridge.common.CallPhoneJsBridge(context)
            }
        }

        @JvmStatic
        fun factory(): JsBridgeHandlerFactory<WebViewOwner> {
            return createJsBridgeHandlerFactory(CALL_PHONE) { owner ->
                com.radiuswallet.uniweb.jsbridge.common.CallPhoneJsBridge(owner.context)
            }
        }

    }

}