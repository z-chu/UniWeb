package com.radiuswallet.uniweb.jsbridge.common

import com.radiuswallet.uniweb.jsbridge.JsBridgeCallback
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandler
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandlerFactory
import com.radiuswallet.uniweb.jsbridge.TAG_WEB_LOG
import com.radiuswallet.uniweb.jsbridge.common.owner.WebViewOwner
import com.radiuswallet.uniweb.jsbridge.common.utils.ImageFormat
import com.radiuswallet.uniweb.jsbridge.common.utils.compressImageFile
import com.radiuswallet.uniweb.jsbridge.common.utils.createCacheMirror
import com.radiuswallet.uniweb.jsbridge.common.utils.encodeImageBase64
import com.radiuswallet.uniweb.jsbridge.common.utils.imageFormat
import com.radiuswallet.uniweb.jsbridge.createJsBridgeHandlerFactory
import org.json.JSONObject
import timber.log.Timber

class PictureChooseJsBridge(
    private val webViewOwner: WebViewOwner,
    private val imageQuality: Int,//图片压缩比率
) : JsBridgeHandler {

    override fun handler(action: String, data: JSONObject, callBack: JsBridgeCallback): Boolean {
        if (ACTION_PICTURE == action) {
            webViewOwner.choosePhotoFromGallery { photoFromGallery ->
                try {
                    photoFromGallery?.compressImageFile(
                        outFile = photoFromGallery.createCacheMirror(
                            webViewOwner.context
                        ),
                        quality = imageQuality
                    )?.let { compressedFile ->
                        callBack.onCallback(
                            compressedFile.encodeImageBase64(
                                compressedFile.imageFormat() ?: ImageFormat.JPEG
                            )
                        )
                    }
                } catch (e: Exception) {//这里如果图片很大，会报 java.lang.OutOfMemoryError
                    Timber.tag(TAG_WEB_LOG).e(e, "js-bridge-action:%s", action)
                }
            }
            return true
        }
        return false
    }

    companion object {
        private const val ACTION_PICTURE = "picture"

        @JvmStatic
        @JvmOverloads
        fun factory(imageQuality: Int = 80): JsBridgeHandlerFactory<WebViewOwner> {
            return createJsBridgeHandlerFactory(ACTION_PICTURE) {
                PictureChooseJsBridge(it, imageQuality)
            }
        }
    }


}