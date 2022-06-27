package com.radiuswallet.uniweb.view

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.webkit.ValueCallback
import android.widget.Toast
import com.radiuswallet.uniweb.interceptor.UrlLoadHeader
import com.radiuswallet.uniweb.interceptor.UrlLoadInterceptor
import com.radiuswallet.uniweb.jsbridge.JavascriptCaller
import com.radiuswallet.uniweb.jsbridge.TAG_WEB_LOG
import com.radiuswallet.uniweb.utils.openActionView
import com.radiuswallet.uniweb.utils.openInBrowser
import com.tencent.smtt.sdk.DownloadListener
import com.tencent.smtt.sdk.WebSettings
import com.tencent.smtt.sdk.WebView
import timber.log.Timber

class UniWebView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : WebView(context, attrs, defStyleAttr) {

    private val uiThreadHandler = Handler(Looper.getMainLooper())

    val javascriptCaller: JavascriptCaller by lazy {
        UniWebJavascriptCaller(uiThreadHandler, this)
    }

    private val initialUrlInterceptors = ArrayList<UrlLoadInterceptor>()
    private var urlLoadHeader: UrlLoadHeader? = null

    var lastLoadUrl: String? = null
        private set

    private val urlLoader: UrlLoadInterceptor.UrlLoader = object : UrlLoadInterceptor.UrlLoader {
        override fun loadUrl(url: String, headers: Map<String, String>) {
            doLoadUrl(url, headers)
        }
    }

    private val downloadListener =
        DownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            //通过隐私意图，调用系统下载支持
            if (getContext().openActionView(url)) {
                return@DownloadListener
            }
            //用手机浏览器打开该链接，在手机浏览器中下载
            if (getContext().openInBrowser(url)) {
                return@DownloadListener
            }
            Toast.makeText(getContext(), "无法下载", Toast.LENGTH_SHORT).show()
        }

    init {
        isHorizontalScrollBarEnabled = false //水平不显示小方块
        isVerticalScrollBarEnabled = false //垂直不显示小方块
        val settings = settings
        settings.javaScriptEnabled = true
        settings.builtInZoomControls = true
        settings.displayZoomControls = true
        settings.loadWithOverviewMode = true
        settings.domStorageEnabled = true
        settings.pluginState = WebSettings.PluginState.ON //支持插件
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        settings.setSupportMultipleWindows(true)
        settings.setAppCacheEnabled(true)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        settings.setSupportZoom(true)
        settings.javaScriptCanOpenWindowsAutomatically = true//设置js可以直接打开窗口，如window.open()，默认为false
        settings.useWideViewPort = true//设置此属性，可任意比例缩放。大视图模式
        settings.domStorageEnabled = true//DOM Storage
        settings.allowFileAccess = true
        x5WebViewExtension?.let {
            val bundle = Bundle()
            // true表示标准全屏，false表示X5全屏；不设置默认false，
            bundle.putBoolean("standardFullScreen", true)
            // false：关闭小窗；true：开启小窗；不设置默认true，
            bundle.putBoolean("supportLiteWnd", false)
            //1：以页面内开始播放，2：以全屏开始播放；不设置默认：1
            bundle.putInt("DefaultVideoScreen", 1)
            it.invokeMiscMethod(
                "setVideoParams",
                bundle
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) {
                setWebContentsDebuggingEnabled(0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE)
            }
        }
        setDownloadListener(downloadListener)
    }

    /**
     * 用于给网页动态插入 javascript
     */
    internal fun loadJavascript(javascript: String) {
        if (javascript.startsWith("javascript")) {
            super.loadUrl(javascript)
        }
    }

    override fun loadUrl(url: String?) {
        this.loadUrl(url, null)
    }

    override fun loadUrl(url: String?, headers: MutableMap<String, String>?) {
        if (url != null) {
            val localHeaders = headers ?: emptyMap()
            if (initialUrlInterceptors.isNotEmpty()) {
                for (initialUrlInterceptor in initialUrlInterceptors) {
                    if (initialUrlInterceptor.intercept(urlLoader, url, localHeaders)) {
                        return
                    }
                }
            }
            urlLoader.loadUrl(url, localHeaders)
        }
    }

    private fun doLoadUrl(url: String, headers: Map<String, String>) {
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            val newHeaders = urlLoadHeader?.getHeaders(url, headers) ?: headers
            super.loadUrl(url, newHeaders)
            lastLoadUrl = url
            Timber.tag(TAG_WEB_LOG).d("UniWebView.loadUrl:url=$url ， headers=$headers")
        } else {
            uiThreadHandler.post {
                doLoadUrl(url, headers)
            }
        }
    }

    fun addInitialUrlInterceptor(interceptor: UrlLoadInterceptor) {
        initialUrlInterceptors.add(interceptor)
    }

    fun addInitialUrlInterceptor(interceptors: List<UrlLoadInterceptor>) {
        initialUrlInterceptors.addAll(interceptors)
    }

    fun removeInitialUrlInterceptor(interceptor: UrlLoadInterceptor) {
        initialUrlInterceptors.remove(interceptor)
    }

    fun setUrlLoadHeader(urlLoadHeader: UrlLoadHeader) {
        this.urlLoadHeader = urlLoadHeader
    }

    fun setUrlLoadHeader(urlLoadHeader: (url: String, headers: Map<String, String>) -> Map<String, String>) {
        setUrlLoadHeader(object : UrlLoadHeader {
            override fun getHeaders(
                url: String,
                headers: Map<String, String>,
            ): Map<String, String> {
                return urlLoadHeader.invoke(url, headers)
            }

        })
    }

    internal class UniWebJavascriptCaller(
        private val handler: Handler,
        private val webView: WebView,
    ) : JavascriptCaller {
        override fun callJs(js: String, callback: ValueCallback<String>?) {
            if (Thread.currentThread() == Looper.getMainLooper().thread) {
                webView.evaluateJavascript(js) { p0 -> callback?.onReceiveValue(p0) }
            } else {
                handler.post {
                    webView.evaluateJavascript(js) { p0 -> callback?.onReceiveValue(p0) }
                }
            }
        }
    }

}