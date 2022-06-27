package com.radiuswallet.uniweb.jsbridge

@RequiresOptIn(
    message = "当 JsBridgeHandlerFactory.actions 包含这个 action ，代表它能处理所以 action。" +
            "当你使用它时，请注意不要在 JsBridgeHandler 中轻易的 return true 而不小心拦截了所有的 action 而影响其他 JsBridgeHandler 的正常调用。" +
            "请谨慎使用！",
    level = RequiresOptIn.Level.ERROR
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
annotation class JsBridgeAllActionsHandler