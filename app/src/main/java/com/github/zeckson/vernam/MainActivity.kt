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

        passwordText.setText(
            getEncryptedPreferences().getString(
                getString(R.string.preference_password),
                ""
            )
        )

        setupTextListeners()

        intent.getHost()?.let {
            plainText.setText(it)
            this.setTextToClipBoard(it)
            showToast("Text Copied To Clipboard")
            setResultText(it)
            // BC! https://stackoverflow.com/questions/2590947/how-does-activity-finish-work-in-android
            return finish()
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
        val token = sharedPreferences.getString(getString(R.string.preference_token), "")
        val plainText = plainText.text.toString() + token
        val password = passwordText.text.toString()
        val isHashed = sharedPreferences.getBoolean(getString(R.string.preference_is_hashed), false)
        val generated = encrypt(if (isHashed) hash(plainText) else plainText, hash(password))
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
