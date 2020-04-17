package com.github.zeckson.vernam

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.preference.PreferenceManager

class SettingsWrapper private constructor(
    val preferences: SharedPreferences,
    private val context: Context
) {
    fun isBiometricEnabled(): Boolean {
        val password = preferences.getString(getString(R.string.preference_password), null)
        return password != null && BiometricManager.from(context)
            .canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun getMaxCipherSize(): Int {
        return preferences.getInt(getString(R.string.preference_max_size), MAX_CIPHER_SIZE_DEFAULT)
    }

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