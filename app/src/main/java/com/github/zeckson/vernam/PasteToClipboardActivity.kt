package com.github.zeckson.vernam

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager

class PasteToClipboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (BiometricManager.from(application).canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // TODO: on can auth
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                showToast(getString(R.string.setup_lock_screen))
            }
            else -> {
                showToast(getString(R.string.biometric_is_not_supported))
            }
        }

        val text = "Text Copied To Clipboard"

        intent.getHost()?.let {
            showToast(text)
            setTextToClipBoard(it)
            setResultText(it)
        }
        // BC! https://stackoverflow.com/questions/2590947/how-does-activity-finish-work-in-android
        finish()
        return
    }


}