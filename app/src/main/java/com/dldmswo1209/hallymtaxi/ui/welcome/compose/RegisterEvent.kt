package com.dldmswo1209.hallymtaxi.ui.welcome.compose

import androidx.compose.ui.focus.FocusState

// 이벤트 종류
/***
 * 비밀번호 입력
 * 비밀번호 확인 입력
 * 이름 입력
 * 성별 입력
 * 개인정보처리방침 동의
 * 회원가입 버튼 클릭
 */
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

