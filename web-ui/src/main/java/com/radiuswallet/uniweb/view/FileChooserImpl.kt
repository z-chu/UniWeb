package com.radiuswallet.uniweb.view

import android.content.Intent
import android.net.Uri
import android.os.Build
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.radiuswallet.uniweb.R
import com.radiuswallet.uniweb.jsbridge.common.owner.WebViewOwner
import com.radiuswallet.uniweb.utils.isAvailableActivityIntent
import com.tencent.smtt.sdk.ValueCallback
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebView
import java.io.File

class FileChooserImpl(private val webViewOwner: WebViewOwner) : IFileChooser {

    private var uploadMsg: ValueCallback<Uri>? = null
    private var uploadMessageAboveL: ValueCallback<Array<Uri>>? = null

    override fun openFileChooser(
        valueCallback: ValueCallback<Uri>?,
        acceptType: String?,
        capture: String?,
    ) {
        this.uploadMsg = valueCallback
        var needCamera = false
        var needVideo = false
        if (acceptType != null) {
            if (acceptType.contains("*/") || acceptType.contains("image/")) {  //这是拍照模式
                needCamera = true
            } else if (acceptType.contains("video/")) {
                needVideo = true
            } else if (acceptType.contains("*/") || acceptType.contains("file/*")) {
                needCamera = true
                needVideo = true
            }
        }
        openFileChooserInternal(needCamera, needVideo, null, acceptType)
    }

    override fun onShowFileChooser(
        webView: WebView?,
        valueCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: WebChromeClient.FileChooserParams?,
    ): Boolean {
        uploadMessageAboveL = valueCallback
        var needCamera = false
        var needVideo = false
        val acceptTypes = fileChooserParams?.acceptTypes
        if (acceptTypes != null) {
            for (acceptType in acceptTypes) {
                if (acceptType.contains("image/")) {  //这是拍照模式
                    needCamera = true
                    continue
                }
                if (acceptType.contains("video/")) {
                    needVideo = true
                    continue
                }
                if (acceptType.contains("*/") || acceptType.contains("file/*")) {
                    needCamera = true
                    needVideo = true
                }
            }
        }
        openFileChooserInternal(needCamera, needVideo, fileChooserParams?.createIntent(), null)
        return true
    }

    private fun openFileChooserInternal(
        needCamera: Boolean,
        needVideo: Boolean,
        intent: Intent?,
        acceptType: String?,
    ) {
        if (needCamera || needVideo) {
            val context = webViewOwner.context
            val items = ArrayList<Pair<String, () -> Unit>>()
            if (needCamera) {
                items.add(
                    Pair(context.getString(R.string.uniweb_action_choose_photo_from_gallery),
                        {
                            webViewOwner.choosePhotoFromGallery {
                                onChooseFileComplete(it)
                            }
                        })
                )
                items.add(Pair(context.getString(R.string.uniweb_action_choose_photo_from_camera), {
                    webViewOwner.takePhotoWithCamera {
                        onChooseFileComplete(it)
                    }
                }))
            }
            if (needVideo) {
                items.add(Pair(context.getString(R.string.uniweb_action_choose_photo_from_video), {
                    webViewOwner.shootVideo {
                        onChooseFileComplete(it)
                    }
                }))
            }
            items.add(Pair(context.getString(R.string.uniweb_action_choose_photo_from_files), {
                doOpenFileChooser(intent, acceptType)
            }))
            MaterialAlertDialogBuilder(webViewOwner.context)
                .setItems(items.map { it.first }.toTypedArray()) { _, i ->
                    items[i].second.invoke()
                }
                .setOnCancelListener {
                    cancelChoosePhotos()
                }
                .show()
        } else {
            doOpenFileChooser(intent, acceptType)
        }

    }

    private fun doOpenFileChooser(
        intent: Intent?,
        acceptType: String?,
    ) {
        if (intent != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && intent.getAction() == Intent.ACTION_GET_CONTENT) {
                intent.action = Intent.ACTION_OPEN_DOCUMENT
            }
            if (intent.isAvailableActivityIntent(webViewOwner.context) != false) {
                webViewOwner.openFileChooser(intent) {
                    onChooseUrisComplete(it)
                }
                return
            }

        }
        webViewOwner.openFileChooser(acceptType ?: "*/*") {
            onChooseUrisComplete(it)
        }
    }


    private fun onChooseFileComplete(file: File?) {
        if (file != null) {
            val localUri = Uri.fromFile(file)
            onChooseUrisComplete(arrayOf(localUri))
        } else {
            onChooseUrisComplete(null)
        }
    }

    private fun onChooseUrisComplete(uris: Array<Uri>?) {
        if (uris != null) {
            uploadMessageAboveL?.onReceiveValue(uris)
            if (uris.isEmpty()) {
                uploadMsg?.onReceiveValue(uris[0])
            }
        } else {
            uploadMessageAboveL?.onReceiveValue(null)
            uploadMsg?.onReceiveValue(null)
        }
        uploadMessageAboveL = null
        uploadMsg = null
    }


    private fun cancelChoosePhotos() {
        uploadMessageAboveL?.onReceiveValue(null)
        uploadMessageAboveL = null
        uploadMsg?.onReceiveValue(null)
        uploadMsg = null
    }
}