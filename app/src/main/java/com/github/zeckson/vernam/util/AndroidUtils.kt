package com.github.zeckson.vernam.util

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.AttrRes

fun Context.themeColor(@AttrRes attrRes: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrRes, typedValue, true)
    return typedValue.data
}

fun Intent?.getHost(): String? {
    when (this?.action) {
        Intent.ACTION_SEND -> {
            return this.getStringExtra(Intent.EXTRA_TEXT)?.let { stripUrlToHost(it) }
        }
    }
    return null
}

fun Activity.setTextToClipBoard(text: String?) {
    val clipboard: ClipboardManager =
        this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("zvernam", text))
}

fun Activity.setResultText(it: String?) {
    val intent = Intent()
    intent.putExtra("text", it)
    setResult(Activity.RESULT_OK, intent)
}

fun Context.showToast(text: CharSequence) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}
