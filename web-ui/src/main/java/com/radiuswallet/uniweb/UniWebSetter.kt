package com.radiuswallet.uniweb

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import com.radiuswallet.uniweb.jsbridge.TAG_WEB_LOG
import com.radiuswallet.uniweb.jsbridge.common.owner.WebViewOwner
import com.radiuswallet.uniweb.module.UniWebModule
import com.radiuswallet.uniweb.module.uniWebModule
import com.radiuswallet.uniweb.view.IFileChooser
import com.radiuswallet.uniweb.view.IVideo
import com.tencent.smtt.export.external.TbsCoreSettings
import com.tencent.smtt.sdk.QbSdk
import com.tencent.smtt.sdk.TbsListener
import timber.log.Timber


object UniWebSetter {


    private var defaultWebModule: UniWebModule = uniWebModule { }
    private val webModuleMap = HashMap<String, UniWebModule>()
    internal fun getWebModule(key: String? = null): UniWebModule {
        val custom = key?.let { webModuleMap[key] }
        return if (custom != null) {
            custom
        } else {
            if (key != null) {
                Timber.tag(TAG_WEB_LOG)
                    .w("未能找到与 webModuleKey=$key 相匹配的 webModule ,所以使用 defaultWebModule ")
            }
            defaultWebModule
        }
    }

    fun setWebModule(webModule: UniWebModule) {
        this.defaultWebModule = webModule
    }

    fun setWebModule(key: String, webModule: UniWebModule) {
        webModuleMap[key] = webModule
    }


    private var iFileChooserFunc: ((WebViewOwner) -> IFileChooser)? = null
        private set

    internal fun getIFileChooser(webViewOwner: WebViewOwner): IFileChooser? {
        return iFileChooserFunc?.invoke(webViewOwner)
    }

    fun setIFileChooser(func: ((WebViewOwner) -> IFileChooser)?) {
        this.iFileChooserFunc = func
    }


    internal var iVideoFunc: ((Fragment, View) -> IVideo)? = null
        private set

    internal fun getIVideo(fragment: Fragment, webView: View): IVideo? {
        return iVideoFunc?.invoke(fragment, webView)
    }

    fun setIVideo(func: ((fragment: Fragment, webView: View) -> IVideo)? = null) {
        this.iVideoFunc = func
    }


    fun init(context: Context) {
        val startTime = System.currentTimeMillis()
        Timber.tag(TAG_WEB_LOG).d("initX5 start：%d", System.currentTimeMillis())
        /**
         *  1.1 设置开启优化方案, 在调用TBS初始化、创建WebView之前进行如下配置
         */
        QbSdk.initTbsSettings(
            mutableMapOf<String, Any>(
                TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER to true,
                TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE to true
            )
        )
        Timber.tag(TAG_WEB_LOG).i("initX5>>开始加载内核")
        QbSdk.initX5Environment(context, object : QbSdk.PreInitCallback {
            override fun onCoreInitFinished() {
            }

            override fun onViewInitFinished(p0: Boolean) {
                if (p0) {
                    Timber.tag(TAG_WEB_LOG).i("initX5>>内核加载成功")
                } else {
                    Timber.tag(TAG_WEB_LOG).i("initX5>>内核加载失败")
                }
            }

        })

        QbSdk.setDownloadWithoutWifi(true)
        QbSdk.setTbsListener(object : TbsListener {
            override fun onDownloadFinish(progress: Int) {
                Timber.tag(TAG_WEB_LOG).i("initX5>>下载完成 progress = %d", progress)
            }

            override fun onInstallFinish(progress: Int) {
                Timber.tag(TAG_WEB_LOG).i("initX5>>正在安装内核 progress = %d", progress)
            }

            override fun onDownloadProgress(progress: Int) {
                Timber.tag(TAG_WEB_LOG).i("initX5>>已经下载 progress = %d", progress)
            }
        })
        Timber.tag(TAG_WEB_LOG).d("initX5 end：%d", System.currentTimeMillis() - startTime)

    }
}