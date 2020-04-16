package com.github.zeckson.vernam

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.AttributeSet
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.preference.EditTextPreference
import androidx.preference.Preference.SummaryProvider
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceViewHolder
import javax.crypto.Cipher

class BiometricEditTextPreference(context: Context?, attrs: AttributeSet?) :
    EditTextPreference(context, attrs) {

    init {
        this.summaryProvider = SummaryProvider<EditTextPreference> {
            if (it.text == null || it.text.isEmpty()) "Not set" else "Password is set"
        }
    }

    fun showBiometricPrompt(caller: PreferenceFragmentCompat): Boolean {
        caller.fragmentManager ?: return false
        val cipher = getDefaultCipher()
        if (cipher.init()) {
            createBiometricPrompt(caller).authenticate(
                createPromptInfo(),
                BiometricPrompt.CryptoObject(cipher)
            )
        }
        return true
    }

    fun showDialog(caller: PreferenceFragmentCompat, cipher: Cipher?) {
        val dialog = BiometricPasswordDialog(cipher)
        dialog.setTargetFragment(caller, 0)
        dialog.show(caller.fragmentManager!!, DIALOG_TAG)


        val b = Bundle(1)
        b.putString("key", this.key)
        dialog.arguments = b
    }


    private fun createBiometricPrompt(caller: PreferenceFragmentCompat): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(caller.context)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.d(TAG, "$errorCode :: $errString")
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d(TAG, "Authentication failed for an unknown reason")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(TAG, "Authentication was successful")
                showDialog(caller, result.cryptoObject?.cipher)
            }
        }

        return BiometricPrompt(caller, executor, callback)
    }


    private fun createPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.prompt_info_title))
            .setSubtitle(context.getString(R.string.prompt_info_subtitle))
            .setDescription(context.getString(R.string.prompt_info_description))
            .setConfirmationRequired(false)
            .setNegativeButtonText(context.getString(R.string.cancel_enter_password))
            //.setDeviceCredentialAllowed(true) // Allow PIN/pattern/password authentication.
            // Also note that setDeviceCredentialAllowed and setNegativeButtonText are
            // incompatible so that if you uncomment one you must comment out the other
            .build()
    }


    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        when (BiometricManager.from(context).canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> this.isVisible = true
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> this.isEnabled = false
            else -> this.isVisible = false
        }
    }

    class BiometricPasswordDialog(val cipher: Cipher?) : PreferenceDialogFragmentCompat() {

        private lateinit var myEditText: EditText
        private lateinit var mText: CharSequence

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val text = if (savedInstanceState == null) {
                getEditTextPreference().text
            } else {
                savedInstanceState.getCharSequence(SAVE_STATE_TEXT)
            }
            mText = text ?: ""
        }


        override fun onBindDialogView(view: View) {
            super.onBindDialogView(view)

            myEditText = view.findViewById<EditText>(R.id.passwordTextPreference)
            myEditText.requestFocus()
            if (mText.isNotEmpty()) {
                myEditText.setHint(R.string.password_not_changed)
                myEditText.setHintTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.primaryTextColor
                    )
                )
            } else {
                myEditText.setText(mText)
            }

            myEditText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        override fun onDialogClosed(positiveResult: Boolean) {
            if (positiveResult) {
                val text = myEditText.text.toString()
                val encrypted = cipher?.let {
                    Base64.encodeToString(it.doFinal(text.toByteArray()), Base64.DEFAULT)
                }

                val value = encrypted ?: text
                val preference = preference as EditTextPreference
                if (preference.callChangeListener(value)) {
                    preference.text = value
                }
            }
        }

        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            outState.putCharSequence(SAVE_STATE_TEXT, mText)
        }


        private fun getEditTextPreference(): EditTextPreference {
            return preference as EditTextPreference
        }


    }

    companion object {
        private const val DIALOG_TAG = "BiometricPasswordDialog"
        private const val TAG = "BiometricPasswordPreference"
        private const val SAVE_STATE_TEXT = "BiometricPasswordDialog.text"
    }
}