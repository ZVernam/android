package com.github.zeckson.vernam.settings

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.github.zeckson.vernam.BuildConfig
import com.github.zeckson.vernam.R
import com.github.zeckson.vernam.util.biometricStatus

class SettingsActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, SettingsFragment())
            .commit()
    }

    override fun onPreferenceDisplayDialog(
        caller: PreferenceFragmentCompat,
        pref: Preference?
    ): Boolean {
        return when (pref) {
            is BiometricEditTextPreference -> pref.showBiometricPrompt(caller)
            else -> false
        }
    }


    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preference, rootKey)

            setupBiometric()
            setupVersion()
        }

        private fun setupVersion() {
            val appVersion =
                findPreference<Preference>(getString(R.string.preference_version_id))

            appVersion?.summary = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        }

        private fun setupBiometric() {
            val biometricSwitchPreference =
                findPreference<SwitchPreferenceCompat>(getString(R.string.preference_save_password_id))
            biometricSwitchPreference?.let {
                when (context?.biometricStatus) {
                    BiometricManager.BIOMETRIC_SUCCESS -> {
                        it.isVisible = true
                    }
                    else -> {
                        // nothing
                    }
                }
            }

        }

    }

}

