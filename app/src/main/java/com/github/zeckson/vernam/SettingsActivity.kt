package com.github.zeckson.vernam

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat

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

        lateinit var defaultPreference: SharedPreferences

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preference, rootKey)

            val context = context
            if (context != null) {
                defaultPreference = PreferenceManager.getDefaultSharedPreferences(context)

                setupBiometric(context)
            }

        }

        private fun setupBiometric(context: Context) {
            val biometricSwitchPreference =
                findPreference<SwitchPreferenceCompat>(getString(R.string.preference_is_biometric))
            biometricSwitchPreference?.let {
                when (BiometricManager.from(context).canAuthenticate()) {
                    BiometricManager.BIOMETRIC_SUCCESS -> {
                        it.isVisible = true
                    }
                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                        it.isEnabled = false
                        it.isVisible = true
                        it.shouldDisableView = true
                        it.title = "Click to setup Biometric"
                        it.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                            startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                            true
                        }
                    }
                    else -> {
                        // nothing
                    }
                }
            }

        }

    }

}

