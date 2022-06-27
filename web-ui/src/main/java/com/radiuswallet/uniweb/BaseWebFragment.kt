package com.radiuswallet.uniweb

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.addCallback
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import com.radiuswallet.uniweb.interceptor.OthersSchemeInterceptor
import com.radiuswallet.uniweb.jsbridge.InternalAppInfoJsBridge
import com.radiuswallet.uniweb.jsbridge.UiThreadJsBridgeDispatcher
import com.radiuswallet.uniweb.jsbridge.WebCallJsBridgeDispatcher
import com.radiuswallet.uniweb.jsbridge.common.CameraJsBridge
import com.radiuswallet.uniweb.jsbridge.common.CheckWxInstallJsBridge
import com.radiuswallet.uniweb.jsbridge.common.ClipboardJsBridge
import com.radiuswallet.uniweb.jsbridge.common.CloseWebViewJsBridge
import com.radiuswallet.uniweb.jsbridge.common.GoBrowserJsBridge
import com.radiuswallet.uniweb.jsbridge.common.ImageSaveJsBridge
import com.radiuswallet.uniweb.jsbridge.common.OpenWebViewJsBridge
import com.radiuswallet.uniweb.jsbridge.common.OpenWifiSettingJsBridge
import com.radiuswallet.uniweb.jsbridge.common.PictureChooseJsBridge
import com.radiuswallet.uniweb.jsbridge.common.RealTimePositionJsBridge
import com.radiuswallet.uniweb.jsbridge.common.RequestedOrientationJsBridge
import com.radiuswallet.uniweb.jsbridge.common.owner.FragmentWebViewOwner
import com.radiuswallet.uniweb.jsbridge.common.owner.WebViewOwner
import com.radiuswallet.uniweb.module.UniWebDefinition
import com.radiuswallet.uniweb.module.UniWebModule
import com.radiuswallet.uniweb.utils.bindLifecycle
import com.radiuswallet.uniweb.utils.setCookie
import com.radiuswallet.uniweb.view.FileChooserImpl
import com.radiuswallet.uniweb.view.UniWebView
import com.radiuswallet.uniweb.view.VideoImpl
import com.tencent.smtt.sdk.WebSettings
import com.tencent.smtt.sdk.WebView

abstract class BaseWebFragment : Fragment() {

    protected lateinit var webView: UniWebView
    protected lateinit var initialUrl: String
        private set

    private lateinit var webModule: UniWebModule
    private lateinit var webDefinition: UniWebDefinition

    protected val viewModel by activityViewModels<com.radiuswallet.uniweb.UniWebViewModel>()
    private lateinit var fragmentWebViewOwner: FragmentWebViewOwner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialUrl =
            prepareInitialUrl(arguments?.getString(com.radiuswallet.uniweb.BaseWebFragment.Companion.K_ARG_URL))
        webModule =
            com.radiuswallet.uniweb.UniWebSetter.getWebModule(arguments?.getString(com.radiuswallet.uniweb.BaseWebFragment.Companion.K_WEB_MODULE_KEY))
    }

    protected open fun prepareInitialUrl(fromArgUrl: String?): String {
        return fromArgUrl ?: com.radiuswallet.uniweb.BaseWebFragment.Companion.URL_BLANK_PAGE
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentWebViewOwner = createFragmentWebViewOwner()
        val createWebDefinition = createWebDefinition(webModule)
        val parentFragment = parentFragment
        val activity = activity
        if (parentFragment is com.radiuswallet.uniweb.UniWebContainer) {
            parentFragment.onWebDefinitionCreated(createWebDefinition)
        } else if (activity is com.radiuswallet.uniweb.UniWebContainer) {
            activity.onWebDefinitionCreated(createWebDefinition)
        }
        onWebDefinitionCreated(createWebDefinition)
        webDefinition = createWebDefinition
        webView = lazyUniWebView(view)
        requireContext().setCookie(getCookieConfig())
        webView.bindLifecycle(viewLifecycleOwner)
        webView.setUrlLoadHeader { url, headers ->
            webDefinition.headersFunc.invoke(url, headers)
        }

        webView.addInitialUrlInterceptor(webDefinition.urlLoadInterceptors)
        initWebSettings(webView.settings)
        initWebClient(webView, savedInstanceState)
        initWebJavascriptInterface(webView)
        initOnBackPressedCallback(webView, requireActivity().onBackPressedDispatcher)
        onWebViewInitialized(webView, initialUrl, savedInstanceState)
    }

    protected abstract fun lazyUniWebView(view: View): UniWebView


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }


    @CallSuper
    protected open fun createWebDefinition(webModule: UniWebModule): UniWebDefinition {
        return webModule.define(this, fragmentWebViewOwner)
    }

    protected open fun onWebDefinitionCreated(webDefinition: UniWebDefinition) {
    }

    protected open fun initWebSettings(settings: WebSettings) {
        webDefinition.webSettingsFunc.invoke(initialUrl, settings)
    }

    protected open fun initWebClient(webView: UniWebView, savedInstanceState: Bundle?) {
        //setup WebViewClient
        val urlInterceptors = webDefinition.urlInterceptors.toMutableList()
        urlInterceptors.add(OthersSchemeInterceptor())
        val supportWebViewClient =
            SupportWebViewClient(
                requireContext(),
                fragmentWebViewOwner,
                urlInterceptors
            )
        supportWebViewClient.onMainFrameReceivedError = { url, errorCode ->
            viewModel.receiveLoadingError(url, errorCode)
        }
        supportWebViewClient.onPageStarted = {
            viewModel.startLoadPage(it)
        }
        supportWebViewClient.onPageFinished = {
            viewModel.finishLoadingPage(it)
        }
        webView.webViewClient = supportWebViewClient

        //setup WebChromeClient
        val supportWebChromeClient = SupportWebChromeClient(
            requireContext(),
            fragmentWebViewOwner,
            UniWebSetter.getIVideo(this, webView) ?: VideoImpl(activity,
                webView),
            UniWebSetter.getIFileChooser(fragmentWebViewOwner)
                ?: FileChooserImpl(
                    fragmentWebViewOwner)
        )
        supportWebChromeClient.onProgressChanged = {
            viewModel.changeLoadingProgress(it)
        }
        supportWebChromeClient.onReceivedTitle = {
            viewModel.receiveTitle(it)
        }

        webView.webChromeClient = supportWebChromeClient
    }

    protected open fun initWebJavascriptInterface(webView: UniWebView) {
        val supportJavascriptInterface = com.radiuswallet.uniweb.SupportJavascriptInterface()
        //注册在主线程执行的 [JsBridgeDispatcher] ，主要用于做 JsBridge 执行前的拦截
        supportJavascriptInterface.registerJsBridgeDispatcher(object :
            UiThreadJsBridgeDispatcher() {
            override fun dispatchWebCallOnUiThread(string: String): Boolean {
                if (context == null || isDetached || viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                    return true
                }
                val uiThreadJsBridgeDispatchers =
                    webDefinition.createUiThreadJsBridgeDispatchers(webView.javascriptCaller)
                for (uiThreadJsBridgeDispatcher in uiThreadJsBridgeDispatchers) {
                    if (uiThreadJsBridgeDispatcher.dispatchWebCall(string)) {
                        return true
                    }
                }
                return false
            }
        })


        //注册外部注入的 [JsBridgeDispatcher]
        supportJavascriptInterface.registerJsBridgeDispatcher(webDefinition.createJsBridgeDispatchers(
            webView.javascriptCaller))

        //获取外部注入的 JsBridgeHandlerFactory<WebViewOwner>
        val jsBridgeHandlerFactories =
            webDefinition.createJsBridgeFactories().toMutableList()

        //[InternalAppInfoJsBridge] 必须添加
        jsBridgeHandlerFactories.add(InternalAppInfoJsBridge.factory {
            val hashMap = HashMap<String, String>()
            hashMap.putAll(webDefinition.extraDataFunc.invoke())
            hashMap
        })

        //带上 UniWebKit 内置的一部分 Common JsBridges
        jsBridgeHandlerFactories.add(OpenWifiSettingJsBridge.factory())
        jsBridgeHandlerFactories.add(GoBrowserJsBridge.factory())
        jsBridgeHandlerFactories.add(RealTimePositionJsBridge.factory(4000))
        jsBridgeHandlerFactories.add(com.radiuswallet.uniweb.jsbridge.common.CallPhoneJsBridge.factory())
        jsBridgeHandlerFactories.add(ClipboardJsBridge.factory())
        jsBridgeHandlerFactories.add(CameraJsBridge.factory(80))
        jsBridgeHandlerFactories.add(PictureChooseJsBridge.factory(80))
        jsBridgeHandlerFactories.add(CloseWebViewJsBridge.factory())
        jsBridgeHandlerFactories.add(RequestedOrientationJsBridge.factory())
        jsBridgeHandlerFactories.add(CheckWxInstallJsBridge.factory())
        jsBridgeHandlerFactories.add(ImageSaveJsBridge.factory())
        jsBridgeHandlerFactories.add(OpenWebViewJsBridge.factory { webViewOwner: WebViewOwner, url: String, title: String? ->
            webViewOwner.openNewWebView(url, title)
        })
        val jsBridgeDispatcher = WebCallJsBridgeDispatcher.create(
            fragmentWebViewOwner,
            jsBridgeHandlerFactories,
            webView.javascriptCaller
        )
        //带上额外的独立 JsBridges
        supportJavascriptInterface.registerJsBridgeDispatcher(jsBridgeDispatcher)
        webView.addJavascriptInterface(supportJavascriptInterface, "app")
    }


    protected open fun onWebViewInitialized(
        webView: UniWebView,
        initialUrl: String,
        savedInstanceState: Bundle?,
    ) {
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            webView.loadUrl(initialUrl)
        }
    }


    protected open fun initOnBackPressedCallback(
        webView: UniWebView,
        onBackPressedDispatcher: OnBackPressedDispatcher,
    ) {
        onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fragmentWebViewOwner.dispatchActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        fragmentWebViewOwner.dispatchRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )
    }

    fun goBack() {
        webView.goBack()
    }

    fun goForward() {
        webView.goForward()
    }

    fun reload() {
        webView.reload()
    }

    fun goBackOrForward(index: Int) {
        webView.goBackOrForward(index)
    }

    fun getWebView(): WebView? {
        return if (::webView.isInitialized) {
            webView
        } else {
            null
        }
    }

    fun getWebViewOwner(): WebViewOwner? {
        return if (::fragmentWebViewOwner.isInitialized) {
            fragmentWebViewOwner
        } else {
            null
        }
    }

    fun resetWebSettings() {
        webDefinition.webSettingsFunc.invoke(initialUrl, webView.settings)
    }

    @CallSuper
    protected open fun getCookieConfig(): Map<String, List<String>> {
        return webDefinition.cookieMapFunc.invoke()
    }

    protected open fun createFragmentWebViewOwner(): FragmentWebViewOwner {
        return FragmentWebViewOwner(
            requireContext(),
            this,
            openNewWebViewFunc = { url: String, title: String? ->
                context?.let {
                    com.radiuswallet.uniweb.UniWebActivity.Companion.start(it,
                        url = url,
                        title = title,
                        webModuleKey = arguments?.getString(com.radiuswallet.uniweb.BaseWebFragment.Companion.K_WEB_MODULE_KEY))
                }
            },
            webViewUrlFunc = {
                getWebView()?.url
            }
        )
    }

    companion object {
        const val URL_BLANK_PAGE = "about:blank"

        private const val K_ARG_URL = "url"
        private const val K_WEB_MODULE_KEY = "web_module_key"

        fun constructArguments(url: String, webModuleKey: String? = null): Bundle {
            val args = Bundle()
            args.putString(com.radiuswallet.uniweb.BaseWebFragment.Companion.K_ARG_URL, url)
            args.putString(com.radiuswallet.uniweb.BaseWebFragment.Companion.K_WEB_MODULE_KEY,
                webModuleKey)
            return args
        }

    }
}