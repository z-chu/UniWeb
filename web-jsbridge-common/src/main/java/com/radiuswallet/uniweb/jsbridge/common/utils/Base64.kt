package com.radiuswallet.uniweb.jsbridge.common.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import timber.log.Timber
import java.io.File
import java.io.FileInputStream


/**
 * 如果文件很大，会报 java.lang.OutOfMemoryError
 * @param flags 2-terminal communication, {@link Base64.NO_WRAP} is preferred
 */
fun File.encodeBase64(flags: Int = Base64.NO_WRAP): String? {
    return try {
        val inputFile = FileInputStream(this)
        val buffer = ByteArray(this.length().toInt())
        inputFile.read(buffer)
        inputFile.close()
        Base64.encodeToString(buffer, flags)
    } catch (throwable: Throwable) {
        Timber.e(throwable, "File.encodeBase64")
        null
    }
}

/**
 * 如果文件很大，会报 java.lang.OutOfMemoryError
 * @param flags 2-terminal communication, {@link Base64.NO_WRAP} is preferred
 */
fun File.encodeImageBase64(imageFormat: ImageFormat? = null, flags: Int = Base64.NO_WRAP): String? {
    val mineType =
        imageFormat?.mineType ?: this.imageFormat()?.mineType ?: ImageFormat.JPEG.mineType
    return encodeBase64(flags)?.let {
        "data:$mineType;base64,$it"
    }
}


fun File.encodeDataBase64(mineType: String, flags: Int = Base64.NO_WRAP): String? {
    return encodeBase64(flags)?.let {
        "data:$mineType;base64,$it"
    }
}


fun File.encodeMp4Base64(flags: Int = Base64.NO_WRAP): String? {
    return encodeBase64(flags)?.let {
        "data:video/mp4;base64,$it"
    }
}

fun String.asBase64toBitmap(flags: Int = Base64.NO_WRAP): Bitmap? {
    return try {
        val split = this.split(",")
        if (split.size != 2) {
            Timber.e("Is not the correct data base64 format")
        }
        val bitmapArray = Base64.decode(split[1], flags)
        val bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.size)
        if (bitmap == null) {
            Timber.e("Failed to convert Base64 to bitmap. Base64 may be in abnormal format and null after decode.")
        }
        bitmap
    } catch (throwable: Throwable) {
        Timber.e(throwable)
        null
    }
}