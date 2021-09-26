package com.github.zeckson.vernam

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
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
                when (context.biometricStatus) {
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

