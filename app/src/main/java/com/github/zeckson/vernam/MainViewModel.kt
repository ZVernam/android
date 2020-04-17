package com.github.zeckson.vernam

import androidx.lifecycle.ViewModel

class MainViewModel() : ViewModel() {
    enum class PasswordState {
        NOT_SET,
        RESET,
        SET
    }

    var plainTextValue:String? = null
    var passwordState: PasswordState = PasswordState.NOT_SET

}