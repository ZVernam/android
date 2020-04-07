package com.github.zeckson.vernam

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PasteToClipboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val toast = Toast.makeText(
            applicationContext,
            "Text Copied To Clipboard",
            Toast.LENGTH_SHORT
        )

        intent.getHost().let {
            toast.show()
            setTextToClipBoard(it)
            setResultText(it)
        }
        // BC! https://stackoverflow.com/questions/2590947/how-does-activity-finish-work-in-android
        finish()
        return
    }

}