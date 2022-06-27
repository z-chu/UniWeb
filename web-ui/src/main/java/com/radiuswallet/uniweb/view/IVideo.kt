package com.radiuswallet.uniweb.view

import android.view.View
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient

interface IVideo {

    /**
     * 当 H5 需要开启视频全屏的调用，请在该方法实现全屏的 UI 逻辑
     */
    fun onShowCustomView(view: View, callback: IX5WebChromeClient.CustomViewCallback)

    /**
     * 当 H5 需要关闭视频全屏的调用
     */
    fun onHideCustomView()

    /**
     * 当前的视频全屏状态
     * false : 视频未全屏。 true: 视频已全屏
     */
    val isVideoState: Boolean
}