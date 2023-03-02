package com.dldmswo1209.hallymtaxi.ui.welcome.compose

import androidx.compose.ui.focus.FocusState

// 회원가입 화면 이벤트
sealed class RegisterEvent {
    data class EnteredPassword(val value: String) : RegisterEvent()
    data class ChangePasswordFocus(val focusState: FocusState) : RegisterEvent()
    data class EnteredPasswordConfirm(val value: String) : RegisterEvent()
    data class ChangePasswordConfirmFocus(val focusState: FocusState) : RegisterEvent()
    data class EnteredName(val value: String) : RegisterEvent()
    data class ChangeNameFocus(val focusState: FocusState) : RegisterEvent()
    data class EnteredGender(val value: String) : RegisterEvent()
    object Next : RegisterEvent()
    object OnClickPrivacyPolicy : RegisterEvent()
    object OnClickPasswordVisibleIcon: RegisterEvent()
    object OnClickPasswordConfirmVisibleIcon: RegisterEvent()
}

