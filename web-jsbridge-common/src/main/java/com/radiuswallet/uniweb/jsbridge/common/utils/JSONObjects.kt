package com.radiuswallet.uniweb.jsbridge.common.utils

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

fun JSONObject.getOrNull(name: String): Any? {
    return try {
        this.get(name)
    } catch (e: JSONException) {
        null
    }
}

fun JSONObject.getStringOrNull(name: String): String? {
    return try {
        this.getString(name)
    } catch (e: JSONException) {
        null
    }
}

fun JSONObject.getBooleanOrNull(name: String): Boolean? {
    return try {
        this.getBoolean(name)
    } catch (e: JSONException) {
        null
    }
}

fun JSONObject.getDoubleOrNull(name: String): Double? {
    return try {
        this.getDouble(name)
    } catch (e: JSONException) {
        null
    }
}

fun JSONObject.getIntOrNull(name: String): Int? {
    return try {
        this.getInt(name)
    } catch (e: JSONException) {
        null
    }
}

fun JSONObject.getLongOrNull(name: String): Long? {
    return try {
        this.getLong(name)
    } catch (e: JSONException) {
        null
    }
}

fun JSONObject.getJSONObjectOrNull(name: String): JSONObject? {
    return try {
        this.getJSONObject(name)
    } catch (e: JSONException) {
        null
    }
}

fun JSONObject.getJSONArrayOrNull(name: String): JSONArray? {
    return try {
        this.getJSONArray(name)
    } catch (e: JSONException) {
        null
    }
}