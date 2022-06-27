package com.radiuswallet.uniweb.jsbridge.common

import android.content.Context
import android.content.pm.PackageManager
import com.radiuswallet.uniweb.jsbridge.JsBridgeCallback
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandler
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandlerFactory
import com.radiuswallet.uniweb.jsbridge.common.owner.WebViewOwner
import com.radiuswallet.uniweb.jsbridge.createJsBridgeHandlerFactory
import org.json.JSONObject

/**
 * 自 Android 11（API 级别 30）起，大多数用户安装的应用程序默认不可见。在您的清单中，您必须静态声明您将获取有关哪些应用程序的信息，如下所示：
 *
 *  <queries>
 *      <package android:name="com.tencent.mm"/>
 *  </queries>
 *
 *  如果没有在清单文件中声明，即使安装了微信，该 JsBridgeHandler 回调的依然是 false
 */
class CheckWxInstallJsBridge(private val context: Context) : JsBridgeHandler {
    override fun handler(action: String, data: JSONObject, callBack: JsBridgeCallback): Boolean {
        if (CHECK_WX_INSTALL == action) {
            val checkState = checkWxInstall(context)
            callBack.onCallback(checkState)
            return true
        }
        return false
    }

    private fun checkWxInstall(context: Context): String {
        val result = checkWXAppInstalled(context)
        return if (result) {
            "1"
        } else {
            "0"
        }
    }

    /**
     * 自 Android 11（API 级别 30）起，大多数用户安装的应用程序默认不可见。在您的清单中，您必须静态声明您将获取有关哪些应用程序的信息，如下所示：
     *  <queries>
     *      <package android:name="com.tencent.mm"/>
     *  </queries>
     *  如果安装了微信，这将在 Android 11 上返回 true，因为我们在上面的清单中声明了它。
     *  如果没有在清单文件中声明，即使安装了微信，这也会在 Android 11 上返回 false
     */
    fun checkWXAppInstalled(context: Context): Boolean {
        return reflectCheckWXAppInstalled(context)
            ?: try {
                context.packageManager.getPackageInfo("com.tencent.mm", 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
    }

    /**
     * 优先使用微信 sdk 中的检查 API
     */
    private fun reflectCheckWXAppInstalled(context: Context): Boolean? {
        try {
            val aRouterClass = Class.forName("com.tencent.mm.opensdk.openapi.WXAPIFactory")
            val createWXAPI =
                aRouterClass.getMethod("createWXAPI", Context::class.java, String::class.java)
            val wxAPI = createWXAPI.invoke(null, context, null)
            val wxAPIClass = Class.forName("com.tencent.mm.opensdk.openapi.IWXAPI")
            val method = wxAPIClass.getMethod("isWXAppInstalled")
            val invoke = method.invoke(wxAPI)
            return invoke == true
        } catch (e: Throwable) {
            //ignore throwable
        }
        return null
    }


    companion object {
        private const val CHECK_WX_INSTALL = "checkWxInstall"

        @JvmStatic
        fun <T> factory(context: Context): JsBridgeHandlerFactory<T> {
            return createJsBridgeHandlerFactory(CHECK_WX_INSTALL) {
                CheckWxInstallJsBridge(context)
            }
        }

        @JvmStatic
        fun factory(): JsBridgeHandlerFactory<WebViewOwner> {
            return createJsBridgeHandlerFactory(CHECK_WX_INSTALL) {
                CheckWxInstallJsBridge(it.context)
            }
        }

    }
}