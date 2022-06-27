package com.radiuswallet.uniweb.jsbridge

import org.json.JSONObject

interface JsBridgeHandler {

    /**
     * @param action 来自解析 data 数据，将 action 字段取出
     * @param data 包含 H5 传递的完整数据，如： {action: "camera",callback: "callbackFunc"}
     * @param callBack 无需担心空指针。当 H5 没有设置 callBack字段 时 ，则调用会走空实现。
     */
    fun handler(
        action: String,
        data: JSONObject,
        callBack: JsBridgeCallback,
    ): Boolean

}
