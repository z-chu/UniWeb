package com.radiuswallet.webexample

import android.widget.Toast
import com.google.auto.service.AutoService
import com.radiuswallet.uniweb.jsbridge.JsBridgeCallback
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandler
import com.radiuswallet.uniweb.module.UniWebSubModule
import com.radiuswallet.uniweb.module.WebModuleInstallable
import com.radiuswallet.uniweb.module.uniWebSubModule
import org.json.JSONObject


@Suppress("unused")
@AutoService(WebModuleInstallable::class)
class HomeWebModuleInstaller : WebModuleInstallable {

    override val modules: List<UniWebSubModule>
        get() = listOf(home_web_module)
}

val home_web_module = uniWebSubModule {

    jsBridgeHandler(listOf("test1", "test2")) {
        object : JsBridgeHandler {
            override fun handler(
                action: String,
                data: JSONObject,
                callBack: JsBridgeCallback,
            ): Boolean {
                Toast.makeText(it.context, "action:" + action + "被调用了", Toast.LENGTH_SHORT).show()
                return true
            }
        }
    }

}

