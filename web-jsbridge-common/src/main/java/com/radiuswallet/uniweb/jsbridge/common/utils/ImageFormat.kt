package com.radiuswallet.uniweb.jsbridge.common.utils

import android.graphics.Bitmap
import java.io.File
import java.util.*

enum class ImageFormat(val fileExtensions: String, val mineType: String) {
    @Deprecated("Use JPEG")
    JPG("jpg", "image/jpeg"),
    JPEG("jpeg", "image/jpeg"),
    PNG("png", "image/png"),
    WEBP("webp", "image/webp");
}

fun ImageFormat.toCompressFormat(): Bitmap.CompressFormat {
    return when (this) {
        ImageFormat.PNG -> {
            Bitmap.CompressFormat.PNG
        }
        ImageFormat.WEBP -> {
            Bitmap.CompressFormat.WEBP
        }
        ImageFormat.JPEG, ImageFormat.JPG -> {
            Bitmap.CompressFormat.JPEG

        }
    }
}

fun Bitmap.CompressFormat.toImageFormat(): ImageFormat {
    return when (this) {
        Bitmap.CompressFormat.PNG -> {
            ImageFormat.PNG
        }
        Bitmap.CompressFormat.JPEG -> {
            ImageFormat.JPEG
        }
        else -> {
            ImageFormat.WEBP
        }
    }
}

fun File.compressFormat() = when (extension.lowercase(Locale.US)) {
    "png" -> Bitmap.CompressFormat.PNG
    "webp" -> Bitmap.CompressFormat.WEBP
    "jpg", "jpeg" -> Bitmap.CompressFormat.JPEG
    else -> null
}


fun File.imageFormat() = compressFormat()?.toImageFormat()

fun Bitmap.CompressFormat.extension() = when (this) {
    Bitmap.CompressFormat.PNG -> "png"
    Bitmap.CompressFormat.JPEG -> "jpg"
    else -> "webp"
}