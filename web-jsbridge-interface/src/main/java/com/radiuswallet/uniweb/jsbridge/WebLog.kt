package com.radiuswallet.uniweb.jsbridge

import timber.log.Timber

const val TAG_WEB_LOG = "uniweb"

/**
 * 用于截断日志，避免日志显示过长
 */
internal fun String.subTimberLog(maxLength: Int = 300): String {
    if (Timber.treeCount == 0) {
        return this
    }
    return if (this.length < maxLength) {
        this
    } else {
        this.substring(0, maxLength) + "..."
    }
}
