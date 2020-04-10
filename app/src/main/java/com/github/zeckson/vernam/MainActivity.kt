package com.github.zeckson.vernam

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.inputs_layout.*


class MainActivity : AppCompatActivity() {
    private val TAG = "MyActivity"
    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        intent.getHost()?.let { plainText.setText(it) }
        passwordText.setText(
            sharedPreferences.getString(
                getString(R.string.preference_password),
                ""
            )
        )
        updateTextValues()

        setupTextListeners()

        copyToClipboard.setOnClickListener {
            val text = cipherText.text.toString()
            if (text.isEmpty()) return@setOnClickListener

            this.setTextToClipBoard(text)
            showToast("Text Copied To Clipboard")
            setResultText(text)
            // BC! https://stackoverflow.com/questions/2590947/how-does-activity-finish-work-in-android
            finish()
        }
    }

    private fun setupTextListeners() {
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) = updateTextValues()

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // do nothing
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // do nothing
            }
        }

        plainText.addTextChangedListener(textWatcher)
        passwordText.addTextChangedListener(textWatcher)
    }

    private fun updateTextValues() {

        val plainText = plainText.text.toString()
        if (plainText.isEmpty()) {
            cipherText.setText("")
            return
        }

        val token = sharedPreferences.getString(getString(R.string.preference_suffix), "")
        val textWithToken = plainText + token

        val password = passwordText.text.toString()
        if (password.isEmpty()) {
            cipherText.setText("")
            return
        }

        val isHashed = sharedPreferences.getBoolean(getString(R.string.preference_is_hashed), false)
        val generated =
            encrypt(if (isHashed) hash(textWithToken) else textWithToken, hash(password))
        val maxSize = sharedPreferences.getInt(getString(R.string.preference_max_size), 15)
        cipherText.setText(generated.take(maxSize))
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
