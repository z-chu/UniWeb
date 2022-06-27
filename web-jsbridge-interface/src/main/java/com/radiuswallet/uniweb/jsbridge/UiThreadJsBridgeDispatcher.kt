package com.radiuswallet.uniweb.jsbridge

import android.os.Handler
import android.os.Looper
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock

abstract class UiThreadJsBridgeDispatcher : JsBridgeDispatcher {

    private val handler: Handler = Handler(Looper.getMainLooper())

    final override fun dispatchWebCall(string: String): Boolean {
        val allow = AtomicBoolean(false)
        waitUntilAllow(allow, string)
        return allow.get()
    }

    abstract fun dispatchWebCallOnUiThread(string: String): Boolean

    private fun waitUntilAllow(allow: AtomicBoolean, args: String) {
        val lock = ReentrantLock()
        val allowFinishedCond = lock.newCondition()
        val allowFinished = AtomicBoolean()
        handler.post {
            try {
                lock.lock()
                allow.set(dispatchWebCallOnUiThread(args))
                allowFinished.set(true)
                allowFinishedCond.signal()
            } catch (e: Exception) {
                Timber.tag(TAG_WEB_LOG).e(e)
            } finally {
                lock.unlock()
            }
        }

        try {
            lock.lock()
            for (i in 0..10) {
                if (!allowFinished.get()) {
                    allowFinishedCond.await(200, TimeUnit.MILLISECONDS)
                } else {
                    break
                }
            }
        } catch (e: Exception) {
            Timber.tag(TAG_WEB_LOG).e(e)
        } finally {
            lock.unlock()
        }
    }

}