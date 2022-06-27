package com.radiuswallet.uniweb

import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.radiuswallet.uniweb.interceptor.UrlInterceptor
import com.radiuswallet.uniweb.interceptor.WebResourceRequestCompat
import com.radiuswallet.uniweb.jsbridge.TAG_WEB_LOG
import com.radiuswallet.uniweb.jsbridge.common.owner.WebViewOwner
import com.tencent.smtt.export.external.interfaces.SslErrorHandler
import com.tencent.smtt.export.external.interfaces.WebResourceError
import com.tencent.smtt.export.external.interfaces.WebResourceRequest
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient
import timber.log.Timber

open class SupportWebViewClient(
    private val context: Context,
    private val webViewOwner: WebViewOwner,
    private val interceptors: List<UrlInterceptor> = emptyList(),
) : WebViewClient() {

    var onPageStarted: ((url: String) -> Unit)? = null
    var onPageFinished: ((url: String) -> Unit)? = null
    var onMainFrameReceivedError: ((url: String, errorCode: Int) -> Unit)? = null

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        Timber.tag(TAG_WEB_LOG).d("shouldOverrideUrlLoading:url=$url")
        if (interceptors.isNotEmpty()) {
            for (interceptor in interceptors) {
                if (interceptor.intercept(view, WebResourceRequestCompat(url))) {
                    return true
                }
            }
        }
        return super.shouldOverrideUrlLoading(view, url)
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        Timber.tag(TAG_WEB_LOG).d("shouldOverrideUrlLoading:url=" + request.url)
        if (interceptors.isNotEmpty()) {
            for (interceptor in interceptors) {
                if (interceptor.intercept(view, WebResourceRequestCompat(request))) {
                    return true
                }
            }
        }
        return super.shouldOverrideUrlLoading(view, request)
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        onPageStarted?.invoke(url)
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        onPageFinished?.invoke(url)
    }

    @Suppress("DEPRECATION")
    override fun onReceivedError(
        view: WebView,
        errorCode: Int,
        description: String?,
        failingUrl: String,
    ) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        // Perhaps it is still called in newer API levels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) return
        //TODO 低版本判断 isForMainFrame 待测试
        if (!view.canGoBack() && !view.canGoForward()) {
            onMainFrameReceivedError?.invoke(failingUrl, errorCode)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError,
    ) {
        super.onReceivedError(view, request, error)
        if (request.isForMainFrame) {
            onMainFrameReceivedError?.invoke(request.url.toString(), error.errorCode)
        }
    }

    override fun onReceivedSslError(
        view: WebView?,
        handler: SslErrorHandler?,
        error: com.tencent.smtt.export.external.interfaces.SslError?,
    ) {
        //  super.onReceivedSslError(view, handler, error)
        val builder = MaterialAlertDialogBuilder(context)
        builder.setMessage(context.getString(R.string.uniweb_message_ssl_authentication_failed))
        builder.setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
            handler?.proceed()
        }
        builder.setNegativeButton(android.R.string.cancel) { _: DialogInterface, _: Int ->
            super.onReceivedSslError(view, handler, error)
            handler?.cancel()
        }
        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.show()
    }

}