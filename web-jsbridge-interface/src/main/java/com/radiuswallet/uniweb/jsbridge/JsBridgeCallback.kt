package com.radiuswallet.uniweb.jsbridge

import android.webkit.ValueCallback

interface JsBridgeCallback {

    /**
     * 回调数据，回调给 H5 的完整 javascriptString 将是："javascript:$methodName('$param')"
     * 这里你只需要传递 $param 部分的数据
     * @param param  "javascript:$methodName('${param?:finallyCallData}')"
     *                 当 param 和 finallyCallData 都为 null 时，不会回调
     */
    fun onCallback(param: String?)

    /**
     * "javascript:$methodName()"
     * "javascript:$methodName('$param')"
     * "javascript:$methodName('$param1','$param2')"
     * "javascript:$methodName('$param1','$param2','$param3')"
     */
    fun onCallback(vararg params: String)

    /**
     * "javascript:$methodName('$param')"
     * "javascript:$methodName('$param1','$param2')"
     * "javascript:$methodName('$param1','$param2','$param3')"
     */
    fun onCallback(callback: ValueCallback<String>, vararg params: String)


    /**
     * 你可以使用 JsBridgeParam 的 isRaw 属性控制是否需要在回调时添加 ' ' 包裹参数，可用于回调 jsonObject。
     * "javascript:$methodName(jsonObject)"
     * "javascript:$methodName(jsonObject,'param2')"
     * "javascript:$methodName('param1',jsonObject,"param3")"
     */
    fun onCallback(callback: ValueCallback<String>?, vararg params: JsBridgeParam)

}