package com.github.zeckson.vernam

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.preference.DialogPreference
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceViewHolder

class BiometricEditTextPreference(context: Context?, attrs: AttributeSet?) :
    DialogPreference(context, attrs) {

    fun showBiometricPrompt(caller: PreferenceFragmentCompat): Boolean {
        caller.fragmentManager ?: return false
        promptBiometric(createPromptInfo(), createBiometricPrompt(caller))
        return true
    }

    fun showDialog(caller: PreferenceFragmentCompat) {
        val dialog = BiometricPasswordDialog()
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
                showDialog(caller)
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

    class BiometricPasswordDialog : PreferenceDialogFragmentCompat() {
        override fun onDialogClosed(positiveResult: Boolean) {
            TODO("Not yet implemented")
        }
    }

    companion object {
        val DIALOG_TAG = "BiometricPasswordDialog"
        private const val TAG = "BiometricPasswordPreference"
    }
}