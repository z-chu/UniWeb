package com.radiuswallet.uniweb.jsbridge.common.utils

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.radiuswallet.uniweb.jsbridge.TAG_WEB_LOG
import timber.log.Timber

/**
 * 该类封装了调起系统文件选择功能, 需要权限
 * Manifest.permission.READ_EXTERNAL_STORAGE ,
 * Manifest.permission.WRITE_EXTERNAL_STORAGE
 */
class SystemFileChooser {

    private val requestCode: Int
    private val permissionRequestCode: Int
    private val fragmentActivity: FragmentActivity?
    private val fragment: Fragment?

    var lastSelectedFilePath: String? = null
        private set

    var onSelectedListener: ((uris: Array<Uri>?) -> Unit)? = null
    var onPermissionDeniedListener: ((permission: String) -> Unit)? = null
    private var onPermissionGranted: (() -> Unit)? = null

    constructor(
        activity: FragmentActivity,
        requestCode: Int,
        permissionRequestCode: Int? = null,
        onPermissionDenied: ((permission: String) -> Unit)? = null,
        onSelected: ((uris: Array<Uri>?) -> Unit)? = null,
    ) {
        this.requestCode = requestCode
        this.permissionRequestCode =
            permissionRequestCode ?: PERMISSIONS_REQUEST_CODE
        this.fragmentActivity = activity
        this.fragment = null
        this.onSelectedListener = onSelected
        this.onPermissionDeniedListener = onPermissionDenied

    }

    constructor(
        fragment: Fragment,
        requestCode: Int,
        permissionRequestCode: Int? = null,
        onPermissionDenied: ((permission: String) -> Unit)? = null,
        onSelected: ((uris: Array<Uri>?) -> Unit)? = null,
    ) {
        this.requestCode = requestCode
        this.permissionRequestCode =
            permissionRequestCode ?: PERMISSIONS_REQUEST_CODE
        this.fragmentActivity = null
        this.fragment = fragment
        this.onSelectedListener = onSelected
        this.onPermissionDeniedListener = onPermissionDenied
    }

    private fun requireActivity(): FragmentActivity {
        return fragmentActivity ?: fragment?.requireActivity()
        ?: throw IllegalStateException("Must set up fragmentActivity or fragment.")
    }

    private fun requireContext(): Context {
        return fragmentActivity ?: fragment?.requireContext()
        ?: throw IllegalStateException("Must set up fragmentActivity or fragment.")
    }

    @JvmOverloads
    fun openFileChooser(acceptType: String = "*/*") {
        checkPermission {
            val intent = newIntentFileChooser(acceptType)
            doStartActivity(intent)
        }
    }

    fun openFileChooser(intent: Intent) {
        checkPermission {
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

    private fun checkPermission(onGranted: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onPermissionGranted = onGranted
            if (fragmentActivity != null) {
                ActivityCompat.requestPermissions(
                    fragmentActivity,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    permissionRequestCode
                )
            } else {
                fragment!!.requestPermissions(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    permissionRequestCode
                )
            }
        } else {
            onPermissionGranted = null
            onGranted.invoke()
        }
    }

    fun dispatchRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (requestCode == permissionRequestCode) {
            if (permissions.isNotEmpty()
                && permissions.contains(Manifest.permission.READ_EXTERNAL_STORAGE)
                && permissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && grantResults.isPermissionGrantedAll()
            ) {
                onPermissionGranted?.invoke()
            } else {
                onPermissionDeniedListener?.invoke(Manifest.permission.CAMERA)
            }
        }

    }

    fun dispatchActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == this.requestCode) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val processData = processData(data)
                if (!processData.isNullOrEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    val contentResolver: ContentResolver = requireContext().contentResolver
                    val takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION
                            or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    for (i in processData.indices) {
                        try {
                            contentResolver.takePersistableUriPermission(processData[i], takeFlags)
                        } catch (throwable: Throwable) {
                            Timber.tag(TAG_WEB_LOG).e(throwable)
                        }
                    }
                }
                onSelectedListener?.invoke(processData)
            } else {
                onSelectedListener?.invoke(null)
            }
        }

    }


    private fun processData(data: Intent): Array<Uri>? {
        val target = data.dataString
        if (!TextUtils.isEmpty(target)) {
            return arrayOf(Uri.parse(target))
        }
        val mClipData = data.clipData
        if (mClipData != null && mClipData.itemCount > 0) {
            val arrayOfNulls = arrayOfNulls<Uri>(mClipData.itemCount)
            for (i in 0 until mClipData.itemCount) {
                val mItem = mClipData.getItemAt(i)
                arrayOfNulls[i] = mItem.uri
            }
            return arrayOfNulls.requireNoNulls()
        }
        return null
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 45

        fun newIntentFileChooser(acceptType: String = "*/*"): Intent {
            val intent = Intent()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent.action = Intent.ACTION_OPEN_DOCUMENT
            } else {
                intent.action = Intent.ACTION_GET_CONTENT
            }
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = acceptType
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            return Intent.createChooser(intent, "");
        }

    }
}