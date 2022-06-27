package com.radiuswallet.uniweb

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.ActionMenuItemView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.appbar.AppBarLayout
import com.radiuswallet.uniweb.jsbridge.UniUIJsBridge
import com.radiuswallet.uniweb.jsbridge.WebCallJsBridgeDispatcher
import com.radiuswallet.uniweb.jsbridge.common.owner.WebViewOwner
import com.radiuswallet.uniweb.model.PageLoadState
import com.radiuswallet.uniweb.module.UniWebDefinition
import com.radiuswallet.uniweb.module.UniWebModule
import com.radiuswallet.uniweb.utils.clearAllData
import com.radiuswallet.uniweb.utils.copyTextToClipboard
import com.radiuswallet.uniweb.utils.openInBrowser
import com.radiuswallet.uniweb.view.UniWebToolbar
import com.radiuswallet.uniweb.view.UniWebView

open class UniWebFragment : com.radiuswallet.uniweb.BaseWebFragment(), UniWebViewOwner {

    //toolbar UI
    private lateinit var appbar: AppBarLayout
    private lateinit var toolbar: UniWebToolbar
    private lateinit var progressBar: ProgressBar
    private var readyNavigationOnClickListener: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.uniweb_web_fragment, container, false)
    }


    override fun lazyUniWebView(view: View): UniWebView {
        return view.findViewById(R.id.uniweb_web_view)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        setupToolbar(toolbar)
        setToolbarVisible(arguments?.getBoolean(K_IS_SHOW_TOOLBAR, true) != false)
        toolbar.title = arguments?.getString(K_TITLE)
        observeLiveData()
    }

    private fun initView(view: View) {
        //toolbar UI
        appbar = view.findViewById(R.id.appbar)
        toolbar = view.findViewById(R.id.toolbar)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupToolbar(toolbar: UniWebToolbar) {
        val activity = activity
        if (activity is AppCompatActivity) {
            activity.setSupportActionBar(toolbar)
            activity.supportActionBar?.let {
                it.setDisplayHomeAsUpEnabled(true)
                it.title = null
            }
        }

        toolbar.setNavigationOnClickListener {
            readyNavigationOnClickListener?.let {
                it.invoke()
                return@setNavigationOnClickListener
            }
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
        toolbar.setCloseOnClickListener {
            activity?.finish()
        }
    }

    fun setToolbarVisible(isVisible: Boolean) {
        if (isVisible) {
            toolbar.visibility = View.VISIBLE
        } else {
            toolbar.visibility = View.GONE
        }
    }

    private fun observeLiveData() {
        viewModel.webTitle.observe(viewLifecycleOwner) {
            setToolbarTitle(it)
        }
        viewModel.progress.observe(viewLifecycleOwner) {
            progressBar.progress = it
            if (it == 100) {
                progressBar.visibility = View.GONE
            } else {
                progressBar.visibility = View.VISIBLE
            }
        }
        viewModel.pageLoadState.observe(viewLifecycleOwner) {
            if (it is PageLoadState.Success) {
                setCloseButtonVisibility(getWebView()?.canGoBack() == true)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> {
                webView.reload()
            }
            R.id.action_copy_url -> {
                webView.url?.let {
                    context?.copyTextToClipboard("web link", it)
                }
                context?.let {
                    Toast.makeText(it, "已复制", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.action_open_url -> {
                webView.url?.let { context?.openInBrowser(it) }
            }
            R.id.action_clean_cache -> {
                webView.let {
                    it.clearAllData()
                    it.reload()
                }
            }
            R.id.action_close -> {
                activity?.finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setTitleMenuItem(
        title: String,
        titleColor: Int?,
        textSize: Int?,
        onClick: (() -> Unit)?,
    ) {
        toolbar.menu.clear()
        val menuItem = toolbar.menu.add(title)
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        val actionMenuView = com.radiuswallet.uniweb.utils.ToolbarUtils.getActionMenuView(toolbar)
        val childAt = actionMenuView?.getChildAt(0)
        if (childAt is ActionMenuItemView) {
            titleColor?.let { childAt.setTextColor(it) }
            textSize?.toFloat()?.let { childAt.textSize = it }
        }
        menuItem.setOnMenuItemClickListener {
            onClick?.invoke()
            return@setOnMenuItemClickListener onClick != null
        }
    }

    override fun setIconMenuItem(iconUrl: String, title: String?, onClick: (() -> Unit)?) {
        Glide.with(this).load(iconUrl).into(object : SimpleTarget<Drawable?>() {
            override fun onResourceReady(
                resource: Drawable,
                transition: Transition<in Drawable?>?,
            ) {
                toolbar.menu.clear()
                val menuItem = toolbar.menu.add(title ?: "")
                menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                menuItem.icon = resource
                menuItem.setOnMenuItemClickListener {
                    onClick?.invoke()
                    return@setOnMenuItemClickListener onClick != null
                }
            }
        })
    }

    override fun clearMenuItems() {
        toolbar.menu.clear()
    }

    override fun setToolbarStyle(backgroundColor: Int?, titleColor: Int?, IconColor: Int?) {
        backgroundColor?.let {
            toolbar.setBackgroundColor(it)
            val window = activity?.window
            if (window != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.statusBarColor = it
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!isColorDark(it)) {
                        window.decorView.systemUiVisibility =
                            window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

                    } else {
                        window.decorView.systemUiVisibility =
                            window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                    }
                }
            }
        }
        titleColor?.let { color ->
            toolbar.setTitleTextColor(color)
            toolbar.navigationIcon?.let {
                val mutate = it.mutate()
                mutate.colorFilter =
                    PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
                toolbar.navigationIcon = mutate
            }
            toolbar.closeIcon?.let {
                val mutate = it.mutate()
                mutate.colorFilter =
                    PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
                toolbar.closeIcon = mutate
            }
        }
    }

    override fun setToolbarTitle(title: String?) {
        toolbar.title = title
    }

    override fun setNavigationOnClickListener(onClick: (() -> Unit)?) {
        readyNavigationOnClickListener = onClick
    }

    override fun getBaseWebViewOwner(): WebViewOwner? {
        return getWebViewOwner()
    }

    fun getUniWebViewOwner(): UniWebViewOwner {
        return this
    }

    override fun setCloseButtonVisibility(isShow: Boolean) {
        toolbar.isShowCloseButton = isShow
    }


    private fun isColorDark(@ColorInt color: Int): Boolean {
        val darkness: Double = (1
                - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255)
        return darkness >= 0.5
    }

    @CallSuper
    override fun createWebDefinition(webModule: UniWebModule): UniWebDefinition {
        val defineWebDefinition = super.createWebDefinition(webModule)
        defineWebDefinition.jsBridgeDispatcher {
            WebCallJsBridgeDispatcher.create(
                this@UniWebFragment,
                listOf(
                    UniUIJsBridge.factory()
                ),
                it
            )
        }
        return defineWebDefinition
    }


    companion object {

        const val K_TITLE = "title"
        const val K_IS_SHOW_TOOLBAR = "isShowToolbar"

        @JvmStatic
        fun newInstance(
            url: String,
            title: String? = null,
            webModuleKey: String? = null,
            isShowToolbar: Boolean = true,
        ): UniWebFragment {
            val fragment = UniWebFragment()
            fragment.arguments = constructArguments(url, title, webModuleKey, isShowToolbar)
            return fragment
        }


        fun constructArguments(
            url: String,
            title: String? = null,
            webModuleKey: String? = null,
            isShowToolbar: Boolean = true,
        ): Bundle {
            val args = com.radiuswallet.uniweb.BaseWebFragment.constructArguments(url, webModuleKey)
            args.putString(K_TITLE, title)
            args.putBoolean(K_IS_SHOW_TOOLBAR, isShowToolbar)
            return args
        }
    }
}

