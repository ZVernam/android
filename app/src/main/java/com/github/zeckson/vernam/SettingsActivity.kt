package com.github.zeckson.vernam

import android.content.Context
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, SettingsFragment())
            .commit()
    }

    class SettingsFragment() : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preference, rootKey)

            val context = context
            if (context != null) {
                setupPassword()
                setupBiometric(context)
            }

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

            passwordPreference?.summaryProvider = Preference.SummaryProvider<EditTextPreference> {
                if (it.text == null || it.text.isEmpty()) "Not set" else "Password is set"
            }
            passwordPreference?.setOnBindEditTextListener {
                it.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }
    }
}

