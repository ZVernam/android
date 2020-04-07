package com.github.zeckson.vernam

import java.util.*

fun stripUrlToHost(url: String): String {
    var result = url.toLowerCase(Locale.ENGLISH)
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
    return if (result.isBlank()) url else result
}
