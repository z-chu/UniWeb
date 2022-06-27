package com.radiuswallet.uniweb

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Message
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.radiuswallet.uniweb.jsbridge.common.owner.WebViewOwner
import com.radiuswallet.uniweb.view.IFileChooser
import com.radiuswallet.uniweb.view.IVideo
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient
import com.tencent.smtt.export.external.interfaces.JsPromptResult
import com.tencent.smtt.export.external.interfaces.JsResult
import com.tencent.smtt.export.external.interfaces.WebResourceRequest
import com.tencent.smtt.sdk.ValueCallback
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient

open class SupportWebChromeClient(
    private val context: Context,
    private val webViewOwner: WebViewOwner,
    private val iVideo: IVideo,
    private val iFileChooser: IFileChooser,
) : WebChromeClient() {

    private var defaultVideoPoster: Bitmap? = null

    var onProgressChanged: ((newProgress: Int) -> Unit)? = null
    var onReceivedTitle: ((title: String?) -> Unit)? = null
    var onJsPrompt: ((
        view: WebView?,
        url: String?,
        message: String?,
        defaultValue: String?,
        result: JsPromptResult?,
    ) -> Boolean)? = null

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        onProgressChanged?.invoke(newProgress)
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
        onReceivedTitle?.invoke(title)
    }

    override fun getDefaultVideoPoster(): Bitmap? {
        try {
            val defaultVideoPoster = super.getDefaultVideoPoster()
            if (defaultVideoPoster != null) {
                return defaultVideoPoster
            }
        } catch (e: Exception) {
        }
        if (defaultVideoPoster == null) {
            BitmapFactory.decodeResource(
                context.resources,
                R.drawable.uniweb_default_video_poster
            )
        }
        return defaultVideoPoster
    }

    /**
     * 在一些 Android 4.4 Kitkat 的手机上可能不生效
     */
    override fun openFileChooser(
        valueCallback: ValueCallback<Uri>?,
        acceptType: String?,
        capture: String?,
    ) {
        iFileChooser.openFileChooser(valueCallback, acceptType, capture)
    }

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?,
    ): Boolean {
        return iFileChooser.onShowFileChooser(webView, filePathCallback, fileChooserParams)
    }


    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?,
    ): Boolean {
        MaterialAlertDialogBuilder(context)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                result?.confirm()
            }
            .show()
        return true
    }

    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?,
    ): Boolean {
        MaterialAlertDialogBuilder(context)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                result?.confirm()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                result?.cancel()
            }
            .show()
        return true

    }

    override fun onJsPrompt(
        view: WebView?,
        url: String?,
        message: String?,
        defaultValue: String?,
        result: JsPromptResult?,
    ): Boolean {
        if (onJsPrompt?.invoke(view, url, message, defaultValue, result) == true) {
            return true
        }
        val editText = EditText(context)
        editText.setText(defaultValue)
        if (defaultValue != null) {
            editText.setSelection(defaultValue.length)
        }
        val listener =
            DialogInterface.OnClickListener { _, which ->
                if (which == Dialog.BUTTON_POSITIVE) {
                    result?.confirm(editText.text.toString())
                } else {
                    result?.cancel()
                }
            }
        MaterialAlertDialogBuilder(context)
            .setTitle(message)
            .setView(editText)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok, listener)
            .setNegativeButton(android.R.string.cancel, listener)
            .show()
        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val dpi: Float = context.resources.displayMetrics.density
        val t = (dpi * 16).toInt()
        layoutParams.setMargins(t, 0, t, 0)
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL
        editText.layoutParams = layoutParams
        val padding = (15 * dpi).toInt()
        editText.setPadding(padding - (5 * dpi).toInt(), padding, padding, padding)
        return true

    }


    override fun onCloseWindow(window: WebView?) {
        super.onCloseWindow(window)
        webViewOwner.closeCurrentWebView()
    }


    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?,
    ): Boolean {
        val webViewTransport = resultMsg?.obj as WebView.WebViewTransport
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?,
            ): Boolean {
                request?.let {
                    webViewOwner.openNewWebView(it.url.toString())
                }

                return true
            }
        }
        webViewTransport.webView = webView
        resultMsg.sendToTarget()
        return true
    }

    override fun onShowCustomView(view: View, callback: IX5WebChromeClient.CustomViewCallback) {
        iVideo.onShowCustomView(view, callback)
    }

    override fun onHideCustomView() {
        iVideo.onHideCustomView()
    }

}