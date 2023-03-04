package com.dldmswo1209.hallymtaxi.ui.welcome.compose

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.dldmswo1209.hallymtaxi.data.UiState
import com.dldmswo1209.hallymtaxi.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel
@Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private var _password = mutableStateOf(
        TextFieldState(
            hint = "비밀번호"
        )
    )
    val password: State<TextFieldState> = _password

    private var _passwordConfirm = mutableStateOf(
        TextFieldState(
            hint = "비밀번호 확인"
        )
    )
    val passwordConfirm: State<TextFieldState> = _passwordConfirm

    private var _name = mutableStateOf(
        TextFieldState(
            hint = "실명기재"
        )
    )
    val name: State<TextFieldState> = _name

    private var _gender = mutableStateOf(GenderState())
    val gender: State<GenderState> = _gender

    private var _agreePrivacyPolicy = mutableStateOf(false)
    val agreePrivacyPolicy: State<Boolean> = _agreePrivacyPolicy

    private var _guideText = mutableStateOf("비밀번호를 입력해주세요")
    val guideText: State<String> = _guideText

    private var _nextButtonVisible = mutableStateOf(false)
    val nextButtonVisible: State<Boolean> = _nextButtonVisible

    private var _nextCount = mutableStateOf(0)
    private val nextCount: State<Int> = _nextCount

    private var _registerButtonVisible = mutableStateOf(false)
    val registerButtonVisible: State<Boolean> = _registerButtonVisible

    private var _loadingDialogVisible = mutableStateOf(false)
    val loadingDialogVisible: State<Boolean> = _loadingDialogVisible

    private var _failedDialogVisible = mutableStateOf(false)
    val failedDialogVisible: State<Boolean> = _failedDialogVisible

    private var _successDialogVisible = mutableStateOf(false)
    val successDialogVisible: State<Boolean> = _successDialogVisible

    private var _registerResult = mutableStateOf("")
    val registerResult : State<String> = _registerResult

    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.EnteredPassword -> {
                _password.value = password.value.copy(
                    text = event.value,
                    isValid = isValidPassword(event.value)
                )
                _passwordConfirm.value = passwordConfirm.value.copy(
                    isValid = event.value == passwordConfirm.value.text
                )
                _nextButtonVisible.value = password.value.isValid && nextCount.value == 0
            }
            is RegisterEvent.ChangePasswordFocus -> {
                _password.value = password.value.copy(
                    isHintVisible = !event.focusState.isFocused &&
                            password.value.text.isBlank()
                )
            }
            is RegisterEvent.EnteredPasswordConfirm -> {
                _passwordConfirm.value = passwordConfirm.value.copy(
                    text = event.value,
                    isValid = event.value == password.value.text
                )
                _nextButtonVisible.value = passwordConfirm.value.isValid && nextCount.value == 1
            }
            is RegisterEvent.ChangePasswordConfirmFocus -> {
                _passwordConfirm.value = passwordConfirm.value.copy(
                    isHintVisible = !event.focusState.isFocused &&
                            passwordConfirm.value.text.isBlank()
                )
            }
            is RegisterEvent.EnteredName -> {
                _name.value = name.value.copy(
                    text = event.value,
                    isValid = isValidName(event.value)
                )
                _nextButtonVisible.value = name.value.isValid && nextCount.value == 2
            }
            is RegisterEvent.ChangeNameFocus -> {
                _name.value = name.value.copy(
                    isHintVisible = !event.focusState.isFocused &&
                            name.value.text.isBlank()
                )
            }
            is RegisterEvent.EnteredGender -> {
                _gender.value = gender.value.copy(
                    gender = event.value,
                    isGenderAlertVisible = event.value == "선택 안함"
                )
                _nextButtonVisible.value = gender.value.gender.isNotBlank() && nextCount.value == 3
            }
            is RegisterEvent.OnClickPrivacyPolicy -> {
                _agreePrivacyPolicy.value = !agreePrivacyPolicy.value
            }
            is RegisterEvent.OnClickPasswordVisibleIcon -> {
                _password.value = password.value.copy(
                    valueVisible = !password.value.valueVisible
                )
            }
            is RegisterEvent.OnClickPasswordConfirmVisibleIcon -> {
                _passwordConfirm.value = passwordConfirm.value.copy(
                    valueVisible = !passwordConfirm.value.valueVisible
                )
            }
            is RegisterEvent.OnClickRegister -> {
                authRepository.registerUser(event.user, event.password) {
                    when(it) {
                        is UiState.Loading -> {
                            _loadingDialogVisible.value = true
                        }
                        is UiState.Failure -> {
                            _loadingDialogVisible.value = false
                            _failedDialogVisible.value = true
                            _registerResult.value = it.error ?: "회원가입 실패"
                        }
                        is UiState.Success -> {
                            _loadingDialogVisible.value = false
                            _successDialogVisible.value = true
                            _registerResult.value = it.data
                        }
                    }
                }
            }
            is RegisterEvent.Next -> {
                _nextCount.value = nextCount.value + 1

                when (nextCount.value) {
                    1 -> {
                        _password.value = password.value.copy(
                            isOk = true
                        )
                        _guideText.value = "비밀번호를 확인해주세요"
                    }
                    2 -> {
                        _passwordConfirm.value = passwordConfirm.value.copy(
                            isOk = true
                        )
                        _guideText.value = "이름을 입력해주세요"
                    }
                    3 -> {
                        _name.value = name.value.copy(
                            isOk = true
                        )
                        _guideText.value = "성별을 선택해주세요\n(성별을 속이는 행위는 제재 대상 입니다)"
                    }
                    4 -> {
                        _gender.value = gender.value.copy(
                            isOk = true
                        )
                        _guideText.value = "개인정보처리방침에 동의해주세요"
                    }
                }
                _nextButtonVisible.value = false
            }
            is RegisterEvent.OnClickDialogPositiveBotton -> {
                _failedDialogVisible.value = !failedDialogVisible.value
            }

        }
        _registerButtonVisible.value =
            password.value.isValid && passwordConfirm.value.isValid && name.value.isValid && gender.value.gender.isNotBlank() && agreePrivacyPolicy.value
        if(nextCount.value == 4 && agreePrivacyPolicy.value)
            _guideText.value = "안녕하세요 ${name.value.text}님\n회원가입 버튼을 터치해 회원가입을 완료해주세요"
    }

    private fun isValidPassword(password: String) : Boolean {
        val passwordPattern = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&.])[A-Za-z[0-9]$@$!%*#?&.]{8,16}$"
        return Pattern.matches(passwordPattern, password)
    }

    private fun isValidName(name: String) : Boolean {
        val namePattern = "^[ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z]{2,16}$"
        return Pattern.matches(namePattern, name)
    }

}