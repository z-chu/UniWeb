package com.radiuswallet.uniweb.jsbridge.common.utils

import android.content.Context
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class JsBridgePhotoFileProvider : FileProvider() {

    companion object {
        private const val EXTERNAL_CACHE_PATH = "/JsBridgeMedia"
        private const val CACHE_PATH = "/JsBridgeMedia"
        const val MEDIA_TYPE_IMAGE = 1
        const val MEDIA_TYPE_VIDEO = 2

        fun getAuthority(context: Context): String {
            return context.applicationContext.packageName + ".js_bridge_photo.provider"
        }

        fun generateOutputMediaFile(
            context: Context,
            type: Int = MEDIA_TYPE_IMAGE,
        ): File? {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val outputMediaDirPath = getOutputMediaDirPath(context)
            return if (outputMediaDirPath != null) {
                if (type == MEDIA_TYPE_VIDEO) {
                    File("$outputMediaDirPath${File.separator}VID_$timeStamp.mp4")
                } else {
                    File("$outputMediaDirPath${File.separator}IMG_$timeStamp.jpg")
                }
            } else {
                null
            }
        }

        private fun getOutputMediaDirPath(context: Context): String? {
            var outputMediaDirPath: String? = null
            val externalCacheDir = context.externalCacheDir
            if (externalCacheDir != null) {
                val fileDir = File(externalCacheDir.path + EXTERNAL_CACHE_PATH)
                if (fileDir.exists() || fileDir.mkdirs()) {
                    outputMediaDirPath = externalCacheDir.path + EXTERNAL_CACHE_PATH
                }
            }
            if (outputMediaDirPath == null) {
                val cacheDir = context.cacheDir
                val fileDir = File(cacheDir.path + CACHE_PATH)
                if (fileDir.exists() || fileDir.mkdirs()) {
                    outputMediaDirPath = cacheDir.path + CACHE_PATH
                }
            }
            return outputMediaDirPath
        }
    }

}