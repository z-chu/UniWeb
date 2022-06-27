package com.radiuswallet.uniweb.module

import androidx.fragment.app.Fragment
import com.radiuswallet.uniweb.jsbridge.common.owner.WebViewOwner
import java.util.*

class UniWebModule internal constructor(
    private val isAutoInstallSubModule: Boolean,
    private val subModules: List<UniWebSubModule>,
    val declaration: UniWebDefinition.() -> Unit,
) {
    fun define(targetComponent: Fragment, webViewOwner: WebViewOwner): UniWebDefinition {
        val definition = UniWebDefinition(targetComponent, webViewOwner)
        declaration(definition)
        val localSubModules = ArrayList<UniWebSubModule>()
        localSubModules.addAll(subModules)
        if (isAutoInstallSubModule) {
            localSubModules.addAll(getAutoRegisterSubModules())
            localSubModules.sortBy {
                -it.priority
            }
        }
        definition.subModule(localSubModules)
        return definition
    }
}

fun uniWebModule(
    isAutoInstallSubModule: Boolean = true,
    subModules: List<UniWebSubModule> = emptyList(),
    declaration: UniWebDefinition.() -> Unit,
): UniWebModule {
    return UniWebModule(isAutoInstallSubModule, subModules, declaration)
}

private fun getAutoRegisterSubModules(): List<UniWebSubModule> {
    val installableServiceLoader = ServiceLoader.load(WebModuleInstallable::class.java)
    val subModules = ArrayList<UniWebSubModule>()
    installableServiceLoader.iterator().forEach {
        subModules.addAll(it.modules)
    }
    return subModules
}

