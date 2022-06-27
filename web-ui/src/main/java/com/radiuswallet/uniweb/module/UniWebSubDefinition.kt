package com.radiuswallet.uniweb.module

import com.radiuswallet.uniweb.interceptor.UrlInterceptor
import com.radiuswallet.uniweb.interceptor.UrlLoadInterceptor
import com.radiuswallet.uniweb.jsbridge.JavascriptCaller
import com.radiuswallet.uniweb.jsbridge.JsBridgeDispatcher
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandler
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandlerFactory
import com.radiuswallet.uniweb.jsbridge.common.owner.WebViewOwner

class UniWebSubDefinition(
    private val mainWebDefinition: UniWebDefinition,
) {

    val targetComponent = mainWebDefinition.targetComponent
    val webViewOwner = mainWebDefinition.webViewOwner

    fun jsBridgeHandler(action: String, func: (WebViewOwner) -> JsBridgeHandler) {
        mainWebDefinition.jsBridgeHandler(action, func)
    }

    fun jsBridgeHandler(actions: List<String>, func: (WebViewOwner) -> JsBridgeHandler) {
        mainWebDefinition.jsBridgeHandler(actions, func)

    }

    fun jsBridgeHandlerFactory(func: () -> JsBridgeHandlerFactory<WebViewOwner>) {
        mainWebDefinition.jsBridgeHandlerFactory(func)

    }

    fun jsBridgeHandlerFactories(func: () -> List<JsBridgeHandlerFactory<WebViewOwner>>) {
        mainWebDefinition.jsBridgeHandlerFactories(func)
    }

    fun jsBridgeDispatcher(jsBridgeDispatcherFunc: (JavascriptCaller) -> JsBridgeDispatcher) {
        mainWebDefinition.jsBridgeDispatcher(jsBridgeDispatcherFunc)
    }

    fun uiThreadJsBridgeDispatcher(jsBridgeDispatcherFunc: (JavascriptCaller) -> JsBridgeDispatcher) {
        mainWebDefinition.uiThreadJsBridgeDispatcher(jsBridgeDispatcherFunc)
    }

    fun addUrlLoadInterceptor(urlLoadInterceptor: UrlLoadInterceptor) {
        mainWebDefinition.addUrlLoadInterceptor(urlLoadInterceptor)
    }

    fun addUrlInterceptors(urlInterceptor: UrlInterceptor) {
        mainWebDefinition.addUrlInterceptors(urlInterceptor)
    }

    inline fun <reified T> getCustomWebViewOwner(): T? {
        return targetComponent.getCustomWebViewOwner<T>()
    }

}