package com.radiuswallet.uniweb.model


sealed class PageLoadState {
    object Idle : PageLoadState()
    data class Loading(val url: String) : PageLoadState()
    data class Success(val url: String) : PageLoadState()
}