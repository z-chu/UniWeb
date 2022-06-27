package com.radiuswallet.webexample

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.multidex.MultiDex
import com.radiuswallet.uniweb.UniWebSetter
import com.radiuswallet.uniweb.jsbridge.common.OpenAppJsBridge
import com.radiuswallet.uniweb.module.uniWebModule
import timber.log.Timber

class BaseApp : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

    }


    override fun onCreate() {
        super.onCreate()
        UniWebSetter.init(this)
        //设置单独的 main webModule
        UniWebSetter.setWebModule("separate", separateWebModule)
        //设置默认的 main webModule
        UniWebSetter.setWebModule(uniWebModule {
            cookies {
                mapOf(
                    "github.com" to listOf("asa=zchu1")
                )
            }

            headers { url, headers ->
                val map = HashMap<String, String>()
                map.putAll(headers)
                map.put("TEST_KEY", "test header value")
                map
            }

            jsBridgeHandlerFactories {
                listOf(OpenAppJsBridge.factory { owner, uri ->
                    Toast.makeText(this@BaseApp,
                        "我是默认的 mainWebModule, 我我哦我跳转到：" + uri,
                        Toast.LENGTH_SHORT).show()
                })
            }
        })
    }
}