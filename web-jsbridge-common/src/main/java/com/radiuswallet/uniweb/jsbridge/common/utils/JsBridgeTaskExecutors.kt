package com.radiuswallet.uniweb.jsbridge.common.utils

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * 仅供内部使用的 线程池
 */
object JsBridgeTaskExecutors {

    private val asyncExecutorService: ExecutorService by lazy {
        Executors.newFixedThreadPool(2, object : ThreadFactory {
            private val THREAD_NAME_STEM = "js_bridge_async_%d"
            private val mThreadId = AtomicInteger(0)
            override fun newThread(r: Runnable): Thread {
                val t = Thread(r)
                t.name = String.format(THREAD_NAME_STEM, mThreadId.getAndIncrement())
                return t
            }
        })
    }

    val asyncThreadExecutor = Executor { command -> asyncExecutorService.execute(command) }

    private val mainHandler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    val mainThreadExecutor = Executor { command ->
        if (Looper.getMainLooper().thread === Thread.currentThread()) {
            command.run()
        } else {
            mainHandler.post(command)
        }
    }


}