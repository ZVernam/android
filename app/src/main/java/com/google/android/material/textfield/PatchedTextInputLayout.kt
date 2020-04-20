package com.google.android.material.textfield

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet

class PatchedTextInputLayout(context: Context, attrs: AttributeSet?) :
    TextInputLayout(context, attrs) {

    fun watchLabel() {
        this.editText.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val isHintSet = !this.editText.hint.isNullOrEmpty()
                if (isHintSet) collapseLabel()
            }
        }
    }

    @SuppressLint("RestrictedApi")
    fun collapseLabel() {
        collapsingTextHelper.expansionFraction = 1f
    }
}