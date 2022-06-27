package com.radiuswallet.uniweb.jsbridge

import android.os.Looper
import android.text.TextUtils
import android.webkit.ValueCallback
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.Executor


class ExecuteJsBridgeCallback(
    val action: String,
    val javascriptCaller: JavascriptCaller,
    val methodName: String,
    val mainExecutor: Executor,
    val finallyCallData: String?,
) : JsBridgeCallback {

    private var isCalled = false

    fun finallyCall() {
        if (!isCalled) {
            if (finallyCallData != null) {
                doOnCallback(null, JsBridgeParam(finallyCallData))
            }
        }
    }

    override fun onCallback(param: String?) {
        val data = param ?: finallyCallData
        if (data != null) {
            doOnCallback(null, JsBridgeParam(data))
        }
    }

    override fun onCallback(vararg params: String) {
        val toTypedArray = params.map { JsBridgeParam(it) }.toTypedArray()
        doOnCallback(null, *toTypedArray)
    }

    override fun onCallback(callback: ValueCallback<String>, vararg params: String) {
        val toTypedArray = params.map { JsBridgeParam(it) }.toTypedArray()
        doOnCallback(callback, *toTypedArray)
    }

    override fun onCallback(callback: ValueCallback<String>?, vararg params: JsBridgeParam) {
        doOnCallback(callback, *params)
    }

    private fun doOnCallback(callback: ValueCallback<String>?, vararg params: JsBridgeParam) {
        isCalled = true
        val sb = StringBuilder()
        sb.append("javascript:").append(methodName)
        if (params.isEmpty()) {
            sb.append("()")
        } else {
            sb.append("(").append(concat(*params)).append(")")
        }
        val javascript = sb.toString()
        if (Looper.getMainLooper().thread === Thread.currentThread()) {
            doCallJavascript(javascript, callback)
        } else {
            mainExecutor.execute {
                doCallJavascript(javascript, callback)
            }
        }
    }

    private fun doCallJavascript(javascript: String, callback: ValueCallback<String>?) {
        Timber.tag(TAG_WEB_LOG).d("doCallJavascript-> %s", javascript.subTimberLog())
        javascriptCaller.callJs(javascript) { value ->
            callback?.onReceiveValue(value ?: "")
        }
    }

    private fun concat(vararg params: JsBridgeParam): String {
        val mStringBuilder = StringBuilder()
        for (i in params.indices) {
            val param = params[i]
            val value = param.value
            if (param.isRaw) {
                if (Timber.treeCount > 0) {
                    if (!value.isJson()) {//如果原始数据不是合法的 JsonObject ，将发出警告
                        Timber.w(
                            "web call:action=$action,callback=$methodName," +
                                    " The following callback parameter are not jsonObject, but raw data is used ---> $value"
                        )
                    }
                }
                mStringBuilder.append(value)
            } else {
                mStringBuilder.append("'")
                mStringBuilder.append(value)
                mStringBuilder.append("'")
            }
            if (i != params.size - 1) {
                mStringBuilder.append(",")
            }
        }
        return mStringBuilder.toString()
    }


    private fun String.isJson(): Boolean {
        if (TextUtils.isEmpty(this)) {
            return false
        }
        return try {
            if (this.startsWith("[")) {
                JSONArray(this)
            } else {
                JSONObject(this)
            }
            true
        } catch (ignore: JSONException) {
            false
        }
    }
}