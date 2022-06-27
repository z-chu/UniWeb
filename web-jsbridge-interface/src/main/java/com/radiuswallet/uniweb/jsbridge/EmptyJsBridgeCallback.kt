package com.radiuswallet.uniweb.jsbridge

import android.webkit.ValueCallback
import timber.log.Timber

/**
 *  [JsBridgeCallback] 的空实现，只打印日志，不做任何事情
 */
class EmptyJsBridgeCallback(
    val action: String,
    val finallyCallData: String?,
) : JsBridgeCallback {

    override fun onCallback(param: String?) {
        val data = param ?: finallyCallData
        if (data != null) {
            onCallback(data)
        }
    }

    override fun onCallback(callback: ValueCallback<String>, vararg params: String) {
        onCallback(*params)
    }

    override fun onCallback(vararg params: String) {
        if (Timber.treeCount > 0) {
            val toTypedArray = params.map { JsBridgeParam(it) }.toTypedArray()
            onCallback(null, *toTypedArray)
        }
    }

    override fun onCallback(callback: ValueCallback<String>?, vararg params: JsBridgeParam) {
        if (Timber.treeCount > 0) {
            val localData = concat(*params)
            Timber.tag(TAG_WEB_LOG)
                .d("action:$action,empty callback-> %s", localData.subTimberLog())
        }
    }

    private fun concat(vararg params: JsBridgeParam): String {
        val mStringBuilder = StringBuilder()
        for (i in params.indices) {
            val param = params[i]
            if (param.isRaw) {
                mStringBuilder.append(param.value)
            } else {
                mStringBuilder.append("'")
                mStringBuilder.append(param.value)
                mStringBuilder.append("'")
            }
            if (i != params.size - 1) {
                mStringBuilder.append(",")
            }
        }
        return mStringBuilder.toString()
    }

}