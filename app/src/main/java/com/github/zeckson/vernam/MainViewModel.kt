package com.github.zeckson.vernam

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settings = SettingsWrapper.get(application)

    var plainTextValue: String = EMPTY_STRING
    var password: String = EMPTY_STRING
    var passwordHash: String = EMPTY_STRING

    fun generateCipherText(plainText: String, passwordText: String): String {
        if (plainText.isEmpty()) return EMPTY_STRING

        val passwordHash =
            if (passwordHash.isNotEmpty() && settings.passwordState == SettingsWrapper.PasswordState.SET) {
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