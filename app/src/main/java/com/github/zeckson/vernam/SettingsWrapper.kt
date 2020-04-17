package com.github.zeckson.vernam

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.preference.PreferenceManager
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

class SettingsWrapper private constructor(
    val preferences: SharedPreferences,
    private val context: Context
) {
    fun isBiometricEnabled(): Boolean {
        val password = getEncodedPassword()
        return !password.isNullOrEmpty() && BiometricManager.from(context)
            .canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun getMaxCipherSize(): Int {
        return preferences.getInt(getString(R.string.preference_max_size), MAX_CIPHER_SIZE_DEFAULT)
    }

    fun getDefaultPasswordHash(cipher: Cipher): String? {
        val password = getEncodedPassword()
        if (password.isNullOrEmpty()) return null
        val parts = password.split(":")
        if (parts.size != 2) {
            throw RuntimeException("Illegal input password format. Found parts: ${parts.size}")
        }
        val input = Base64.decode(parts[0], Base64.DEFAULT)
        val iv = Base64.decode(parts[1], Base64.DEFAULT)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(),IvParameterSpec(iv))
        val decoded = cipher.doFinal(input)
        return decoded.toString(Charsets.UTF_8)
    }

    private fun getEncodedPassword() =
        preferences.getString(getString(R.string.preference_password), null)

    private fun getString(resId: Int): String? {
        return context.getString(resId)
    }

    companion object {
        private const val MAX_CIPHER_SIZE_DEFAULT = 15
        fun get(ctx: Context): SettingsWrapper {
            return SettingsWrapper(PreferenceManager.getDefaultSharedPreferences(ctx), ctx)
        }
    }
}