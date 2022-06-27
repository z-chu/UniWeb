package com.radiuswallet.uniweb

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radiuswallet.uniweb.model.MainFrameError
import com.radiuswallet.uniweb.model.PageLoadState

class UniWebViewModel : ViewModel() {

    /**
     * 第一页的加载状态
     */
    internal val _pageLoadState = MutableLiveData<PageLoadState>(PageLoadState.Idle)
    val pageLoadState: LiveData<PageLoadState> = _pageLoadState

    internal val _mainFrameError = MutableLiveData<MainFrameError>()
    val mainFrameError: LiveData<MainFrameError> = _mainFrameError

    private val _progress = MutableLiveData<Int>()
    val progress: LiveData<Int>
        get() = _progress


    internal val _webTitle = MutableLiveData<String>()
    val webTitle: LiveData<String>
        get() = _webTitle


    fun receiveTitle(title: String?) {
        _webTitle.value = title
    }

    fun changeLoadingProgress(newProgress: Int) {
        _progress.value = newProgress
    }


    fun startLoadPage(url: String) {
        _pageLoadState.value = PageLoadState.Loading(url)
    }

    fun finishLoadingPage(url: String) {
        _pageLoadState.value = PageLoadState.Success(url)
    }

    fun receiveLoadingError(url: String, errorCode: Int) {
        // Should ignore error for local assets even it gives ERROR_HOST_LOOKUP
        if (Uri.parse(url).authority != "appassets.androidplatform.net") {
            _mainFrameError.value = MainFrameError(url, errorCode)
        }
    }
}