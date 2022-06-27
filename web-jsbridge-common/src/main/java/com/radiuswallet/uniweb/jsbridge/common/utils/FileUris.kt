package com.radiuswallet.uniweb.jsbridge.common.utils

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore


fun Uri.toFilePath(context: Context): String? {
    val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
    // DocumentProvider
    if (isKitKat && DocumentsContract.isDocumentUri(context, this)) {
        // ExternalStorageProvider
        if (isExternalStorageDocument(this)) {
            val docId = DocumentsContract.getDocumentId(this)
            val split = docId.split(":".toRegex()).toTypedArray()
            val type = split[0]
            if ("primary".equals(type, ignoreCase = true)) {
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            }
            // TODO handle non-primary volumes
        } else if (isDownloadsDocument(this)) {
            val id = DocumentsContract.getDocumentId(this)
            val contentUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
            return getDataColumn(context, contentUri, null, null)
        } else if (isMediaDocument(this)) {
            val docId = DocumentsContract.getDocumentId(this)
            val split = docId.split(":".toRegex()).toTypedArray()
            val type = split[0]
            var contentUri: Uri? = null
            when (type) {
                "image" -> {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                "video" -> {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
                "audio" -> {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
            }
            val selection = "_id=?"
            val selectionArgs = arrayOf(
                split[1]
            )
            return if (contentUri != null) {
                getDataColumn(context, contentUri, selection, selectionArgs)
            } else {
                null
            }
        }
    } else if ("content".equals(this.scheme, ignoreCase = true)) {

        // Return the remote address
        return if (isGooglePhotosUri(this)) this.lastPathSegment else getDataColumn(context,
            this,
            null,
            null)
    } else if ("file".equals(this.scheme, ignoreCase = true)) {
        return this.path
    }
    return null
}

fun getDataColumn(
    context: Context, uri: Uri, selection: String?,
    selectionArgs: Array<String>?,
): String? {
    val column = "_data"
    val projection = arrayOf(
        column
    )
    context.contentResolver.query(uri, projection, selection, selectionArgs,
        null)?.use {
        if (it.moveToFirst()) {
            val index = it.getColumnIndexOrThrow(column)
            return it.getString(index)
        }
    }
    return null
}

fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
}

fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
}

fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
}

fun isGooglePhotosUri(uri: Uri): Boolean {
    return "com.google.android.apps.photos.content" == uri.authority
}