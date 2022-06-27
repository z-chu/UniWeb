package com.radiuswallet.uniweb.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.core.view.updatePadding
import com.google.android.material.appbar.MaterialToolbar
import com.radiuswallet.uniweb.R
import timber.log.Timber

/**
 * 仅供内部使用,内部随时会修改API，外部要使用相同效果的 Toolbar ，请另外 copy
 */
class UniWebToolbar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.toolbarStyle,
) : MaterialToolbar(context, attrs, defStyleAttr) {


    private val closeButtonWidth: Int =
        context.resources.getDimensionPixelSize(R.dimen.uniweb_toolbar_close_button_width)

    private val titleCloseButtonInterval: Int =
        context.resources.getDimensionPixelSize(R.dimen.uniweb_toolbar_title_close_button_interval)

    private val closeButtonView: ImageView by lazy {
        val appCompatImageButton = try {
            LayoutInflater.from(getContext())
                .inflate(R.layout.uniweb_action_item_close, this, false)
        } catch (throwable: Throwable) {
            Timber.e("The theme Not set attr toolbarNavigationButtonStyle")
            LayoutInflater.from(getContext())
                .inflate(R.layout.uniweb_action_item_close_try, this, false)
        }

        appCompatImageButton.isClickable = true
        appCompatImageButton as ImageView
    }

    private var _titleTextView: View? = null
    private val titleTextView: View?
        get() {
            _titleTextView?.let { return it }
            val local = com.radiuswallet.uniweb.utils.ToolbarUtils.getTitleTextView(this)
            _titleTextView = local
            return local
        }

    private var _subtitleTextView: View? = null
    private val subtitleTextView: View?
        get() {
            _subtitleTextView?.let { return it }
            val local = com.radiuswallet.uniweb.utils.ToolbarUtils.getSubtitleTextView(this)
            _subtitleTextView = local
            return local
        }

    var isShowCloseButton: Boolean = false
        set(value) {
            field = value
            if (value) {
                closeButtonView.visibility = View.VISIBLE
                titleTextView?.updatePadding(left = titleCloseButtonInterval,
                    right = titleCloseButtonInterval)
                subtitleTextView?.updatePadding(left = titleCloseButtonInterval,
                    right = titleCloseButtonInterval)
            } else {
                closeButtonView.visibility = View.GONE
                titleTextView?.updatePadding(left = 0, right = 0)
                subtitleTextView?.updatePadding(left = 0, right = 0)
            }
        }

    init {
        addView(closeButtonView)
    }


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        closeButtonView.visibility = GONE
        super.onLayout(changed, left, top, right, bottom)
        val navigationIconButton =
            com.radiuswallet.uniweb.utils.ToolbarUtils.getNavigationIconButton(this)
        val closeLeft = navigationIconButton?.right ?: 0
        if (navigationIconButton != null) {
            if (isShowCloseButton) {
                closeButtonView.visibility = VISIBLE
                closeButtonView.layout(closeLeft,
                    navigationIconButton.top,
                    closeLeft + closeButtonWidth,
                    navigationIconButton.bottom)
            }
        } else {
            //TODO 判断 navigationIconButton 不显示的状态
        }
    }

    fun setCloseOnClickListener(listener: OnClickListener?) {
        closeButtonView.setOnClickListener(listener)
    }

    var closeIcon: Drawable?
        get() = closeButtonView.drawable
        set(value) {
            closeButtonView.setImageDrawable(value)
        }


}