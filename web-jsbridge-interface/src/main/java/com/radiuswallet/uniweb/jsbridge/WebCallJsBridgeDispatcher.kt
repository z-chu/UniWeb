package com.radiuswallet.uniweb.jsbridge

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException


class WebCallJsBridgeDispatcher<T> private constructor(
    private val webViewOwner: T,
    private val jsBridgeHandlerFactors: List<JsBridgeHandlerFactory<T>>,
    private val javascriptCaller: JavascriptCaller,
    private val finallyCallData: String?,
) : JsBridgeDispatcher {

    private val mainExecutor: MainHandlerExecutor by lazy(LazyThreadSafetyMode.NONE) {
        MainHandlerExecutor(Handler(Looper.getMainLooper()))
    }
    private val jsBridgeHandlerMap = HashMap<JsBridgeHandlerFactory<T>, JsBridgeHandler>()

    override fun dispatchWebCall(string: String): Boolean {
        var action: String? = null
        var callbackMethodName: String? = null
        var jsonObject: JSONObject? = null
        try {
            jsonObject = JSONObject(string)
            action = jsonObject.optString("action")
            callbackMethodName = jsonObject.optString("callback")
        } catch (e: Throwable) {
            Timber.tag(TAG_WEB_LOG).e(e)
        }
        if (jsonObject == null || action.isNullOrBlank()) {
            return false
        }

        var canHandler = false
        val canHandlerFactors = ArrayList<JsBridgeHandlerFactory<T>>()
        for (jsBridgeHandlerFactory in jsBridgeHandlerFactors) {
            val actions = jsBridgeHandlerFactory.actions
            if (actions.contains(ALL_ACTIONS) || actions.contains(action)) {
                canHandler = true
                canHandlerFactors.add(jsBridgeHandlerFactory)
            }
        }

        if (canHandlerFactors.isNotEmpty()) {
            mainExecutor.execute {
                val jsCallBack: JsBridgeCallback = if (!callbackMethodName.isNullOrBlank()) {
                    ExecuteJsBridgeCallback(
                        action,
                        javascriptCaller,
                        callbackMethodName,
                        mainExecutor,
                        finallyCallData
                    )
                } else {
                    EmptyJsBridgeCallback(action, finallyCallData)
                }
                for (jsBridgeHandlerFactory in canHandlerFactors) {
                    val jsBridgeHandler = jsBridgeHandlerMap.getOrPut(jsBridgeHandlerFactory) {
                        jsBridgeHandlerFactory.create(webViewOwner)
                    }
                    val ended = jsBridgeHandler.handler(action, jsonObject, jsCallBack)
                    if (ended) {
                        Timber.tag(TAG_WEB_LOG).d(
                            "web call:%s , handler:%s",
                            string.subTimberLog(),
                            jsBridgeHandler.javaClass.name
                        )
                        return@execute
                    }
                }
                //???????????? JsBridgeHandler ?????????????????????????????????????????? null
                if (jsCallBack is ExecuteJsBridgeCallback) {
                    jsCallBack.finallyCall()
                }
            }
        }
        return canHandler
    }


    private class MainHandlerExecutor(private val mHandler: Handler) : Executor {
        override fun execute(command: Runnable) {
            if (!mHandler.post(command)) {
                throw RejectedExecutionException("$mHandler is shutting down")
            }
        }
    }

    fun getAllActions(): List<String> {
        val actions = HashSet<String>()
        for (jsBridgeHandlerFactor in jsBridgeHandlerFactors) {
            actions.addAll(jsBridgeHandlerFactor.actions)
        }
        actions.remove(ALL_ACTIONS)
        return actions.toList()
    }

    companion object {


        private const val ALL_ACTIONS = "#&web_call_all_actions&#"

        /**
         * ??? [JsBridgeHandlerFactory.actions] ???????????? action ??????????????????????????? action???
         * ???????????????????????????????????????????????????????????? [JsBridgeHandler] ??? return true ?????????????????????????????? action ??????????????? [JsBridgeHandler] ??????????????????
         */
        @JsBridgeAllActionsHandler
        fun allActions(): String = ALL_ACTIONS

        @JvmStatic
        @JvmOverloads
        fun <T> create(
            webViewOwner: T,
            jsBridgeHandlerFactors: List<JsBridgeHandlerFactory<T>>,
            javascriptCaller: JavascriptCaller,
            finallyCallData: String? = null,
        ): WebCallJsBridgeDispatcher<T> {
            return WebCallJsBridgeDispatcher(
                webViewOwner,
                jsBridgeHandlerFactors,
                javascriptCaller,
                finallyCallData
            )
        }

        @JvmStatic
        @JvmOverloads
        fun <T> create(
            webViewOwner: T,
            jsBridgeHandlerFactory: JsBridgeHandlerFactory<T>,
            javascriptCaller: JavascriptCaller,
            finallyCallData: String? = null,
        ): WebCallJsBridgeDispatcher<T> {
            return WebCallJsBridgeDispatcher(
                webViewOwner,
                listOf(jsBridgeHandlerFactory),
                javascriptCaller,
                finallyCallData
            )
        }

        /**
         * ????????????  [JsBridgeHandler] ????????????????????????????????? [JsBridgeHandlerFactory] ???
         * ????????????????????????????????? Activity ??? Fragment ???????????? [JsBridgeHandler]???
         * ?????????????????????????????????????????????????????????????????????
         */
        @JvmStatic
        @JvmOverloads
        fun compose(
            actions: List<String>,
            jsBridgeHandler: JsBridgeHandler,
            javascriptCaller: JavascriptCaller,
            finallyCallData: String? = null,
        ): WebCallJsBridgeDispatcher<Any> {
            return WebCallJsBridgeDispatcher(
                Any(),
                listOf(jsBridgeHandler.disguisedAsFactory(actions)),
                javascriptCaller,
                finallyCallData
            )
        }

        /**
         * ????????????  [JsBridgeHandler] ????????????????????????????????? [JsBridgeHandlerFactory] ???
         * ????????????????????????????????? Activity ??? Fragment ???????????? [JsBridgeHandler]???
         * ?????????????????????????????????????????????????????????????????????
         */
        @JvmStatic
        @JvmOverloads
        fun compose(
            actions: List<String>,
            jsBridgeHandlers: List<JsBridgeHandler>,
            javascriptCaller: JavascriptCaller,
            finallyCallData: String? = null,
        ): WebCallJsBridgeDispatcher<Any> {
            return WebCallJsBridgeDispatcher(
                Any(),
                listOf(jsBridgeHandlers.merge().disguisedAsFactory(actions)),
                javascriptCaller,
                finallyCallData
            )
        }
    }
}

fun WebCallJsBridgeDispatcher<*>.toJavascriptInterface(): WebCallJavascriptInterface {
    return WebCallJavascriptInterface(this)
}

class WebCallJavascriptInterface(private val jsBridgeDispatcher: WebCallJsBridgeDispatcher<*>) {
    @JavascriptInterface
    fun webcall(string: String) {
        jsBridgeDispatcher.dispatchWebCall(string)
    }
}

/**
 * ??? js ????????????????????? [WebCallJsBridgeDispatcher] ????????????????????????
 */
fun List<WebCallJsBridgeDispatcher<*>>.dispatchWebCall(string: String): Boolean {
    for (webCallJsBridgeDispatcher in this) {
        if (webCallJsBridgeDispatcher.dispatchWebCall(string)) {
            return true
        }
    }
    return false
}