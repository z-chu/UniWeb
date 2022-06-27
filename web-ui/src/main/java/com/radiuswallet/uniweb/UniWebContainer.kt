package com.radiuswallet.uniweb

import com.radiuswallet.uniweb.module.UniWebDefinition

interface UniWebContainer {

    fun onWebDefinitionCreated(webDefinition: UniWebDefinition)

}