package com.radiuswallet.webexample

import android.widget.Toast
import com.radiuswallet.uniweb.jsbridge.JsBridgeCallback
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandler
import com.radiuswallet.uniweb.module.uniWebModule
import org.json.JSONObject

/**
 * 单独的一个 webModule
 */
val separateWebModule = uniWebModule {
    jsBridgeHandler(listOf("webModuleTest1", "webModuleTest2", "webModuleTest3")) {
        object : JsBridgeHandler {
            override fun handler(
                action: String,
                data: JSONObject,
                callBack: JsBridgeCallback,
            ): Boolean {
                Toast.makeText(it.context,
                    "单独的 main webModule action:" + action + "被调用了",
                    Toast.LENGTH_SHORT).show()

                return true
            }

        }
    }
}