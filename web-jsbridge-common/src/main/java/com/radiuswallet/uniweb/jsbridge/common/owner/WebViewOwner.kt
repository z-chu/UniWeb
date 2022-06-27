package com.radiuswallet.uniweb.jsbridge.common.owner

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import java.io.File

interface WebViewOwner {

    val context: Context

    val lifecycle: Lifecycle

    val fragmentManager: FragmentManager

    val activity: FragmentActivity?

    fun setRequestedOrientation(requestedOrientation: Int)

    fun takePhotoWithCamera(onResult: (File?) -> Unit)

    /**
     * 调起手机系统相机，拍摄视频
     */
    fun shootVideo(
        highQuality: Boolean = true,//视频文件是否为高质量
        durationLimit: Int? = null,//设置视频最大允许录制的时长，单位为 秒
        sizeLimit: Int? = null,//指定视频最大允许的尺寸，单位为byte。
        onResult: (File?) -> Unit,
    )

    /**
     * 选择的图片来自相册，注意不要对用户相册的图片进行修改，如果要修改应该先 copy 一份到 cache 目录进行修改
     */
    fun choosePhotoFromGallery(onResult: (File?) -> Unit)

    /**
     * @param onSelected 一定要返回结果，不然会阻断下次的文件选择
     */
    fun openFileChooser(acceptType: String = "*/*", onSelected: (uris: Array<Uri>?) -> Unit)

    /**
     * @param onSelected 一定要返回结果，不然会阻断下次的文件选择
     */
    fun openFileChooser(intent: Intent, onSelected: (uris: Array<Uri>?) -> Unit)

    fun showPermissionDeniedDialog(message: String)

    fun requestPermissions(
        permissions: Array<String>,
        callback: (grantResults: IntArray) -> Unit,
    )

    fun startActivityForResult(
        intent: Intent, action: String,
        callback: (resultCode: Int, data: Intent?) -> Unit,
    )

    fun closeCurrentWebView()

    fun openNewWebView(url: String, title: String? = null)

    fun getWebViewUrl(): String?

}