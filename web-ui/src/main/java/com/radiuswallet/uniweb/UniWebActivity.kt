package com.radiuswallet.uniweb

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager

open class UniWebActivity : AppCompatActivity() {

    protected lateinit var initialUrl: String
        private set

    protected val webViewModel by viewModels<UniWebViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layoutId = contentViewLayoutId
        if (layoutId == null) {
            val fragmentContainerView = FragmentContainerView(this)
            fragmentContainerView.id = R.id.root_fragment_container
            setContentView(fragmentContainerView)
        } else {
            setContentView(layoutId)
        }
        initialUrl = prepareInitialUrl(intent.getStringExtra(K_EXTRA_URL))
        replaceWebFragment(
            supportFragmentManager,
            webContainerViewId,
        )
    }


    open val contentViewLayoutId: Int?
        get() = null

    open val webContainerViewId: Int
        get() = R.id.root_fragment_container

    private fun replaceWebFragment(
        fragmentManager: FragmentManager,
        @IdRes containerViewId: Int,
    ) {
        val findFragmentByTag = fragmentManager.findFragmentByTag(TAG_UNIWEB_FRAGMENT)
        if (findFragmentByTag == null) {
            val intent = intent
            val webModuleKey = intent?.getStringExtra(K_WEB_MODULE_KEY)
            val title = intent?.getStringExtra(K_EXTRA_TITLE)
            val isShowToolbar = intent?.getBooleanExtra(K_IS_SHOW_TOOLBAR, true) ?: true
            fragmentManager
                .beginTransaction()
                .replace(
                    containerViewId,
                    createWebFragment(initialUrl, title, webModuleKey, isShowToolbar),
                    TAG_UNIWEB_FRAGMENT
                )
                .commit()
        }
    }

    open fun prepareInitialUrl(fromExtraUrl: String?): String {
        return fromExtraUrl ?: com.radiuswallet.uniweb.BaseWebFragment.URL_BLANK_PAGE
    }

    open fun createWebFragment(
        url: String,
        title: String?,
        webModuleKey: String?,
        isShowToolbar: Boolean,
    ): UniWebFragment {
        return UniWebFragment.newInstance(url, title, webModuleKey, isShowToolbar)
    }

    fun getUniWebFragment(): UniWebFragment? {
        return supportFragmentManager.findFragmentByTag(TAG_UNIWEB_FRAGMENT) as? UniWebFragment
    }


    companion object {
        private const val TAG_UNIWEB_FRAGMENT = "uniweb_fragment"
        const val K_EXTRA_URL = "url"
        const val K_EXTRA_TITLE = "title"
        const val K_WEB_MODULE_KEY = "web_module_key"
        const val K_IS_SHOW_TOOLBAR = "isShowToolbar"

        @JvmOverloads
        @JvmStatic
        fun start(
            context: Context,
            url: String,
            title: String? = null,
            webModuleKey: String? = null,
            isShowToolbar: Boolean = true,
        ) {
            val starter = Intent(context, UniWebActivity::class.java)
                .putExtra(K_EXTRA_URL, url)
                .putExtra(K_EXTRA_TITLE, title)
                .putExtra(K_IS_SHOW_TOOLBAR, isShowToolbar)
                .putExtra(K_WEB_MODULE_KEY, webModuleKey)
            context.startActivity(starter)
        }


    }


}