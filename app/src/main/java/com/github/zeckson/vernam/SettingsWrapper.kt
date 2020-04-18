package com.github.zeckson.vernam

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.preference.PreferenceManager
import javax.crypto.Cipher

class SettingsWrapper private constructor(
    val preferences: SharedPreferences,
    private val context: Context
) {

    private fun getEncodedPassword() =
        preferences.getString(getString(R.string.preference_password), null)

    private fun getString(resId: Int): String? {
        return context.getString(resId)
    }

    val isBiometricEnabled: Boolean
        get() {
            val password = getEncodedPassword()
            return !password.isNullOrEmpty() && BiometricManager.from(context)
                .canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
        }

    val maxCipherSize: Int
        get() {
            return preferences.getInt(
                getString(R.string.preference_max_size),
                MAX_CIPHER_SIZE_DEFAULT
            )
        }

    private fun getEncodedPasswordAndIv(): Pair<ByteArray, ByteArray>? {
        val password = getEncodedPassword()
        if (password.isNullOrEmpty()) return null
        val parts = password.split(":")
        if (parts.size != 2) {
            throw RuntimeException("Illegal input password format. Found parts: ${parts.size}")
        }
        val input = Base64.decode(parts[0], Base64.DEFAULT)
        val iv = Base64.decode(parts[1], Base64.DEFAULT)
        return Pair(input, iv)
    }

    fun getPasswordIV(): ByteArray? {
        val (_, iv) = getEncodedPasswordAndIv() ?: return null
        return iv
    }

    fun getDefaultPasswordHash(cipher: Cipher): String? {
        val (input, _) = getEncodedPasswordAndIv() ?: return null
        val decoded = cipher.doFinal(input)
        return decoded.toString(Charsets.UTF_8)
    }

    val suffix: String
        get() = preferences.getString(getString(R.string.preference_suffix), EMPTY_STRING)!!

    companion object {
        private const val MAX_CIPHER_SIZE_DEFAULT = 15
        private const val EMPTY_STRING = ""
        fun get(ctx: Context): SettingsWrapper {
            return SettingsWrapper(PreferenceManager.getDefaultSharedPreferences(ctx), ctx)
        }
    }
}