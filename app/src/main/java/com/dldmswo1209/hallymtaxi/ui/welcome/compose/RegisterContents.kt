package com.dldmswo1209.hallymtaxi.ui.welcome.compose

sealed class RegisterContents {
    object Password: RegisterContents()
    object PasswordConfirm: RegisterContents()
    object Name: RegisterContents()
    object Gender: RegisterContents()
    object PrivacyPolicy: RegisterContents()
}