package com.radiuswallet.uniweb.jsbridge.common

import com.radiuswallet.uniweb.jsbridge.JsBridgeCallback
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandler
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandlerFactory
import com.radiuswallet.uniweb.jsbridge.TAG_WEB_LOG
import com.radiuswallet.uniweb.jsbridge.common.owner.WebViewOwner
import com.radiuswallet.uniweb.jsbridge.common.utils.ImageFormat
import com.radiuswallet.uniweb.jsbridge.common.utils.JsBridgeTaskExecutors
import com.radiuswallet.uniweb.jsbridge.common.utils.compressImageFile
import com.radiuswallet.uniweb.jsbridge.common.utils.encodeImageBase64
import com.radiuswallet.uniweb.jsbridge.common.utils.imageFormat
import com.radiuswallet.uniweb.jsbridge.createJsBridgeHandlerFactory
import org.json.JSONObject
import timber.log.Timber

class CameraJsBridge(
    private val webViewOwner: WebViewOwner,
    private val imageQuality: Int,//图片压缩比率
) : JsBridgeHandler {

    override fun handler(action: String, data: JSONObject, callBack: JsBridgeCallback): Boolean {
        if (action == ACTION_CAMERA) {
            webViewOwner.takePhotoWithCamera {
                JsBridgeTaskExecutors.asyncThreadExecutor.execute {
                    try {
                        it?.compressImageFile(quality = imageQuality)?.let {
                            callBack.onCallback(
                                it.encodeImageBase64(
                                    it.imageFormat() ?: ImageFormat.JPEG
                                )
                            )
                        }
                    } catch (e: Throwable) {//这里如果图片很大，会报 java.lang.OutOfMemoryError
                        Timber.tag(TAG_WEB_LOG).e(e, "js-bridge-action:%s", action)
                    }
                }
            }
            return true
        }
        return false
    }

    companion object {

        private const val ACTION_CAMERA = "camera"

        @JvmStatic
        @JvmOverloads
        fun factory(imageQuality: Int = 80): JsBridgeHandlerFactory<WebViewOwner> {
            return createJsBridgeHandlerFactory(ACTION_CAMERA) {
                CameraJsBridge(it, imageQuality)
            }
        }
    }
}