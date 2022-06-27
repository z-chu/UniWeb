package com.radiuswallet.uniweb.jsbridge.common

import com.radiuswallet.uniweb.jsbridge.JsBridgeCallback
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandler
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandlerFactory
import com.radiuswallet.uniweb.jsbridge.common.owner.WebViewOwner
import com.radiuswallet.uniweb.jsbridge.common.utils.JsBridgePhotoFileProvider
import org.json.JSONObject
import java.io.File

/**
 * 给业务方去自定义视频拍摄的 JsBridge ，为重写而生。
 * 业务方可以重写该类的[onVideoCaptureComplete] 去自定义 web 调起 app 拍摄视频之后的一系列行为
 * 需要业务方自定义 [JsBridgeHandlerFactory] 来控制可处理的 action
 */
abstract class CustShootJsBridge(
    private val webViewOwner: WebViewOwner,
    private val highQuality: Boolean,//视频文件是否为高质量
    private val durationLimit: Int?,//设置视频最大允许录制的时长，单位为 秒
    private val sizeLimit: Int?,//指定视频最大允许的尺寸，单位为byte。
) : JsBridgeHandler {
    override fun handler(action: String, data: JSONObject, callBack: JsBridgeCallback): Boolean {
        if (!canHandler(action)) return false
        webViewOwner.shootVideo(highQuality, durationLimit, sizeLimit) { videoFile ->
            onVideoCaptureComplete(
                videoFile,
                {
                    JsBridgePhotoFileProvider.generateOutputMediaFile(
                        webViewOwner.context,
                        JsBridgePhotoFileProvider.MEDIA_TYPE_VIDEO
                    )
                },
                webViewOwner,
                callBack
            )
        }

        return true
    }


    /**
     * 业务方可以重写该方法去自定义 web 调起 app 拍摄视频之后的一系列行为
     * @param videoFile 可能为空，为空时代表拍摄视频失败
     * @param outputMediaGenerator 媒体文件生成器，可用于生成新的视频占位文件，以供接收压缩后的视频
     */
    abstract fun onVideoCaptureComplete(
        videoFile: File?,
        outputMediaGenerator: () -> File?,
        webViewOwner: WebViewOwner,
        callBack: JsBridgeCallback,
    )

    abstract fun canHandler(action: String): Boolean


}