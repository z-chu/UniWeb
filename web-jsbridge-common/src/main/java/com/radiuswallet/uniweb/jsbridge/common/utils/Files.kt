package com.radiuswallet.uniweb.jsbridge.common.utils

import java.io.File
import java.util.*


/**
 * 根据 File 大小，获取易理解的大小描述，如：2G、2M、2K、2B
 */
val File.sizeString: String
    get() {
        val len = length()
        return when {
            len > 1073741824 -> {
                (len.toDouble() / 1073741824).toString() + "G"
            }
            len > 1048576 -> {
                (len.toDouble() / 1048576).toString() + "M"
            }
            len > 1024 -> {
                (len.toDouble() / 1024).toString() + "K"
            }
            else -> {
                len.toString() + "B"
            }
        }
    }

/**
 * 检查文件是否超过指定的现在大小
 * @param size 大小
 * @param unit 单位 G、M、K、B
 */
fun File.checkLimitSize(size: Double, unit: String = "K"): Boolean {
    val len = length()
    val fileSize = when {
        unit.uppercase(Locale.ENGLISH) == "B" -> {
            len.toDouble()
        }
        unit.uppercase(Locale.ENGLISH) == "K" -> {
            len.toDouble() / 1024
        }
        unit.uppercase(Locale.ENGLISH) == "M" -> {
            len.toDouble() / 1048576
        }
        unit.uppercase(Locale.ENGLISH) == "G" -> {
            len.toDouble() / 1073741824
        }
        else -> {
            len.toDouble()
        }
    }
    return fileSize < size
}

