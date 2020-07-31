package com.github.zeckson.vernam

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.github.zeckson.VernamUtils
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settings = SettingsWrapper.get(application)

    var plainTextValue: String = EMPTY_STRING
    var password: String = EMPTY_STRING
    var passwordHash: String = EMPTY_STRING

    fun generateCipherText(plainText: String, passwordText: String): String {
        if (plainText.isEmpty()) return EMPTY_STRING

        val suffix = settings.suffix

        var textWithToken = plainText + suffix
        var plainPassword = passwordText

        if (!settings.isCaseSensitive) {
            textWithToken = textWithToken.toLowerCase(Locale.getDefault())
            plainPassword = plainPassword.toLowerCase(Locale.getDefault())
        }

        val passwordHash =
            when {
                plainPassword.isNotEmpty() -> VernamUtils.hash(plainPassword)
                passwordHash.isNotEmpty() -> passwordHash
                else -> EMPTY_STRING
            }

        if (passwordHash.isEmpty()) return EMPTY_STRING

        val isHashed = settings.isHashed

        val generated = VernamUtils.encrypt(if (isHashed) VernamUtils.hash(textWithToken) else textWithToken, passwordHash)
        val maxSize = textWithToken.length

        return generated.take(maxSize)
    }

    companion object {
        private const val EMPTY_STRING = ""
    }

}