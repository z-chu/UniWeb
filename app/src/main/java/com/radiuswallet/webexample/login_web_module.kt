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
class LoginWebModuleInstaller : WebModuleInstallable {

    override val modules: List<UniWebSubModule>
        get() = listOf(login_web_module)
}

val login_web_module = uniWebSubModule {

    jsBridgeHandler("login") {
        object : JsBridgeHandler {
            override fun handler(
                action: String,
                data: JSONObject,
                callBack: JsBridgeCallback,
            ): Boolean {
                Toast.makeText(it.context, "登录啊登录:$action", Toast.LENGTH_SHORT).show()
                return true
            }

        }
    }


}

