package com.github.zeckson.vernam

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import javax.crypto.Cipher
import com.github.zeckson.vernam.databinding.ActivityLayoutBinding
import com.github.zeckson.vernam.databinding.InputsLayoutBinding
import com.github.zeckson.vernam.settings.SettingsActivity
import com.github.zeckson.vernam.settings.SettingsWrapper
import com.github.zeckson.vernam.util.*


class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MyActivity"
    }

    private val settings by lazy(LazyThreadSafetyMode.NONE) {
        SettingsWrapper.get(this)
    }

    private val mainViewModel: MainViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    private lateinit var mainBinding: ActivityLayoutBinding
    private lateinit var inputBinding: InputsLayoutBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "Creating...")

        mainBinding = ActivityLayoutBinding.inflate(layoutInflater)
        inputBinding = InputsLayoutBinding.inflate(layoutInflater, mainBinding.root)
        setContentView(mainBinding.root)

        setSupportActionBar(mainBinding.toolbar)

        restoreSavedState(savedInstanceState == null)

        setupListeners()
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
            mainViewModel.plainTextValue = it
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

        inputBinding.plainText.requestFocus()
    }


    override fun onResume() {
        super.onResume()
        Log.v(TAG, "Resuming...")
    }

    override fun onPause() {
        super.onPause()
        Log.v(TAG, "Paused...")
    }

    override fun onStop() {
        super.onStop()
        Log.v(TAG, "Stopped...")
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

        inputBinding.plainText.onTextChanged(::updateTextValues)
        inputBinding.passwordText.onTextChanged(::updateTextValues)

        mainBinding.copyToClipboard.setOnClickListener {
            val text = inputBinding.cipherText.text.toString()
            if (text.isEmpty()) return@setOnClickListener

            this.setTextToClipBoard(text)
            showToast("Text Copied To Clipboard")
            setResultText(text)
            // BC! https://stackoverflow.com/questions/2590947/how-does-activity-finish-work-in-android
            finish()
        }

    }

    private fun updateTextValues() {

        val plainText = inputBinding.plainText.text.toString()
        val password = inputBinding.passwordText.text.toString()

        val generateCipherText = mainViewModel.generateCipherText(plainText, password)
        inputBinding.cipherText.setText(generateCipherText)
        mainBinding.copyToClipboard.isEnabled = generateCipherText.isNotEmpty()
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
