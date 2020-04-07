package com.github.zeckson.vernam

import org.junit.Assert.assertEquals
import org.junit.Test

class VernamUtilsKtTest {

    private fun assertEncode(expected: String, actual: String) {
        assertEquals("Encode", expected, actual)
    }

    @Test
    fun encodeText() {
        assertEncode("@@@@", encrypt("text", "text"))
    }

    @Test
    fun checkEmpty() {
        assertEncode("", encrypt("", ""))
        assertEncode("", encrypt("   ", ""))
        assertEncode("", encrypt("   ", "   "))
        assertEncode("", encrypt("", "   "))
        assertEncode("", encrypt("text", ""))
        assertEncode("", encrypt("", "secret"))
    }


}