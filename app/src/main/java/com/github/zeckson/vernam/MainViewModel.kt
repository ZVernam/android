package com.github.zeckson.vernam

import android.app.Application
import androidx.biometric.BiometricManager
import androidx.lifecycle.AndroidViewModel

class MainViewModel(application: Application) : AndroidViewModel(application) {

    enum class PasswordState {
        NOT_SET,
        RESET,
        SET
    }

    val settings = SettingsWrapper.get(application)

    var plainTextValue: String = EMPTY_STRING
    var password: String = EMPTY_STRING
    var passwordHash: String = EMPTY_STRING

    val passwordState: PasswordState by lazy(LazyThreadSafetyMode.NONE, ::loadPasswordState)

    private fun loadPasswordState(): PasswordState {
        // No password in preferences (or corrupted)
        val passwordIV = settings.getPasswordIV() ?: return PasswordState.NOT_SET

        // Password set, but biometric was disabled or gone(((
        if (BiometricManager.from(getApplication())
                .canAuthenticate() != BiometricManager.BIOMETRIC_SUCCESS
        ) return PasswordState.RESET

        // Password is set, but invalidated (due biometrics param change)
        val cipher = setupInitedDecryptCipher(passwordIV)

        return if (cipher == null) {
            PasswordState.RESET
        } else {
            PasswordState.SET
        }
    }

    fun generateCipherText(plainText: String, passwordText: String): String {
        if (plainText.isEmpty()) return EMPTY_STRING

        val passwordHash =
            if (passwordHash.isNotEmpty() && passwordState == PasswordState.SET) {
                passwordHash
            } else if (passwordText.isEmpty()) EMPTY_STRING else hash(passwordText)


        val suffix = settings.suffix
        val textWithToken = plainText + suffix

        val isHashed = settings.isHashed

        val generated = encrypt(if (isHashed) hash(textWithToken) else textWithToken, passwordHash)
        val maxSize = settings.maxCipherSize

        return generated.take(maxSize)
    }

    companion object {
        private const val EMPTY_STRING = ""
    }

}