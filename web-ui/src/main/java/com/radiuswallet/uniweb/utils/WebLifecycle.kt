package com.radiuswallet.uniweb.utils

import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.tencent.smtt.sdk.WebView

fun WebView.bindLifecycle(lifecycle: Lifecycle) {
    if (lifecycle.currentState != Lifecycle.State.DESTROYED) {
        lifecycle.addObserver(WebLifecycleEventObserver(this))
    }
}

fun WebView.bindLifecycle(lifecycleOwner: LifecycleOwner) {
    bindLifecycle(lifecycleOwner.lifecycle)
}

class WebLifecycleEventObserver(private val webView: WebView) : LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_RESUME) {
            webView.resumeTimers()
        } else if (event == Lifecycle.Event.ON_PAUSE) {
            webView.pauseTimers()
        } else if (event == Lifecycle.Event.ON_DESTROY) {
            val parent = webView.parent
            if (parent != null) {
                (parent as ViewGroup).removeView(webView)
            }
            webView.stopLoading()
            webView.settings.javaScriptEnabled = false
            webView.clearHistory()
            webView.clearView()
            webView.removeAllViews()
            webView.destroy()
            source.lifecycle.removeObserver(this)
        }
    }

}