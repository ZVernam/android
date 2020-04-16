package com.github.zeckson.vernam

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.preference.*
import java.security.KeyStoreException
import javax.crypto.NoSuchPaddingException

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

            passwordPreference?.setOnPreferenceChangeListener { _, newValue ->

                val defaultCipher = getDefaultCipher()
                if (defaultCipher.isValid()) {

                    val value = newValue as String
                    try {
                        val result = defaultCipher.doFinal(value.toByteArray())
                        val b64 = Base64.encodeToString(result, Base64.DEFAULT)
                        val existing =
                            defaultPreference.getString(
                                getString(R.string.preference_password),
                                ""
                            )
                        if (existing != b64) {
                            defaultPreference.edit()
                                .putString(getString(R.string.preference_password), b64)
                                .apply()
                        }
                    } catch (e: RuntimeException) {
                        when (e) {
                            is KeyStoreException,
                            is NoSuchPaddingException ->
                                throw RuntimeException("Failed to encrypt value", e)
                            else -> throw e
                        }
                    }

                }
                false
            }

            passwordPreference?.summaryProvider = Preference.SummaryProvider<EditTextPreference> {
                if (it.text == null || it.text.isEmpty()) "Not set" else "Password is set"
            }
            passwordPreference?.setOnBindEditTextListener {
                it.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }
    }

}

