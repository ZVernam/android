package com.github.zeckson.vernam

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference
import androidx.preference.PreferenceDialogFragmentCompat

class BiometricEditTextPreference(context: Context?, attrs: AttributeSet?) :
    DialogPreference(context, attrs) {

    class BiometricPasswordDialog : PreferenceDialogFragmentCompat() {
        override fun onDialogClosed(positiveResult: Boolean) {
            TODO("Not yet implemented")
        }
    }

    companion object {
        val DIALOG_TAG = "BiometricPasswordDialog"
    }
}