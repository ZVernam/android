package com.github.zeckson.vernam

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.preference.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, SettingsFragment())
            .commit()
    }

    class SettingsFragment() : PreferenceFragmentCompat() {
        private lateinit var encryptedPreference: SharedPreferences

        private val encryptedDataStore = object : PreferenceDataStore() {

            override fun getString(key: String?, defValue: String?): String? {
                return encryptedPreference.getString(key, defValue)
            }

            override fun putString(key: String?, value: String?) {
                encryptedPreference.edit().putString(key, value).apply()
            }

        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preference, rootKey)

            val context = context
            if (context != null) {
                encryptedPreference = context.getEncryptedPreferences()

                setupSuffix()
                setupPassword()
                setupBiometric(context)
            }

        }

        private fun setupSuffix() {
            val suffixPreference =
                findPreference<EditTextPreference>(getString(R.string.preference_suffix))
            suffixPreference?.preferenceDataStore = encryptedDataStore
        }

        private fun setupBiometric(context: Context) {
            val biometricSwitchPreference =
                findPreference<SwitchPreferenceCompat>(getString(R.string.preference_is_biometric))
            when (BiometricManager.from(context).canAuthenticate()) {
                BiometricManager.BIOMETRIC_SUCCESS, BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    biometricSwitchPreference?.isVisible = true
                }
                else -> {
                    // nothing
                }
            }
        }

        private fun setupPassword() {
            val passwordPreference =
                findPreference<EditTextPreference>(getString(R.string.preference_password))

            passwordPreference?.preferenceDataStore = encryptedDataStore
            passwordPreference?.summaryProvider = Preference.SummaryProvider<EditTextPreference> {
                if (it.text.isEmpty()) "Not set" else "Password is set"
            }
            passwordPreference?.setOnBindEditTextListener {
                it.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }
    }
}

