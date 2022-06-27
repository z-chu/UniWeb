package com.radiuswallet.uniweb.jsbridge.common.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import com.radiuswallet.uniweb.jsbridge.common.R

internal fun showSettingAlertDialog(context: Context, message: String) {
    val builder = AlertDialog.Builder(context)
    builder.setMessage(message)
    builder.setPositiveButton(context.getString(R.string.action_setup)) { _, _ ->
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.data = Uri.parse("package:${context.packageName}")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        context.startActivity(intent)
    }
    builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
        dialog.dismiss()
    }
    builder.setCancelable(false)
    builder.show()
}
