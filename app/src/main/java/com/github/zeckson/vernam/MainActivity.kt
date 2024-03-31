package com.github.zeckson.vernam

import android.content.Intent
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.github.zeckson.vernam.databinding.ActivityLayoutBinding
import com.github.zeckson.vernam.databinding.InputsLayoutBinding
import com.github.zeckson.vernam.settings.SettingsActivity
import com.github.zeckson.vernam.settings.SettingsWrapper
import com.github.zeckson.vernam.util.getHost
import com.github.zeckson.vernam.util.setResultText
import com.github.zeckson.vernam.util.setTextToClipBoard
import com.github.zeckson.vernam.util.setupInitedDecryptCipher
import com.github.zeckson.vernam.util.showToast
import javax.crypto.Cipher


class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MyActivity"
    }

    private val settings by lazy(LazyThreadSafetyMode.NONE) {
        SettingsWrapper.get(this)
    }

    private val mainViewModel: MainViewModel by viewModels<MainViewModel>()

    private lateinit var mainBinding: ActivityLayoutBinding
    private lateinit var inputBinding: InputsLayoutBinding

    private val copyToClipboardListener: View.OnClickListener = View.OnClickListener {
        val text = inputBinding.cipherText.text.toString()
        if (text.isEmpty()) return@OnClickListener

        this.setTextToClipBoard(text)
        showToast("Text Copied To Clipboard")
        setResultText(text)
        // BC! https://stackoverflow.com/questions/2590947/how-does-activity-finish-work-in-android
        finish()
    }

    private val inputFieldsWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) = updateTextValues()

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            // do nothing
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            // do nothing
        }
    }

    private val settingsChangeListener =
        OnSharedPreferenceChangeListener { _, _ -> updateTextValues() }


    override fun onCreate(savedInstanceState: Bundle?) {
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) // debug light theme
        super.onCreate(savedInstanceState)
        Log.v(TAG, "Creating...")

        mainBinding = ActivityLayoutBinding.inflate(layoutInflater)
        inputBinding = mainBinding.main
        setContentView(mainBinding.root)

        setSupportActionBar(mainBinding.toolbar)

        restoreSavedState(savedInstanceState == null)

        inputBinding.copyToClipboard.setOnClickListener(copyToClipboardListener)
    }

    private fun restoreSavedState(newState: Boolean) {
        val withState = if (newState) "without" else "with"
        Log.v(TAG, "Restoring... ($withState savedState)")

        val myViewModel = mainViewModel

        if (newState && settings.passwordState == SettingsWrapper.PasswordState.SET) {
            validateBiometrics()
        }

        intent.getHost()?.let {
            Log.i(TAG, "Received url from intent: $it")
            myViewModel.plainTextValue = it
        }

        inputBinding.plainText.setText(myViewModel.plainTextValue)
        inputBinding.passwordText.setText(myViewModel.password)

        updateTextValues()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.v(TAG, "Saving state...")

        mainViewModel.password = inputBinding.passwordText.text.toString()
        mainViewModel.plainTextValue = inputBinding.plainText.text.toString()
    }

    private fun validateBiometrics() {
        val iv = settings.getPasswordIV()!!
        val cipher = setupInitedDecryptCipher(iv)!!
        createBiometricPrompt(::onBiometricSuccess, ::onBiometricFail).authenticate(
            createPromptInfo(),
            BiometricPrompt.CryptoObject(cipher)
        )
    }

    private fun onBiometricFail(code: Int?, message: CharSequence?) {
        if (code != null) {
            val error = message ?: "Error code $code"
            showToast(error.toString())
        }
        if (mainViewModel.plainTextValue.isNotEmpty()) {
            inputBinding.passwordText.requestFocus()
        }
        // Otherwise user cancelled, so no hash will be loaded
    }

    private fun onBiometricSuccess(cipher: Cipher) {
        val loadedPasswordHash =
            settings.getDefaultPasswordHash(cipher)
        if (loadedPasswordHash != null) {
            mainViewModel.passwordHash = loadedPasswordHash

            inputBinding.passwordTextLayout.setInternalHint(
                getString(R.string.default_password_hint),
                ContextCompat.getColor(this, R.color.primaryTextColor)
            )

            updateTextValues()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.v(TAG, "Starting...")

        updateTextValues()
        setupListeners()
    }


    override fun onResume() {
        super.onResume()
        Log.v(TAG, "Resuming...")

        inputBinding.plainText.requestFocus()
    }

    override fun onPause() {
        super.onPause()
        Log.v(TAG, "Paused...")
    }

    override fun onStop() {
        super.onStop()
        Log.v(TAG, "Stopped...")

        cleanListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(TAG, "Destroyed...")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.v(TAG, "On new intent...")

        setIntent(intent)
        restoreSavedState(true)
    }


    private fun createPromptInfo(): BiometricPrompt.PromptInfo =
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.prompt_info_title))
            .setSubtitle(getString(R.string.prompt_info_subtitle))
            .setDescription(getString(R.string.prompt_info_description))
            .setConfirmationRequired(false)
            .setNegativeButtonText(getString(R.string.do_not_use_password))
            //.setDeviceCredentialAllowed(true) // Allow PIN/pattern/password authentication.
            // Also note that setDeviceCredentialAllowed and setNegativeButtonText are
            // incompatible so that if you uncomment one you must comment out the other
            .build()


    private fun createBiometricPrompt(
        onSuccess: (cipher: Cipher) -> Unit,
        onFail: (code: Int?, msg: CharSequence?) -> Unit
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(this)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.d(TAG, "$errorCode :: $errString")
                onFail(errorCode, errString)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d(TAG, "Authentication failed for an unknown reason")
                onFail(null, null)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(TAG, "Authentication was successful")

                result.cryptoObject?.cipher?.let { onSuccess(it) }
            }
        }

        return BiometricPrompt(this, executor, callback)
    }


    private fun setupListeners() {
        inputBinding.plainText.addTextChangedListener(inputFieldsWatcher)
        inputBinding.passwordText.addTextChangedListener(inputFieldsWatcher)

        settings.addChangesListener(settingsChangeListener)
    }

    private fun cleanListeners() {
        inputBinding.plainText.removeTextChangedListener(inputFieldsWatcher)
        inputBinding.passwordText.removeTextChangedListener(inputFieldsWatcher)

        settings.removeChangesListener(settingsChangeListener)
    }

    private fun updateTextValues() {
        Log.i(TAG, "Update text values")

        val plainText = inputBinding.plainText.text.toString()
        val password = inputBinding.passwordText.text.toString()

        val generateCipherText = mainViewModel.generateCipherText(plainText, password)
        inputBinding.cipherText.setText(generateCipherText)
        inputBinding.copyToClipboard.isEnabled = generateCipherText.isNotEmpty()

        val hashButton = mainBinding.toolbar.menu.findItem(R.id.action_menu_hash)
        if (hashButton != null) {
            updateActionMenuHashButton(hashButton)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val result = super.onPrepareOptionsMenu(menu)
        updateActionMenuHashButton(menu.findItem(R.id.action_menu_hash))
        return result
    }

    private fun updateActionMenuHashButton(hashButton: MenuItem) {
        if (settings.isHashed) {
            hashButton.setIcon(R.drawable.ic_lock_locked_24)
            hashButton.title = getString(R.string.action_menu_hash_title)
        } else {
            hashButton.setIcon(R.drawable.ic_lock_open_24)
            hashButton.title = getString(R.string.action_menu_hash_title_plain)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }

            R.id.action_menu_hash -> {
                settings.isHashed = !settings.isHashed
                updateActionMenuHashButton(item)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

}
