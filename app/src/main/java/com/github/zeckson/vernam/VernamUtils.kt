package com.github.zeckson.vernam

private val ALPHABET = "@bCd3f9h1jKlm2nN0pq4r\$tuv5wW6x7y8Zz".toCharArray()

fun encrypt(text: String, secret: String): String {
    // Failfast
    if (text.isBlank() || secret.isBlank()) {
        return "";
    }

    val cipher = charArrayOf();
    for (i in 0..text.length) {
        val result = text.codePointAt(i) xor secret.codePointAt(i % secret.length)
        cipher[i] = toChar(result);
    }
    return cipher.joinToString();
}

private fun toChar(code: Int): Char = ALPHABET[code % ALPHABET.size]
