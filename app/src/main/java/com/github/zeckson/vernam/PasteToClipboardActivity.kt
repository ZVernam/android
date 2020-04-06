package com.github.zeckson.vernam

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.inputs_layout.*

class PasteToClipboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val toast = Toast.makeText(
            applicationContext,
            "Text Copied To Clipboard",
            Toast.LENGTH_SHORT
        )

        when (intent?.action) {
            Intent.ACTION_SEND -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (text != null) {
                    toast.show()
                    val clipboard: ClipboardManager =
                        this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Text copied!", text))
                    val intent = Intent()
                    intent.putExtra("text", text)
                    setResult(Activity.RESULT_OK, intent)
                }

            }
        }
        // BC! https://stackoverflow.com/questions/2590947/how-does-activity-finish-work-in-android
        finish()
        return
    }

}