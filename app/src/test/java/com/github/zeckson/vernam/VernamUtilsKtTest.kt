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

    @Test
    fun checkRegressionWithDefaultHash() {
        assertEncode("C1n25Cu52\$", encrypt("abcdefghij", hash("abcdefghij")))
        assertEncode("86Kl1W1dql4vN29vm056", encrypt("lzFT95rQGbfba5GAedoX", hash("lzFT95rQGbfba5GAedoX")))
        assertEncode("h00rzbb15Zwr42WmN92t", encrypt("OYb2Xlnc1FG5TgWZWPWi", hash("EVDM62G85RrOlJppDTpF")))
        assertEncode("tt@lWfjW7h662K1Wnuzn", encrypt("mlHINlaW5uv7xCiszlCV", hash("vFniW7aivKX8sydozDmB")))
    }

    @Test
    fun checkRegressionWithSHA256() {
        assertEncode("tt@lWfjW7h662K1Wnuzn", encrypt("mlHINlaW5uv7xCiszlCV", hash("vFniW7aivKX8sydozDmB", "SHA-256")))
    }
    @Test
    fun checkRegressionWithSHA1() {
        assertEncode("tt@lWfjW7h662K1Wnuzn", encrypt("mlHINlaW5uv7xCiszlCV", hash("vFniW7aivKX8sydozDmB", "SHA-256")))
    }
    @Test
    fun checkRegressionWithMD5() {
        assertEncode("N2Z34xnWlWlfCmwrnttN", encrypt("mlHINlaW5uv7xCiszlCV", hash("vFniW7aivKX8sydozDmB", "MD5")))
    }



}