package com.radiuswallet.uniweb.jsbridge.common.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.radiuswallet.uniweb.jsbridge.TAG_WEB_LOG
import timber.log.Timber
import java.io.File


/**
 * 该类封装了调起系统拍照功能, 需要权限 Manifest.permission.CAMERA
 */
internal class SystemCameraTakePhoto {

    private val preferences: SharedPreferences by lazy(LazyThreadSafetyMode.NONE) {
        requireContext().getSharedPreferences("SystemCameraTakePhoto", Context.MODE_PRIVATE)
    }

    private val requestCode: Int
    private val permissionRequestCode: Int
    private val fragmentActivity: FragmentActivity?
    private val fragment: Fragment?
    private val fileProviderAuthority: String

    private var outputFile: File? = null
        set(value) {
            field = value
            if (value != null) {
                preferences
                    .edit()
                    .putString("code_$requestCode", value.path)
                    .apply()
            } else {
                preferences
                    .edit()
                    .remove("code_$requestCode").apply()
            }
        }
        get() {
            return field ?: preferences.getString("code_$requestCode", null)?.let { File(it) }
        }


    var onSelectedListener: ((file: File?) -> Unit)? = null
    var onPermissionDeniedListener: ((permission: String) -> Unit)? = null
    private var onPermissionGranted: (() -> Unit)? = null


    constructor(
        activity: FragmentActivity,
        requestCode: Int,
        fileProviderAuthority: String,
        permissionRequestCode: Int? = null,
        onPermissionDenied: ((permission: String) -> Unit)? = null,
        onSelected: ((file: File?) -> Unit)? = null,
    ) {
        this.fileProviderAuthority = fileProviderAuthority
        this.requestCode = requestCode
        this.permissionRequestCode = permissionRequestCode ?: PERMISSIONS_REQUEST_CODE
        this.fragmentActivity = activity
        this.fragment = null
        this.onSelectedListener = onSelected
        this.onPermissionDeniedListener = onPermissionDenied

    }

    constructor(
        fragment: Fragment,
        requestCode: Int,
        fileProviderAuthority: String,
        permissionRequestCode: Int? = null,
        onPermissionDenied: ((permission: String) -> Unit)? = null,
        onSelected: ((file: File?) -> Unit)? = null,
    ) {
        this.fileProviderAuthority = fileProviderAuthority
        this.requestCode = requestCode
        this.permissionRequestCode = permissionRequestCode ?: PERMISSIONS_REQUEST_CODE
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
                    arrayOf(Manifest.permission.CAMERA),
                    permissionRequestCode
                )
            } else {
                fragment!!.requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    permissionRequestCode
                )
            }
        } else {
            onPermissionGranted = null
            onGranted.invoke()
        }
    }

    fun startImageCapture(outputFile: File) {
        checkPermission {
            this.outputFile = outputFile
            val intent =
                newIntentImageCapture(requireContext(), fileProviderAuthority, outputFile)
            if (intent == null) {
                onSelectedListener?.invoke(null)
                return@checkPermission
            }
            doStartActivity(intent)
        }
    }

    fun startVideoCapture(
        outputFile: File,
        highQuality: Boolean = true,//视频文件是否为高质量
        durationLimit: Int? = null,//设置视频最大允许录制的时长，单位为 秒
        sizeLimit: Int? = null,//指定视频最大允许的尺寸，单位为byte。
    ) {
        checkPermission {
            this.outputFile = outputFile
            val intent =
                newIntentVideoCapture(
                    requireContext(),
                    fileProviderAuthority,
                    outputFile,
                    highQuality,
                    durationLimit,
                    sizeLimit
                )
            if (intent == null) {
                onSelectedListener?.invoke(null)
                return@checkPermission
            }
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
            if (permissions.isNotEmpty() && permissions[0] == Manifest.permission.CAMERA &&
                grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                onPermissionGranted?.invoke()
            } else {
                onPermissionDeniedListener?.invoke(Manifest.permission.CAMERA)
            }
        }

    }

    fun dispatchActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == this.requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                outputFile?.let {
                    onSelectedListener?.invoke(it)
                }
            } else {
                onSelectedListener?.invoke(null)
            }
            outputFile = null
        }

    }


    companion object {

        private const val PERMISSIONS_REQUEST_CODE = 44

        fun newIntentVideoCapture(
            context: Context,
            fileProviderAuthority: String,
            outputFile: File,
            highQuality: Boolean = true,//视频文件是否为高质量
            durationLimit: Int? = null,//设置视频最大允许录制的时长，单位为秒
            sizeLimit: Int? = null,//指定视频最大允许的尺寸，单位为byte。
        ): Intent? {
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            intent.putExtra("videoPath", "file:" + outputFile.absolutePath)
            durationLimit?.let {
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, durationLimit)
            }
            sizeLimit?.let {
                intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, sizeLimit)
            }
            intent.putExtra(
                MediaStore.EXTRA_VIDEO_QUALITY,
                if (highQuality) 1 else 0 //0为低质量,1为高质量
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val uriForFile = try {
                    FileProvider
                        .getUriForFile(context, fileProviderAuthority, outputFile)
                } catch (e: IllegalArgumentException) {
                    //在华为手机上k可能发生 IllegalArgumentException: Failed to find configured root that contains
                    Timber.tag(TAG_WEB_LOG).e(e)
                    null
                } ?: return null

                intent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    uriForFile
                )
                intent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    uriForFile
                )
                //添加这一句表示对目标应用临时授权该Uri所代表的文件
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            } else {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outputFile))
            }
            return intent
        }
    }

    fun newIntentImageCapture(
        context: Context,
        fileProviderAuthority: String,
        outputFile: File,
    ): Intent? {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val uriForFile = try {
                FileProvider
                    .getUriForFile(context, fileProviderAuthority, outputFile)
            } catch (e: IllegalArgumentException) {
                //在华为手机上k可能发生 IllegalArgumentException: Failed to find configured root that contains
                Timber.tag(TAG_WEB_LOG).e(e)
                null
            } ?: return null

            intent.putExtra(
                MediaStore.EXTRA_OUTPUT,
                uriForFile
            )
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outputFile))
        }
        return intent
    }


}