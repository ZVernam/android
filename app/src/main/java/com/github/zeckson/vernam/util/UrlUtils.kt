package com.github.zeckson.vernam.util

import java.util.*

private const val WWW_PREFIX = "www."

fun stripUrlToHost(url: String, stripWWW: Boolean = true): String {
    var result = url.lowercase(Locale.ENGLISH)
    // remove invalid scheme like "fun////"
    val scheme = result.lastIndexOf("//")
    if (scheme >= 0) {
        result = result.substring(scheme + 2, result.length)
    }
    val path = result.indexOf('/')
    if (path >= 0) {
        result = result.substring(0, path)
    }
    val port = result.indexOf(':')
    if (port >= 0) {
        result = result.substring(0, port)
    }
    if (stripWWW) {
        val www = result.indexOf(WWW_PREFIX)
        if (www == 0) {
            result = result.substring(WWW_PREFIX.length, result.length)
        }
    }
    return if (result.isBlank()) url else result
}
