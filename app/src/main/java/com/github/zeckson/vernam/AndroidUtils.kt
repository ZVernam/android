package com.github.zeckson.vernam

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

fun Intent?.getHost(): String? {
    when (this?.action) {
        Intent.ACTION_SEND -> {
            return this.getStringExtra(Intent.EXTRA_TEXT)?.let { stripUrlToHost(it) }
        }
    }
    return null
}

fun EditText.onTextChanged(block: () -> Unit) {
    val textWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) = block()

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            // do nothing
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            // do nothing
        }
    }
    this.addTextChangedListener(textWatcher)
}

fun Activity.setTextToClipBoard(text: String?) {
    val clipboard: ClipboardManager =
        this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Text copied!", text))
}

fun Activity.setResultText(it: String?) {
    val intent = Intent()
    intent.putExtra("text", it)
    setResult(Activity.RESULT_OK, intent)
}

fun Activity.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
private const val PREFERENCE_KEY = "com.github.zeckson.user_secret_preferences"

fun Context.getEncryptedPreferences() = EncryptedSharedPreferences.create(
    PREFERENCE_KEY,
    masterKeyAlias,
    this,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)


