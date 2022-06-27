package com.radiuswallet.uniweb.jsbridge

import android.content.Context
import android.os.Build
import com.radiuswallet.uniweb.jsbridge.common.owner.WebViewOwner
import com.radiuswallet.uniweb.utils.AppUtils
import org.json.JSONObject
import timber.log.Timber

internal class InternalAppInfoJsBridge(
    private val context: Context,
    private val extraDataProvider: (() -> Map<String, String>),
) : JsBridgeHandler {

    override fun handler(action: String, data: JSONObject, callBack: JsBridgeCallback): Boolean {
        if (action == GET_APP_INFO) {
            val jsonObject = JSONObject()
            try {
                jsonObject.put("os", "android")
                jsonObject.put("version", AppUtils.getVersionName(context))
                jsonObject.put("revision", AppUtils.getVersionCode(context))
                jsonObject.put("_t", (System.currentTimeMillis() / 1000).toString())
                jsonObject.put("_su", AppUtils.getStartUuid())
                jsonObject.put("device_type", Build.MODEL)
                jsonObject.put("app_id", context.applicationContext.packageName)
                val extraData = extraDataProvider.invoke()
                for (entry in extraData) {
                    jsonObject.put(entry.key, entry.value)
                }
            } catch (e: Exception) {
                Timber.tag(TAG_WEB_LOG).e(e)
            }
            callBack.onCallback(jsonObject.toString())
            return true
        }
        return false
    }

    companion object {

        private const val GET_APP_INFO = "getAppInfo"

        /**
         * @param extraDataProvider 还有部分与业务绑定的信息参数由外部来提供，
         *                          包括：token、device_id、app_type、app_channel
         */
        @JvmStatic
        fun <T> factory(
            context: Context,
            extraDataProvider: ((T) -> Map<String, String>),
        ): JsBridgeHandlerFactory<T> {
            return createJsBridgeHandlerFactory(GET_APP_INFO) { owner ->
                InternalAppInfoJsBridge(context) {
                    extraDataProvider.invoke(owner)
                }
            }
        }

        /**
         * @param extraDataProvider 还有部分与业务绑定的信息参数由外部来提供，
         *                          包括：token、device_id、app_type、app_channel
         */
        @JvmStatic
        fun factory(extraDataProvider: ((WebViewOwner) -> Map<String, String>)): JsBridgeHandlerFactory<WebViewOwner> {
            return createJsBridgeHandlerFactory(GET_APP_INFO) {
                factory(it.context, extraDataProvider).create(it)
            }
        }
    }
}