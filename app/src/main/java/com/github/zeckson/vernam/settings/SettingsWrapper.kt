package com.github.zeckson.vernam.settings

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.preference.PreferenceManager
import com.github.zeckson.vernam.R
import com.github.zeckson.vernam.util.biometricStatus
import com.github.zeckson.vernam.util.setupInitedDecryptCipher
import javax.crypto.Cipher

class SettingsWrapper private constructor(
    private val preferences: SharedPreferences,
    private val context: Context
) {
    enum class PasswordState {
        NOT_SET,
        RESET,
        SET
    }

    val isCaseSensitive: Boolean
        get() = preferences.getBoolean(getString(R.string.preference_is_casesensitive), false)

    val passwordState: PasswordState by lazy(LazyThreadSafetyMode.NONE, ::loadPasswordState)

    private fun loadPasswordState(): PasswordState {
        // No password in preferences (or corrupted)
        val passwordIV = getPasswordIV() ?: return PasswordState.NOT_SET

        // Password set, but biometric was disabled or gone(((
        if (context.biometricStatus != BiometricManager.BIOMETRIC_SUCCESS
        ) return PasswordState.RESET

        // Password is set, but invalidated (due biometrics param change)
        val cipher = setupInitedDecryptCipher(passwordIV)

        return if (cipher == null) {
            PasswordState.RESET
        } else {
            PasswordState.SET
        }
    }

    private fun getEncodedPassword() =
        preferences.getString(getString(R.string.preference_password_title), null)

    private fun getString(resId: Int): String {
        return context.getString(resId)
    }

    val isHashed: Boolean
        get() = preferences.getBoolean(getString(R.string.preference_is_hashed_title), true)


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
        get() = preferences.getString(getString(R.string.preference_suffix_title), EMPTY_STRING)!!

    companion object {
        private const val EMPTY_STRING = ""
        fun get(ctx: Context): SettingsWrapper {
            return SettingsWrapper(PreferenceManager.getDefaultSharedPreferences(ctx), ctx)
        }
    }
}