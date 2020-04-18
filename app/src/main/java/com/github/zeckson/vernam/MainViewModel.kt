package com.github.zeckson.vernam

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class MainViewModel(application: Application) : AndroidViewModel(application) {

    enum class PasswordState {
        NOT_SET,
        RESET,
        SET
    }

    val settings = SettingsWrapper.get(application)
    val suffix:String = settings.suffix

    var plainTextValue:String = EMPTY_STRING
    var password:String = EMPTY_STRING
    var passwordHash:String = EMPTY_STRING

    var passwordState: PasswordState = PasswordState.NOT_SET

    companion object {
        private const val EMPTY_STRING = ""
    }

}