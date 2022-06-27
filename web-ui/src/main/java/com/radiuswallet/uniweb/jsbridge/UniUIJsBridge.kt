package com.radiuswallet.uniweb.jsbridge

import android.graphics.Color
import com.radiuswallet.uniweb.UniWebViewOwner
import com.radiuswallet.uniweb.jsbridge.common.utils.getStringOrNull
import org.json.JSONObject
import timber.log.Timber

class UniUIJsBridge(private val uniUIWebViewOwner: UniWebViewOwner) : JsBridgeHandler {
    override fun handler(action: String, data: JSONObject, callBack: JsBridgeCallback): Boolean {
        when (action) {
            ACTION_CONFIG_NAVIGATION_BAR -> {
                val backgroundColor = colorStringToColorInt(data.optString("backgroundColor"))
                val tintColor = colorStringToColorInt(data.optString("tintColor"))
                uniUIWebViewOwner.setToolbarStyle(backgroundColor, tintColor, tintColor)
            }
            ACTION_SET_XL_RIGHT_ITEM,
            ACTION_SET_RIGHT_ITEM,
            -> {
                val title = data.getStringOrNull("title")
                val iconUrl = data.getStringOrNull("icon_url")
                if (!title.isNullOrEmpty()) {
                    val textColor = colorStringToColorInt(data.optString("textColor"))
                    val _textSizeAndroid = data.getStringOrNull("textSizeAndroid")
                    val _textSize = data.getStringOrNull("textSize")
                    val textSize = if (!_textSizeAndroid.isNullOrBlank()) {
                        _textSizeAndroid.toIntOrNull()
                    } else if (!_textSize.isNullOrBlank()) {
                        _textSize.toIntOrNull()
                    } else {
                        null
                    }
                    uniUIWebViewOwner.setTitleMenuItem(title, textColor, textSize) {
                        callBack.onCallback()
                    }
                } else if (!iconUrl.isNullOrEmpty()) {
                    uniUIWebViewOwner.setIconMenuItem(iconUrl, title) {
                        callBack.onCallback()
                    }
                }
            }
            ACTION_SET_CLOSE_ITEM_SHOW -> {
                uniUIWebViewOwner.setCloseButtonVisibility(data.optString("isShow").equals("1"))
            }
            ACTION_HIDDEN_RIGHT_ITEM -> {
                uniUIWebViewOwner.clearMenuItems()
            }
            ACTION_SET_NAVIGATION_ON_CLICK -> {
                if (callBack is EmptyJsBridgeCallback) {
                    uniUIWebViewOwner.setNavigationOnClickListener(null)
                } else {
                    uniUIWebViewOwner.setNavigationOnClickListener { callBack.onCallback() }
                }
            }
            ACTION_SET_TITLE -> {
                uniUIWebViewOwner.setToolbarTitle(data.optString("title"))
            }
            else -> {
                return false
            }
        }
        return true
    }

    private fun colorStringToColorInt(colorString: String): Int? {
        if (colorString.isNotBlank()) {
            val parseColorString = if (colorString.startsWith("0x")) {
                colorString.replace("0x", "#")
            } else if (colorString.startsWith("#")) {
                colorString
            } else {
                null
            }
            if (parseColorString != null) {
                try {
                    return Color.parseColor(parseColorString)
                } catch (e: Exception) {
                    Timber.tag(TAG_WEB_LOG).e(e)
                }
            }
        }
        return null
    }

    companion object {
        const val ACTION_CONFIG_NAVIGATION_BAR = "configNavigationBar"
        const val ACTION_SET_RIGHT_ITEM = "setRightItem"
        const val ACTION_SET_XL_RIGHT_ITEM = "setXLRightItem"
        const val ACTION_SET_CLOSE_ITEM_SHOW = "setCloseItemShow"
        const val ACTION_HIDDEN_RIGHT_ITEM = "hiddenRightItem"
        const val ACTION_SET_NAVIGATION_ON_CLICK = "setNavigationOnClick"
        const val ACTION_SET_TITLE = "setTitle"

        @JvmStatic
        fun factory(): JsBridgeHandlerFactory<UniWebViewOwner> {
            return createJsBridgeHandlerFactory(listOf(ACTION_CONFIG_NAVIGATION_BAR,
                ACTION_HIDDEN_RIGHT_ITEM,
                ACTION_SET_CLOSE_ITEM_SHOW,
                ACTION_SET_XL_RIGHT_ITEM,
                ACTION_SET_RIGHT_ITEM,
                ACTION_SET_NAVIGATION_ON_CLICK,
                ACTION_SET_TITLE)) {
                UniUIJsBridge(it)
            }
        }
    }

}