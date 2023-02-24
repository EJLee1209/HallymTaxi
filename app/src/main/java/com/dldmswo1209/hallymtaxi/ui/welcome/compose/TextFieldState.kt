package com.dldmswo1209.hallymtaxi.ui.welcome.compose

data class TextFieldState(
    val text : String = "",
    val hint: String = "",
    val isHintVisible: Boolean = true,
    val valueVisible: Boolean = false,
    val isValid: Boolean = false,
    val isOk: Boolean = false,
)
