package com.google.android.material.textfield

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View

class PatchedTextInputLayout(context: Context, attrs: AttributeSet?) :
    TextInputLayout(context, attrs), View.OnFocusChangeListener {
    private var internalHint: String? = null
    private var currentHint: CharSequence? = null

    fun tryCollapseOnLostFocus() {
        // No hint, no worries
        val myHint = internalHint ?: return

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

    fun setInternalHint(newHint: String, color: Int) {
        internalHint = newHint
        currentHint = hint

        hint = ""

        editText.setText("")
        editText.hint = newHint
        editText.setHintTextColor(color)

        //TODO: handle hint unset
        editText.onFocusChangeListener = this

        collapseLabel()
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (internalHint == null) return

        if (internalHint != currentHint) {
            hint = if (hasFocus) currentHint else ""
        }

    }
}