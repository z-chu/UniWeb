package com.radiuswallet.uniweb

import com.radiuswallet.uniweb.jsbridge.common.owner.WebViewOwner


interface UniWebViewOwner {

    fun setCloseButtonVisibility(isShow: Boolean)

    fun setTitleMenuItem(
        title: String,
        titleColor: Int?,
        textSize: Int?,
        onClick: (() -> Unit)?,
    )


    fun setIconMenuItem(
        iconUrl: String,
        title: String?,
        onClick: (() -> Unit)?,
    )

    fun clearMenuItems()

    fun setToolbarStyle(backgroundColor: Int?, titleColor: Int?, IconColor: Int?)

    fun setToolbarTitle(title: String?)

    fun setNavigationOnClickListener(onClick: (() -> Unit)?)

    fun getBaseWebViewOwner(): WebViewOwner?

}