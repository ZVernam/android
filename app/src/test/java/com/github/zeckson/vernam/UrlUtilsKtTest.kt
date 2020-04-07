package com.github.zeckson.vernam

import org.junit.Assert.assertEquals
import org.junit.Test

class UrlUtilsKtTest {

    @Test
    fun stripScheme() {
        expectStrip("http://vk.com", "vk.com")
        expectStrip("https://vk.com", "vk.com")
        expectStrip("ftp://vk.com", "vk.com")
        expectStrip("//host", "host")
    }

    @Test
    fun stripPath() {
        expectStrip("vk.com/mypath", "vk.com")
        expectStrip("http://zeckson.inline/wtf?###easy", "zeckson.inline")
        expectStrip("fun/p/?#daskjldkjsal", "fun")
    }

    @Test
    fun cornerCases() {
        // Invlaid scheme
        expectStrip("fun/////daskjldkjsal", "daskjldkjsal")
    }

    @Test
    fun stripPort() {
        expectStrip("vk.com:8080", "vk.com")
        expectStrip("zeckson.inline:9999/wtf?###easy", "zeckson.inline")
        expectStrip("https://test.site.my:4043/path", "test.site.my")
    }

    @Test
    fun nothingToStrip() {
        expectNoStrip("vk.com")
        expectNoStrip("zeckson")
        expectNoStrip("fun")
    }

    @Test
    fun leaveInvalidAsIs() {
        expectNoStrip("//")
        expectNoStrip("/")
        expectNoStrip(":")
        expectNoStrip(":2020")
        expectNoStrip("/test")

    }

    private fun expectNoStrip(given: String) {
        assertEquals("Expected not stripped", given, stripUrlToHost(given))
    }

    private fun expectStrip(given: String, result: String) {
        assertEquals("Expected stripped", result, stripUrlToHost(given))
    }
}
