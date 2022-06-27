package com.radiuswallet.uniweb.view

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import com.radiuswallet.uniweb.jsbridge.TAG_WEB_LOG
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient
import com.tencent.smtt.sdk.WebView
import timber.log.Timber

class VideoImpl(private val mActivity: Activity?, private val mWebView: WebView?) : IVideo {
    private var flags: HashSet<Pair<Int, Int>> = HashSet()
    private var movieView: View? = null
    private var movieParentView: ViewGroup? = null
    private var mCallback: IX5WebChromeClient.CustomViewCallback? = null

    override fun onShowCustomView(view: View, callback: IX5WebChromeClient.CustomViewCallback) {
        if (this.mActivity == null || mActivity.isFinishing) {
            return
        }
        try {
            mActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } catch (e: Exception) {
            Timber.tag(TAG_WEB_LOG).e(e)
        }
        val mWindow = mActivity.window
        // 保存当前屏幕的状态
        if (mWindow.attributes.flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON == 0) {
            mWindow.setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
            flags.add(Pair(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 0))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && mWindow.attributes.flags and WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED == 0) {
            mWindow.setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            )
            flags.add(Pair(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, 0))
        }
        if (movieView != null) {
            callback.onCustomViewHidden()
            return
        }
        if (mWebView != null) {
            mWebView.visibility = View.GONE
        }
        if (movieParentView == null) {
            val mDecorView = mActivity.window.decorView as FrameLayout
            val frameLayout = FrameLayout(mActivity)
            frameLayout.setBackgroundColor(Color.BLACK)
            frameLayout.requestDisallowInterceptTouchEvent(true)
            mDecorView.addView(frameLayout)
            movieParentView = frameLayout
        }
        mCallback = callback
        movieParentView?.let { parent ->
            movieView = view
            parent.addView(view)
            parent.visibility == View.VISIBLE
        }
    }

    override fun onHideCustomView() {
        val localMovieView = movieView ?: return
        if (mActivity != null && mActivity.requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            try {
                mActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } catch (e: Exception) {
                Timber.tag(TAG_WEB_LOG).e(e)
            }
        }
        if (flags.isNotEmpty()) {
            for (mPair in flags) {
                mActivity!!.window.setFlags(mPair.second, mPair.first)
            }
            flags.clear()
        }

        localMovieView.visibility = View.GONE
        movieParentView?.let {
            it.removeView(localMovieView)
            it.visibility = View.GONE
        }
        mCallback?.onCustomViewHidden()
        movieView = null
        mWebView?.visibility = View.VISIBLE
    }

    override val isVideoState: Boolean
        get() = movieView != null


}