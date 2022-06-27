package com.radiuswallet.uniweb.jsbridge.common

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.contentValuesOf
import com.radiuswallet.uniweb.jsbridge.JsBridgeCallback
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandler
import com.radiuswallet.uniweb.jsbridge.JsBridgeHandlerFactory
import com.radiuswallet.uniweb.jsbridge.TAG_WEB_LOG
import com.radiuswallet.uniweb.jsbridge.common.owner.WebViewOwner
import com.radiuswallet.uniweb.jsbridge.common.utils.JsBridgeTaskExecutors.asyncThreadExecutor
import com.radiuswallet.uniweb.jsbridge.common.utils.JsBridgeTaskExecutors.mainThreadExecutor
import com.radiuswallet.uniweb.jsbridge.common.utils.asBase64toBitmap
import com.radiuswallet.uniweb.jsbridge.common.utils.isPermissionGrantedAll
import com.radiuswallet.uniweb.jsbridge.common.utils.saveBitmap
import com.radiuswallet.uniweb.jsbridge.createJsBridgeHandlerFactory
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.*

class ImageSaveJsBridge(
    private val webViewOwner: WebViewOwner,
    private val imageDir: String,
) : JsBridgeHandler {

    override fun handler(action: String, data: JSONObject, callBack: JsBridgeCallback): Boolean {
        if (SAVE_IMG == action) {
            val content = data.optString("content")
            if (content.isNotBlank()) {
                webViewOwner.requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (it.isPermissionGrantedAll()) {
                        asyncThreadExecutor.execute {
                            //在子线程保存图片
                            val path = saveBase64Bitmap(webViewOwner.context, imageDir, content)
                            mainThreadExecutor.execute {
                                //主线程通知结果
                                if (path != null) {
                                    Toast.makeText(
                                        webViewOwner.context,
                                        webViewOwner.context.getString(
                                            R.string.message_format_image_save_to,
                                            path
                                        ),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    callBack.onCallback("1")
                                } else {
                                    Toast.makeText(
                                        webViewOwner.context,
                                        R.string.message_error_image_save_failed,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    callBack.onCallback("0")
                                }
                            }
                        }
                    } else {
                        callBack.onCallback("0")
                    }
                }

            }
            return true
        }
        return false
    }


    private fun saveBase64Bitmap(context: Context, imageDir: String, base64: String): String? {
        val bitmap =
            base64.asBase64toBitmap(Base64.DEFAULT)
                ?: base64.asBase64toBitmap(Base64.NO_WRAP)
                ?: return null
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val outputImageDirPath = getOutputImageDirPath(context, imageDir)
        val filePic = File("$outputImageDirPath${File.separator}IMG_$timeStamp.jpg")
        if (!filePic.exists()) {
            try {
                if (saveBitmap(bitmap, filePic)) {
                    scanFile(context, filePic)
                    return filePic.path
                }
            } catch (throwable: Throwable) {
                Timber.tag(TAG_WEB_LOG).e(throwable)
            }

        }
        return null
    }

    private fun getOutputImageDirPath(context: Context, imageDir: String): String? {
        var outputMediaDirPath: String? = null
        val externalDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        if (externalDir != null) {
            val fileDir = File(externalDir.path + imageDir)
            if (fileDir.exists() || fileDir.mkdirs()) {
                outputMediaDirPath = externalDir.path + imageDir
            }
        }
        return outputMediaDirPath
    }

    /**
     * 适配 Android 10 ，将文件复制到公共目录
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun File.insertMediaStoreAndroidQ(context: Context, imageDir: String): String? {
        val contentResolver = context.contentResolver
        val path = Environment.DIRECTORY_PICTURES + imageDir
        val uri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValuesOf(
                MediaStore.MediaColumns.DISPLAY_NAME to this.name,
                MediaStore.MediaColumns.MIME_TYPE to URLConnection.guessContentTypeFromName(this.name),
                MediaStore.MediaColumns.RELATIVE_PATH to path,
                MediaStore.MediaColumns.DATE_MODIFIED to System.currentTimeMillis() / 1000
            )
        )
        if (uri != null) {
            val openOutputStream = contentResolver.openOutputStream(uri) ?: return null
            this.inputStream().use { input ->
                openOutputStream.use { output ->
                    input.copyTo(output, 8 * 1024)
                }
            }
            return path
        }
        return null
    }

    private fun scanFile(context: Context, file: File) {
        MediaScannerConnection.scanFile(
            context, arrayOf(file.path),
            arrayOf(URLConnection.guessContentTypeFromName(file.name))
        ) { path, uri ->
            Timber.tag(TAG_WEB_LOG).d("path:$path , uri: ${uri?.toString()}")
        }
        context.sendBroadcast(
            Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(file)
            )
        )
    }

    companion object {

        private const val SAVE_IMG = "saveImg"

        /**
         * @param imageDir 决定保存后的目录路径，最好传 app 的标识，如：/uniweb
         *                 如传：/uniweb , 最后在 Android 10 上的保存路径为：内部存储/Pictures/uniweb/IMG_20210818_154240.jpg
         */
        @JvmStatic
        @JvmOverloads
        fun factory(imageDir: String = "/uniweb"): JsBridgeHandlerFactory<WebViewOwner> {
            val localImageDir = if (!imageDir.startsWith("/")) {
                "/$imageDir"
            } else {
                imageDir
            }
            return createJsBridgeHandlerFactory(SAVE_IMG) {
                ImageSaveJsBridge(it, localImageDir)
            }
        }
    }
}