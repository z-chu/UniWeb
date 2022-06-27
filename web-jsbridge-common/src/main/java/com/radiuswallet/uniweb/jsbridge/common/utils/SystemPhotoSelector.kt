package com.radiuswallet.uniweb.jsbridge.common.utils

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.radiuswallet.uniweb.jsbridge.TAG_WEB_LOG
import timber.log.Timber
import java.io.File


/**
 * 该类封装了调起系统相册选图功能， 需要权限 Manifest.permission.READ_EXTERNAL_STORAGE
 */
internal class SystemPhotoSelector {

    private val requestCode: Int
    private val permissionRequestCode: Int
    private val fragmentActivity: FragmentActivity?
    private val fragment: Fragment?

    var lastSelectedFilePath: String? = null
        private set

    var onSelectedListener: ((filePath: String?) -> Unit)? = null
    var onPermissionDeniedListener: ((permission: String) -> Unit)? = null
    var onErrorListener: ((Throwable) -> Unit)? = null

    constructor(
        activity: FragmentActivity,
        requestCode: Int,
        permissionRequestCode: Int? = null,
        onPermissionDenied: ((permission: String) -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null,
        onSelected: ((filePath: String?) -> Unit)? = null,
    ) {
        this.requestCode = requestCode
        this.permissionRequestCode = permissionRequestCode ?: PERMISSIONS_REQUEST_CODE
        this.fragmentActivity = activity
        this.fragment = null
        this.onSelectedListener = onSelected
        this.onPermissionDeniedListener = onPermissionDenied
        this.onErrorListener = onError

    }

    constructor(
        fragment: Fragment,
        requestCode: Int,
        permissionRequestCode: Int? = null,
        onPermissionDenied: ((permission: String) -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null,
        onSelected: ((filePath: String?) -> Unit)? = null,
    ) {
        this.requestCode = requestCode
        this.permissionRequestCode = permissionRequestCode ?: PERMISSIONS_REQUEST_CODE
        this.fragmentActivity = null
        this.fragment = fragment
        this.onSelectedListener = onSelected
        this.onPermissionDeniedListener = onPermissionDenied
        this.onErrorListener = onError
    }

    private fun requireActivity(): FragmentActivity {
        return fragmentActivity ?: fragment?.requireActivity()
        ?: throw IllegalStateException("Must set up fragmentActivity or fragment.")
    }

    private fun requireContext(): Context {
        return fragmentActivity ?: fragment?.requireContext()
        ?: throw IllegalStateException("Must set up fragmentActivity or fragment.")
    }


    fun selectFromGallery() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (fragmentActivity != null) {
                ActivityCompat.requestPermissions(
                    fragmentActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    permissionRequestCode
                )
            } else {
                fragment!!.requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    permissionRequestCode
                )
            }
        } else {
            val intent = newIntentOpenGallery()
            doStartActivity(intent)
        }

    }

    private fun doStartActivity(intent: Intent) {
        try {
            if (fragmentActivity != null) {
                fragmentActivity.startActivityForResult(intent, requestCode)
            } else {
                fragment?.startActivityForResult(intent, requestCode)
            }
        } catch (throwable: Throwable) {
            Timber.tag(TAG_WEB_LOG).e(throwable)
        }
    }

    fun dispatchRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (requestCode == permissionRequestCode) {
            if (permissions.isNotEmpty() && permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE &&
                grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = newIntentOpenGallery()
                doStartActivity(intent)
            } else {
                onPermissionDeniedListener?.invoke(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

    }

    fun dispatchActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == this.requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                data?.data?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        handleImageOnKitKat(it)
                    } else {
                        handleImageBeforeKitKat(it)
                    }
                }
            } else {
                onSelectedListener?.invoke(null)
            }
        }

    }

    @TargetApi(19)
    private fun handleImageOnKitKat(uri: Uri) {
        val callback: (String?) -> Unit = { imagePath ->
            if (imagePath != null) {
                this.lastSelectedFilePath = imagePath
                onSelectedListener?.invoke(imagePath)
            } else {
                onErrorListener?.invoke(NullPointerException("Image file path not found"))
            }
        }
        if (DocumentsContract.isDocumentUri(requireContext(), uri)) {
            // 如果是document类型的Uri，则通过document id处理
            val docId = DocumentsContract.getDocumentId(uri)
            if ("com.android.providers.media.documents" == uri.authority) {
                val id = docId.split(":".toRegex()).toTypedArray()[1]
                // 解析出数字格式的id
                val selection = MediaStore.Images.Media._ID + "=" + id
                getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null,
                    selection,
                    callback)
            } else if ("com.android.providers.downloads.documents" == uri.authority) {
                val contentUri: Uri = ContentUris.withAppendedId(
                    Uri.parse("content: //downloads/public_downloads"),
                    java.lang.Long.valueOf(docId)
                )
                getImagePath(contentUri, null, null, callback)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            // 如果是content类型的Uri，则使用普通方式处理
            getImagePath(uri, arrayOf(MediaStore.Images.Media.DATA), null, callback)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            // 如果是file类型的Uri，直接获取图片路径即可
            callback.invoke(uri.path)
        }

    }

    private fun handleImageBeforeKitKat(uri: Uri) {
        getImagePath(uri, null, null) { imagePath ->
            if (imagePath != null) {
                this.lastSelectedFilePath = imagePath
                onSelectedListener?.invoke(imagePath)
            } else {
                onErrorListener?.invoke(NullPointerException("Image file path not found"))
            }
        }
    }


    private fun getImagePath(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        callback: (String?) -> Unit,
    ) {
        JsBridgeTaskExecutors.asyncThreadExecutor.execute {
            try {
                var imagePath: String? = null
                requireContext()
                    .contentResolver
                    .query(uri, projection, selection, null, null)
                    ?.use {
                        if (it.moveToFirst()) {
                            imagePath =
                                it.getString(it.getColumnIndex(MediaStore.Images.Media.DATA))
                            return@use
                        }
                    }
                if (imagePath == null) {
                    val saveContentUriToFile = saveContentUriToFile(uri)
                    if (saveContentUriToFile != null) {
                        imagePath = saveContentUriToFile.path
                    }
                }
                JsBridgeTaskExecutors.mainThreadExecutor.execute {
                    callback.invoke(imagePath)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }

        }
    }

    private fun saveContentUriToFile(uri: Uri): File? {
        val requireContext = requireContext()
        val bitmapFromContentUri = getBitmapFromContentUri(requireContext, uri)
        if (bitmapFromContentUri != null) {
            val imageFile = JsBridgePhotoFileProvider.generateOutputMediaFile(requireContext)
            if (imageFile != null) {
                val saveBitmap = saveBitmap(bitmapFromContentUri, imageFile)
                if (saveBitmap) {
                    return imageFile
                }
            }
        }
        return null
    }

    companion object {

        private const val PERMISSIONS_REQUEST_CODE = 43

        fun newIntentOpenGallery(): Intent {
            val intent: Intent
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
            } else {
                intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            }
            return intent
        }

    }


}