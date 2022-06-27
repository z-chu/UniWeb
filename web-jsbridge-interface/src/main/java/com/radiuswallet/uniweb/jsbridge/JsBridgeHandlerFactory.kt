package com.radiuswallet.uniweb.jsbridge

import androidx.annotation.MainThread
import org.json.JSONObject

interface JsBridgeHandlerFactory<T> {
    /**
     * 在主线程执行
     * 创建 [JsBridgeHandler]
     */
    @MainThread
    fun create(webViewOwner: T): JsBridgeHandler

    /**
     * 在子线程执行
     * 用于在创建 [JsBridgeHandler] 之前， 判断是否具备处理该 action 的能力
     */
    val actions: List<String>
}

/**
 * 用于简化 [JsBridgeHandlerFactory] 类的编写
 */
fun <T> createJsBridgeHandlerFactory(
    actions: List<String>,
    creator: (webViewOwner: T) -> JsBridgeHandler,
): JsBridgeHandlerFactory<T> {
    return object : JsBridgeHandlerFactory<T> {
        override fun create(webViewOwner: T): JsBridgeHandler {
            return creator.invoke(webViewOwner)
        }

        override val actions = actions.toList()
    }
}

/**
 * 用于简化 [JsBridgeHandlerFactory] 类的编写
 */
fun <T> createJsBridgeHandlerFactory(
    action: String,
    creator: (webViewOwner: T) -> JsBridgeHandler,
): JsBridgeHandlerFactory<T> {
    return createJsBridgeHandlerFactory(listOf(action), creator)
}


/**
 * 将已经创建的 [JsBridgeHandler] 伪装成 [JsBridgeHandlerFactory],主要为了消除参数差异，复用 api。
 */
fun <T> JsBridgeHandler.disguisedAsFactory(actions: List<String>): JsBridgeHandlerFactory<T> {
    return object : JsBridgeHandlerFactory<T> {
        override fun create(webViewOwner: T): JsBridgeHandler {
            return this@disguisedAsFactory
        }

        override val actions = actions.toList()

    }
}

/**
 * 合并多个 [JsBridgeHandler] 为一个
 */
fun List<JsBridgeHandler>.merge(): JsBridgeHandler {
    return object : JsBridgeHandler {
        override fun handler(
            action: String,
            data: JSONObject,
            callBack: JsBridgeCallback,
        ): Boolean {
            for (jsBridgeHandler in this@merge) {
                if (jsBridgeHandler.handler(action, data, callBack)) {
                    return true
                }
            }
            return false
        }

    }
}
