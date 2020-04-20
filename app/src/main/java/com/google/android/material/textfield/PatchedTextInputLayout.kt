package com.google.android.material.textfield

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet

class PatchedTextInputLayout(context: Context, attrs: AttributeSet?) :
    TextInputLayout(context, attrs) {
    private var internalHint: String? = null

    override fun drawableStateChanged() {
        super.drawableStateChanged()

        // No hint, no worries
        val myHint = internalHint?:return

        val isEnabled = isEnabled
        if (!isEnabled) return

        val myTextEdit = editText ?: return

        val hasText = !TextUtils.isEmpty(myTextEdit.text)
        if (hasText) return

        val hasFocus = myTextEdit.hasFocus()
        if (hasFocus) return

        //if textHints aren't equal and focus is lost
        if (myHint != hint) {
            collapseLabel()
        }
    }

    @SuppressLint("RestrictedApi")
    fun collapseLabel() {
        collapsingTextHelper.expansionFraction = 1f
    }

    fun setInternalHint(hint: String, color: Int) {
        internalHint = hint

        editText.setText("")
        editText.hint = hint
        editText.setHintTextColor(color)

        collapseLabel()
    }
}