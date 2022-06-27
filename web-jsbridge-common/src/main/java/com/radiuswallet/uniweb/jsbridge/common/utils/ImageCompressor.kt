package com.radiuswallet.uniweb.jsbridge.common.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.exifinterface.media.ExifInterface
import androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION
import timber.log.Timber
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.IOException


private val separator = File.separator

internal fun cachePath(context: Context) =
    "${context.cacheDir.path}${separator}jsBridgeImage${separator}"

internal fun copyToCache(context: Context, imageFile: File): File {
    return imageFile.copyTo(imageFile.createCacheMirror(context), true)
}

/**
 * 生成一个原本不在缓存目录的文件的缓存文件
 */
internal fun File.createCacheMirror(context: Context): File {
    return File("${cachePath(context)}${this.name}")
}

internal fun File.createImageCacheMirror(
    context: Context,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
): File {
    val cacheMirror = createCacheMirror(context)
    return if (format == cacheMirror.compressFormat()) {
        cacheMirror
    } else {
        val absolutePath = cacheMirror.absolutePath
        File("${absolutePath.substringBeforeLast(".", absolutePath)}.${format.extension()}")
    }
}

@Throws(Exception::class)
fun File.compressImageFile(
    width: Int = 1000,
    height: Int = 1000,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
    quality: Int = 80,
    outFile: File? = null,//如果传 null,压缩之后输出的图片，会覆盖原来的图片。
): File {
    return decodeSampledBitmapFromFile(this, width, height).run {
        determineImageRotation(this@compressImageFile, this).run {
            if (outFile == null) {
                overWrite(this@compressImageFile, this, format, quality)
            } else {
                saveBitmap(this, outFile, format, quality)
                outFile
            }
        }
    }
}

fun loadBitmap(imageFile: File) = BitmapFactory.decodeFile(imageFile.absolutePath).run {
    determineImageRotation(imageFile, this)
}

@Throws(NullPointerException::class)
fun decodeSampledBitmapFromFile(imageFile: File, reqWidth: Int, reqHeight: Int): Bitmap {
    return BitmapFactory.Options().run {
        inJustDecodeBounds = true
        BitmapFactory.decodeFile(imageFile.absolutePath, this)

        inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

        inJustDecodeBounds = false
        //如果没有 imageFile 的读取权限将会为 NullPointerException
        BitmapFactory.decodeFile(imageFile.absolutePath, this)
    }
}

fun determineImageRotation(imageFile: File, bitmap: Bitmap): Bitmap {
    val exif = ExifInterface(imageFile.absolutePath)
    val orientation = exif.getAttributeInt(TAG_ORIENTATION, 0)
    val matrix = Matrix()
    when (orientation) {
        6 -> matrix.postRotate(90f)
        3 -> matrix.postRotate(180f)
        8 -> matrix.postRotate(270f)
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

fun compress2SmallBitmap(
    requestWith: Int,
    requestHeight: Int,
    filePath: String?,
    options: BitmapFactory.Options,
): Bitmap? {
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(filePath, options)
    options.inSampleSize = calculateInSampleSize(options, requestWith, requestHeight)
    options.inJustDecodeBounds = false
    return BitmapFactory.decodeFile(filePath, options)
}


fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    // Raw height and width of image
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {

        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}


fun overWrite(
    imageFile: File,
    bitmap: Bitmap,
    format: Bitmap.CompressFormat = imageFile.compressFormat() ?: Bitmap.CompressFormat.JPEG,
    quality: Int = 100,
): File {
    val result = if (format == imageFile.compressFormat()) {
        imageFile
    } else {
        val absolutePath = imageFile.absolutePath
        File("${absolutePath.substringBeforeLast(".", absolutePath)}.${format.extension()}")
    }
    saveBitmap(bitmap, result, format, quality)
    return result
}

fun checkFormatAndSaveBitmap(
    bitmap: Bitmap,
    destination: File,
    format: Bitmap.CompressFormat = destination.compressFormat() ?: Bitmap.CompressFormat.JPEG,
    quality: Int = 100,
): File {
    val result = if (format == destination.compressFormat()) {
        destination
    } else {
        val absolutePath = destination.absolutePath
        File("${absolutePath.substringBeforeLast(".", absolutePath)}.${format.extension()}")
    }
    saveBitmap(bitmap, result, format, quality)
    return result
}

fun saveBitmap(
    bitmap: Bitmap,
    destination: File,
    format: Bitmap.CompressFormat = destination.compressFormat() ?: Bitmap.CompressFormat.JPEG,
    quality: Int = 100,
): Boolean {
    destination.parentFile?.mkdirs()
    try {
        FileOutputStream(destination.absolutePath).use {
            bitmap.compress(format, quality, it)
        }
    } catch (e: Exception) {
        Timber.e(e)
        return false
    }
    return true
}

@Throws(IOException::class)
fun getBitmapFromContentUri(context: Context, uri: Uri): Bitmap? {
    val parcelFileDescriptor: ParcelFileDescriptor? =
        context.contentResolver.openFileDescriptor(uri, "r")
    return if (parcelFileDescriptor != null) {
        val fileDescriptor: FileDescriptor = parcelFileDescriptor.fileDescriptor
        val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        image
    } else {
        null
    }
}
