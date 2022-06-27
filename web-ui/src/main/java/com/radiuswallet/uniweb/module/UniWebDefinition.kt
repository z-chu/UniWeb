package com.radiuswallet.uniweb.module

import androidx.fragment.app.Fragment
import com.radiuswallet.uniweb.interceptor.UrlInterceptor
import com.radiuswallet.uniweb.interceptor.UrlLoadInterceptor
import com.radiuswallet.uniweb.jsbridge.JavascriptCaller
import com.radiuswallet.uniweb.jsbridge.JsBridgeDispatcher
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandler
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandlerFactory
import com.radiuswallet.uniweb.jsbridge.common.owner.WebViewOwner
import com.radiuswallet.uniweb.jsbridge.createJsBridgeHandlerFactory
import com.tencent.smtt.sdk.WebSettings

open class UniWebDefinition(
    val targetComponent: Fragment,
    val webViewOwner: WebViewOwner,
) {

    internal var extraDataFunc: () -> Map<String, String> = { emptyMap() }
    internal var cookieMapFunc: () -> Map<String, List<String>> = { emptyMap() }
    internal var webSettingsFunc: (url: String, webSettings: WebSettings) -> Unit = { _, _ -> }
    internal var headersFunc: (url: String, headers: Map<String, String>) -> Map<String, String> =
        { _, _ -> emptyMap() }

    internal val urlLoadInterceptors: ArrayList<UrlLoadInterceptor> = ArrayList()
    internal val urlInterceptors: ArrayList<UrlInterceptor> = ArrayList()

    private val jsBridgeDispatcherDefinitions =
        ArrayList<(JavascriptCaller) -> JsBridgeDispatcher>()

    internal fun createJsBridgeDispatchers(javascriptCaller: JavascriptCaller): List<JsBridgeDispatcher> {
        val jsBridgeDispatchers = ArrayList<JsBridgeDispatcher>()
        for (jsBridgeDispatcherDefinition in jsBridgeDispatcherDefinitions) {
            jsBridgeDispatchers.add(jsBridgeDispatcherDefinition.invoke(javascriptCaller))
        }
        return jsBridgeDispatchers
    }

    private val uiThreadJsBridgeDispatcherDefinitions =
        ArrayList<(JavascriptCaller) -> JsBridgeDispatcher>()

    internal fun createUiThreadJsBridgeDispatchers(javascriptCaller: JavascriptCaller): List<JsBridgeDispatcher> {
        val jsBridgeDispatchers = ArrayList<JsBridgeDispatcher>()
        for (jsBridgeDispatcherDefinition in uiThreadJsBridgeDispatcherDefinitions) {
            jsBridgeDispatchers.add(jsBridgeDispatcherDefinition.invoke(javascriptCaller))
        }
        return jsBridgeDispatchers
    }

    private val jsBridgeFactoriesDefinitions =
        ArrayList<() -> List<JsBridgeHandlerFactory<WebViewOwner>>>()

    internal fun createJsBridgeFactories(): List<JsBridgeHandlerFactory<WebViewOwner>> {
        val jsBridgeFactories = ArrayList<JsBridgeHandlerFactory<WebViewOwner>>()
        for (jsBridgeFactoryDefinition in jsBridgeFactoriesDefinitions) {
            jsBridgeFactories.addAll(jsBridgeFactoryDefinition.invoke())
        }
        return jsBridgeFactories
    }

    fun webSettings(webSettingsFunc: (url: String, webSettings: WebSettings) -> Unit) {
        this.webSettingsFunc = webSettingsFunc
    }

    fun patchWebSettings(patchWebSettingsFunc: (url: String, webSettings: WebSettings) -> Unit) {
        val oldWebSettingsFunc = this.webSettingsFunc
        this.webSettingsFunc = { url, webSettings ->
            oldWebSettingsFunc.invoke(url, webSettings)
            patchWebSettingsFunc.invoke(url, webSettings)
        }
    }

    fun appInfo(extraDataFunc: () -> Map<String, String>) {
        this.extraDataFunc = extraDataFunc
    }

    fun cookies(cookieMapFunc: () -> Map<String, List<String>>) {
        this.cookieMapFunc = cookieMapFunc
    }

    fun headers(headersFunc: (url: String, headers: Map<String, String>) -> Map<String, String>) {
        this.headersFunc = headersFunc
    }

    fun jsBridgeHandler(action: String, func: (WebViewOwner) -> JsBridgeHandler) {
        jsBridgeHandler(listOf(action), func)
    }

    fun jsBridgeHandler(actions: List<String>, func: (WebViewOwner) -> JsBridgeHandler) {
        jsBridgeFactoriesDefinitions.add {
            listOf(
                createJsBridgeHandlerFactory(actions) {
                    func.invoke(it)
                }
            )

        }
    }

    fun jsBridgeHandlerFactory(func: () -> JsBridgeHandlerFactory<WebViewOwner>) {
        jsBridgeFactoriesDefinitions.add { listOf(func.invoke()) }
    }

    fun jsBridgeHandlerFactories(func: () -> List<JsBridgeHandlerFactory<WebViewOwner>>) {
        jsBridgeFactoriesDefinitions.add(func)
    }


    fun jsBridgeDispatcher(jsBridgeDispatcherFunc: (JavascriptCaller) -> JsBridgeDispatcher) {
        jsBridgeDispatcherDefinitions.add(jsBridgeDispatcherFunc)
    }

    fun uiThreadJsBridgeDispatcher(jsBridgeDispatcherFunc: (JavascriptCaller) -> JsBridgeDispatcher) {
        uiThreadJsBridgeDispatcherDefinitions.add(jsBridgeDispatcherFunc)
    }

    fun addUrlLoadInterceptor(urlLoadInterceptor: UrlLoadInterceptor) {
        this.urlLoadInterceptors.add(urlLoadInterceptor)
    }

    fun addUrlInterceptors(urlInterceptor: UrlInterceptor) {
        this.urlInterceptors.add(urlInterceptor)
    }

    fun subModule(uniWebSubModule: UniWebSubModule) {
        uniWebSubModule.define(this)
    }

    fun subModule(uniWebSubModules: List<UniWebSubModule>) {
        for (uniWebSubModule in uniWebSubModules) {
            uniWebSubModule.define(this)
        }
    }

    inline fun <reified T> getCustomWebViewOwner(): T? {
        return targetComponent.getCustomWebViewOwner<T>()
    }

}

inline fun <reified T> Fragment.getCustomWebViewOwner(): T? {
    var fragment: Fragment? = this
    while (fragment != null) {
        if (fragment is T) {
            return fragment
        } else {
            fragment = fragment.parentFragment
        }
    }
    val activity = this.activity
    if (activity is T) {
        return activity
    }
    return null
}
