package com.radiuswallet.uniweb.jsbridge.common.owner

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.SparseArray
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.util.containsKey
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.radiuswallet.uniweb.jsbridge.TAG_WEB_LOG
import com.radiuswallet.uniweb.jsbridge.common.R
import com.radiuswallet.uniweb.jsbridge.common.utils.JsBridgePhotoFileProvider
import com.radiuswallet.uniweb.jsbridge.common.utils.SystemCameraTakePhoto
import com.radiuswallet.uniweb.jsbridge.common.utils.SystemFileChooser
import com.radiuswallet.uniweb.jsbridge.common.utils.SystemPhotoSelector
import com.radiuswallet.uniweb.jsbridge.common.utils.showSettingAlertDialog
import timber.log.Timber
import java.io.File
import java.util.*

open class ActivityWebViewOwner(
    override val activity: AppCompatActivity,
    val openNewWebViewFunc: ((url: String, title: String?) -> Unit)?,
    val webViewUrlFunc: (() -> String?)?,
) : WebViewOwner {
    private val systemPhotoSelectors = SparseArray<SystemPhotoSelector>()
    private val systemCameraTakePhotos = SparseArray<SystemCameraTakePhoto>()
    private val systemFileChoosers = SparseArray<SystemFileChooser>()
    private val codeGenerator = Random()
    private val requestPermissionsCallbacks: SparseArray<(IntArray) -> Unit> = SparseArray()
    private val activityResultCallbacks: SparseArray<(resultCode: Int, data: Intent?) -> Unit> =
        SparseArray()
    private val activityResultTags = HashMap<String, Int>()


    override val context: Context
        get() = activity

    override val fragmentManager: FragmentManager
        get() = activity.supportFragmentManager


    override val lifecycle: Lifecycle
        get() = activity.lifecycle

    override fun setRequestedOrientation(requestedOrientation: Int) {
        try {
            activity.requestedOrientation = requestedOrientation
        } catch (e: Exception) {
            Log.e("ActivityWebViewOwner", e.localizedMessage ?: "setRequestedOrientation error")
        }
    }


    override fun takePhotoWithCamera(onResult: (File?) -> Unit) {
        val generateRequestCode = generateRequestCode()
        val generateOutputMediaFile = JsBridgePhotoFileProvider.generateOutputMediaFile(context)
        if (generateOutputMediaFile != null) {
            val systemCameraTakePhoto = SystemCameraTakePhoto(
                activity = activity,
                requestCode = generateRequestCode,
                permissionRequestCode = generateRequestCode,
                fileProviderAuthority = JsBridgePhotoFileProvider.getAuthority(context),
                onPermissionDenied = {
                    onResult.invoke(null)
                    showPermissionDeniedDialog(
                        activity.getString(R.string.permission_denied_take_photo_with_camera)
                    )
                },
                onSelected = {
                    onResult.invoke(it)
                })
            systemCameraTakePhoto.startImageCapture(generateOutputMediaFile)
            systemCameraTakePhotos.put(generateRequestCode, systemCameraTakePhoto)
        }
    }

    override fun shootVideo(
        highQuality: Boolean,
        durationLimit: Int?,
        sizeLimit: Int?,
        onResult: (File?) -> Unit,
    ) {
        val generateRequestCode = generateRequestCode()
        val generateOutputMediaFile = JsBridgePhotoFileProvider.generateOutputMediaFile(
            context, JsBridgePhotoFileProvider.MEDIA_TYPE_VIDEO
        )
        if (generateOutputMediaFile != null) {
            val systemCameraTakePhoto = SystemCameraTakePhoto(
                activity = activity,
                requestCode = generateRequestCode,
                permissionRequestCode = generateRequestCode,
                fileProviderAuthority = JsBridgePhotoFileProvider.getAuthority(context),
                onPermissionDenied = {
                    onResult.invoke(null)
                    showPermissionDeniedDialog(
                        activity.getString(R.string.permission_denied_take_photo_with_camera)
                    )
                },
                onSelected = {
                    onResult.invoke(it)
                })
            systemCameraTakePhoto.startVideoCapture(
                generateOutputMediaFile,
                highQuality,
                durationLimit,
                sizeLimit
            )
            systemCameraTakePhotos.put(generateRequestCode, systemCameraTakePhoto)
        }
    }

    override fun choosePhotoFromGallery(onResult: (File?) -> Unit) {
        val generateRequestCode = generateRequestCode()
        val systemPhotoSelector = SystemPhotoSelector(
            activity = activity,
            requestCode = generateRequestCode,
            permissionRequestCode = generateRequestCode,
            onPermissionDenied = {
                onResult.invoke(null)
                showPermissionDeniedDialog(
                    activity.getString(R.string.permission_denied_choose_photo_from_gallery)
                )
            },
            onError = {
                Timber.tag(TAG_WEB_LOG).e(it)
                Toast.makeText(
                    activity,
                    R.string.message_get_selected_album_picture_failed,
                    Toast.LENGTH_SHORT
                ).show()
            },
            onSelected = {
                onResult.invoke(it?.let { File(it) })
            }
        )
        systemPhotoSelector.selectFromGallery()
        systemPhotoSelectors.put(generateRequestCode, systemPhotoSelector)
    }

    override fun openFileChooser(acceptType: String, onSelected: (uris: Array<Uri>?) -> Unit) {
        createSystemFileChooser(onSelected).openFileChooser(acceptType)
    }

    override fun openFileChooser(intent: Intent, onSelected: (uris: Array<Uri>?) -> Unit) {
        createSystemFileChooser(onSelected).openFileChooser(intent)
    }

    private fun createSystemFileChooser(onSelected: (uris: Array<Uri>?) -> Unit): SystemFileChooser {
        val generateRequestCode = generateRequestCode()
        val systemFileChooser = SystemFileChooser(
            activity = activity,
            requestCode = generateRequestCode,
            permissionRequestCode = generateRequestCode,
            onPermissionDenied = {
                onSelected.invoke(null)
                showPermissionDeniedDialog(
                    "??????????????????????????????"
                )
            },
            onSelected = onSelected
        )
        systemFileChoosers.put(generateRequestCode, systemFileChooser)
        return systemFileChooser
    }

    override fun requestPermissions(
        permissions: Array<String>,
        callback: (grantResults: IntArray) -> Unit,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val generateRequestCode = generateRequestCode()
            requestPermissionsCallbacks.put(generateRequestCode, callback)
            ActivityCompat.requestPermissions(activity, permissions, generateRequestCode)
        } else {
            callback.invoke(IntArray(permissions.size) { PackageManager.PERMISSION_GRANTED })
        }
    }


    override fun startActivityForResult(
        intent: Intent,
        action: String,
        callback: (resultCode: Int, data: Intent?) -> Unit,
    ) {
        val generateRequestCode = activityResultTags.getOrPut(action) {
            generateRequestCode()
        }
        activityResultCallbacks.put(generateRequestCode, callback)
        activity.startActivityForResult(intent, generateRequestCode)
    }

    override fun closeCurrentWebView() {
        activity.finish()
    }

    override fun openNewWebView(url: String, title: String?) {
        if (openNewWebViewFunc != null) {
            openNewWebViewFunc.invoke(url, title)
        } else {
            Timber.tag(TAG_WEB_LOG).w("????????? openNewWebViewFunc ????????? openNewWebView ??????")
        }
        openNewWebViewFunc?.invoke(url, title)
    }

    override fun getWebViewUrl(): String? {
        return if (webViewUrlFunc != null) {
            webViewUrlFunc.invoke()
        } else {
            Timber.tag(TAG_WEB_LOG).w("????????? webViewUrlFunc ????????? getWebViewUrl ??????????????? null")
            null
        }
    }

    /**
     * ?????????????????????????????? permissionsResult
     */
    fun dispatchRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        requestPermissionsCallbacks.get(requestCode)?.invoke(grantResults)
        systemPhotoSelectors.get(requestCode)
            ?.dispatchRequestPermissionsResult(requestCode, permissions, grantResults)
        systemCameraTakePhotos.get(requestCode)
            ?.dispatchRequestPermissionsResult(requestCode, permissions, grantResults)
        systemFileChoosers.get(requestCode)
            ?.dispatchRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * ??????????????????????????? dispatchActivityResult
     */
    fun dispatchActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        systemPhotoSelectors.get(requestCode)?.dispatchActivityResult(requestCode, resultCode, data)
        systemCameraTakePhotos.get(requestCode)
            ?.dispatchActivityResult(requestCode, resultCode, data)
        systemFileChoosers.get(requestCode)?.dispatchActivityResult(requestCode, resultCode, data)
        systemPhotoSelectors.remove(requestCode)
        systemCameraTakePhotos.remove(requestCode)
        systemFileChoosers.remove(requestCode)
        activityResultCallbacks.get(requestCode)?.invoke(resultCode, data)
        activityResultCallbacks.remove(requestCode)
    }

    /**
     * ??????????????????????????????????????????
     */
    override fun showPermissionDeniedDialog(message: String) {
        showSettingAlertDialog(activity, message)
    }

    /**
     * ??????????????? requestCode
     */
    private fun generateRequestCode(): Int {
        var requestCode: Int
        do {
            requestCode = codeGenerator.nextInt(0xFFFF)
        } while (
            activityResultCallbacks.containsKey(requestCode) ||
            systemPhotoSelectors.containsKey(requestCode) ||
            systemCameraTakePhotos.containsKey(requestCode) ||
            systemFileChoosers.containsKey(requestCode)
        )
        return requestCode
    }

}