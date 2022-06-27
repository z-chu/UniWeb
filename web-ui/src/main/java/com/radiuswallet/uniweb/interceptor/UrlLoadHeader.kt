package com.radiuswallet.uniweb.interceptor

interface UrlLoadHeader {

    fun getHeaders(url: String, headers: Map<String, String>): Map<String, String>

}