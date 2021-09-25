package com.github.zeckson.vernam

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.util.AttributeSet
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.preference.EditTextPreference
import androidx.preference.Preference.SummaryProvider
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import com.github.zeckson.VernamUtils
import javax.crypto.Cipher

class BiometricEditTextPreference(
    context: Context?,
    attrs: AttributeSet?
) :
    EditTextPreference(context, attrs) {
    private val biometricStatus: Int
        get() = from(context).canAuthenticate(Authenticators.BIOMETRIC_STRONG)

    init {
        this.summaryProvider = SummaryProvider<EditTextPreference> {
            if (biometricStatus == BIOMETRIC_ERROR_NONE_ENROLLED
            ) {
                "Biometric weren't enrolled. Click here to setup"
            } else if (it.text == null || it.text.isEmpty()) {
                "Not set"
            } else {
                "Password is set"
            }
        }
    }

    companion object {
        private const val DIALOG_TAG = "BiometricPasswordDialog"
        private const val TAG = "BiometricPasswordPreference"
        private const val SAVE_STATE_TEXT = "BiometricPasswordDialog.text"
    }


    override fun onAttached() {
        when (biometricStatus) {
            BIOMETRIC_SUCCESS -> this.isVisible = true
            BIOMETRIC_ERROR_NONE_ENROLLED -> this.isVisible = true
            else -> this.isVisible = false
        }
    }


    fun showBiometricPrompt(caller: PreferenceFragmentCompat): Boolean {
        if (biometricStatus == BIOMETRIC_ERROR_NONE_ENROLLED) {
            caller.startActivityForResult(Intent(Settings.ACTION_SECURITY_SETTINGS), 0)
        }

        val settings = SettingsWrapper.get(context)
        val state = settings.passwordState
        if (state == SettingsWrapper.PasswordState.RESET) {
            // Refresh key if state was invalidated
            text = ""
            refreshKey()
        }

        val cipher = setupInitedEncryptCipher()
        cipher?.let {
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
        dialog.show(caller.parentFragmentManager, DIALOG_TAG)


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
                context.showToast(errString)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.e(TAG, "Authentication failed for an unknown reason")
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
                val hashed = VernamUtils.hash(text)
                Log.v(TAG, "Saving password: $text")
                Log.v(TAG, "Hashed: $hashed")
                val encrypted = cipher?.let {
                    val b64 =
                        Base64.encodeToString(it.doFinal(hashed.toByteArray()), Base64.DEFAULT)
                    Log.v(TAG, "Encrypted (Base64): $b64")
                    val ivBase64 = Base64.encodeToString(cipher.iv, Base64.DEFAULT)
                    Log.v(TAG, "Encrypted IV (Base64): $ivBase64")
                    "$b64:$ivBase64"
                }

                val value = encrypted ?: hashed
                saveValue(value)
            }
        }

        private fun saveValue(value: String) {
            val preference = preference as EditTextPreference
            if (preference.callChangeListener(value)) {
                preference.text = value
            }
        }

        override fun onPrepareDialogBuilder(builder: AlertDialog.Builder?) {
            super.onPrepareDialogBuilder(builder)
            builder?.setNeutralButton(R.string.clear_default_password) { _: DialogInterface, _: Int ->
                saveValue("")
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
}