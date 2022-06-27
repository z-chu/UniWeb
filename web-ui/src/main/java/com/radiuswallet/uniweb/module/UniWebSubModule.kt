package com.radiuswallet.uniweb.module

class UniWebSubModule internal constructor(
    val priority: Int,
    internal val declaration: UniWebSubDefinition.() -> Unit,
) {
    fun define(uniWebDefinition: UniWebDefinition) {
        val definition = UniWebSubDefinition(uniWebDefinition)
        declaration(definition)
    }
}

fun uniWebSubModule(
    priority: Int = 0,
    declaration: UniWebSubDefinition.() -> Unit,
): UniWebSubModule {
    return UniWebSubModule(priority, declaration)
}
