package com.dldmswo1209.hallymtaxi.ui.welcome.compose


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.data.model.GENDER_OPTION_FEMALE
import com.dldmswo1209.hallymtaxi.data.model.GENDER_OPTION_MALE
import com.dldmswo1209.hallymtaxi.data.model.GENDER_OPTION_NONE
import com.dldmswo1209.hallymtaxi.data.model.User

@Composable
fun RegisterScreen(
    email: String,
    isCreated: Boolean,
    viewModel: RegisterViewModel = hiltViewModel(),
    onClickRegister: (User, String) -> (Unit),
    onDismissRequest: () -> Unit,
) {
    val passwordState = viewModel.password.value
    val passwordConfirmState = viewModel.passwordConfirm.value
    val nameState = viewModel.name.value
    val genderState = viewModel.gender.value
    val privacyPolicyState = viewModel.agreePrivacyPolicy.value
    val guideText = viewModel.guideText.value
    val nextButtonVisible = viewModel.nextButtonVisible.value
    val registerButtonVisible = viewModel.registerButtonVisible.value
    var dialogVisible by remember { mutableStateOf(false) }

    val passwordVisibilityIcon = if (passwordState.valueVisible) {
        R.drawable.ic_visible_on
    } else {
        R.drawable.ic_visible_off
    }

    val passwordConfirmVisibilityIcon = if (passwordConfirmState.valueVisible) {
        R.drawable.ic_visible_on
    } else {
        R.drawable.ic_visible_off
    }

    val scrollState = rememberScrollState()

    val focusManager = LocalFocusManager.current

    if (dialogVisible) {
        MyAlertDialog(
            visible = dialogVisible,
            onDismissRequest = { onDismissRequest() },
            title = "회원가입",
            content = "회원가입이 완료되었습니다.",
            description = "새 정보로 로그인 해주세요.",
            positiveText = "확인"
        )
    }

    if (isCreated) {
        dialogVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                color = colorResource(id = R.color.white)
            )
            .addFocusCleaner(focusManager)
            .padding(top = 27.dp, start = 20.dp, end = 20.dp, bottom = 10.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()

        ) {
            Text(
                text = "사용자 정보",
                fontSize = 24.sp,
                color = colorResource(id = R.color.hallym_black_000000),
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.size(5.dp))
            Text(
                text = guideText,
                fontSize = 16.sp,
                color = colorResource(id = R.color.hallym_black_000000)
            )
            Spacer(modifier = Modifier.size(15.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(15.dp),
                modifier = Modifier.verticalScroll(scrollState)
            ) {
                AnimatedVisibility(
                    visible = genderState.isOk,
                    enter = expandVertically(
                        expandFrom = Alignment.Top
                    ) + fadeIn(
                        initialAlpha = 0.1f,
                        animationSpec = spring(stiffness = Spring.StiffnessVeryLow)
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row {
                            Text(
                                text = "개인정보처리방침",
                                color = colorResource(id = R.color.hallym_black_000000),
                                fontSize = 14.sp,
                                textDecoration = TextDecoration.Underline
                            )
                            Text(
                                text = "에 동의합니다",
                                color = colorResource(id = R.color.hallym_black_000000),
                                fontSize = 14.sp,
                            )
                        }
                        Checkbox(
                            checked = privacyPolicyState,
                            onCheckedChange = { viewModel.onEvent(RegisterEvent.OnClickPrivacyPolicy) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = colorResource(id = R.color.hallym_blue_3351b9),
                                uncheckedColor = colorResource(id = R.color.hallym_grey_f5f5f5)
                            ),
                            modifier = Modifier.size(20.dp)
                        )

                    }
                }

                AnimatedVisibility(
                    visible = nameState.isOk,
                    enter = expandVertically(
                        expandFrom = Alignment.Top
                    ) + fadeIn(
                        initialAlpha = 0.1f,
                        animationSpec = spring(stiffness = Spring.StiffnessVeryLow)
                    ),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(
                            text = "성별",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = colorResource(id = R.color.hallym_black_000000),
                                fontWeight = FontWeight.Bold
                            )
                        )
                        MultiToggleButton(
                            toggleStates = genderState,
                            onClick = { viewModel.onEvent(RegisterEvent.EnteredGender(it)) },
                            alertVisibility = genderState.isGenderAlertVisible
                        )
                    }

                }

                AnimatedVisibility(
                    visible = passwordConfirmState.isOk,
                    enter = expandVertically(
                        expandFrom = Alignment.Top
                    ) + fadeIn(
                        initialAlpha = 0.1f,
                        animationSpec = spring(stiffness = Spring.StiffnessVeryLow)
                    ),
                ) {
                    CustomEditText(
                        value = nameState.text,
                        onValueChange = { viewModel.onEvent(RegisterEvent.EnteredName(it)) },
                        onFocusChange = { viewModel.onEvent(RegisterEvent.ChangeNameFocus(it)) },
                        hintVisibility = nameState.isHintVisible,
                        hint = nameState.hint,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                shape = RoundedCornerShape(10.dp),
                                color = colorResource(id = R.color.hallym_grey_f5f5f5)
                            )
                            .run {
                                if (!nameState.isValid) {
                                    border(
                                        width = 1.dp,
                                        color = colorResource(id = R.color.hallym_red_E43429),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                } else {
                                    this
                                }
                            }
                            .padding(horizontal = 13.dp, vertical = 8.dp),
                        label = "이름",
                        errorVisibility = !nameState.isValid,
                        errorText = "잘못된 이름 형식입니다.",
                        autoFocus = true,
                        trailingIcon = {}
                    )
                }

                AnimatedVisibility(
                    visible = passwordState.isOk,
                    enter = expandVertically(
                        expandFrom = Alignment.Top
                    ) + fadeIn(
                        initialAlpha = 0.1f,
                        animationSpec = spring(stiffness = Spring.StiffnessVeryLow)
                    ),
                ) {
                    CustomEditText(
                        value = passwordConfirmState.text,
                        onValueChange = { viewModel.onEvent(RegisterEvent.EnteredPasswordConfirm(it)) },
                        onFocusChange = {
                            viewModel.onEvent(
                                RegisterEvent.ChangePasswordConfirmFocus(
                                    it
                                )
                            )
                        },
                        hintVisibility = passwordConfirmState.isHintVisible,
                        hint = passwordConfirmState.hint,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                shape = RoundedCornerShape(10.dp),
                                color = colorResource(id = R.color.hallym_grey_f5f5f5)
                            )
                            .run {
                                if (!passwordConfirmState.isValid) {
                                    border(
                                        width = 1.dp,
                                        color = colorResource(id = R.color.hallym_red_E43429),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                } else {
                                    this
                                }
                            }
                            .padding(horizontal = 13.dp, vertical = 8.dp),
                        label = "비밀번호 확인",
                        isVisibleText = passwordConfirmState.valueVisible,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = passwordConfirmVisibilityIcon),
                                contentDescription = null,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        viewModel.onEvent(RegisterEvent.OnClickPasswordConfirmVisibleIcon)
                                    }

                            )
                        },
                        errorVisibility = !passwordConfirmState.isValid,
                        errorText = "비밀번호가 일치하지 않습니다.",
                        autoFocus = true
                    )
                }

                CustomEditText(
                    value = passwordState.text,
                    onValueChange = { viewModel.onEvent(RegisterEvent.EnteredPassword(it)) },
                    onFocusChange = { viewModel.onEvent(RegisterEvent.ChangePasswordFocus(it)) },
                    hintVisibility = passwordState.isHintVisible,
                    hint = passwordState.hint,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            shape = RoundedCornerShape(10.dp),
                            color = colorResource(id = R.color.hallym_grey_f5f5f5)
                        )
                        .padding(horizontal = 13.dp, vertical = 8.dp)
                        .focusTarget(),
                    label = "비밀번호",
                    subLabel = "(영문 대/소문자, 숫자, 특수문자 포함 8~16자)",
                    isVisibleText = passwordState.valueVisible,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = passwordVisibilityIcon),
                            contentDescription = null,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    viewModel.onEvent(RegisterEvent.OnClickPasswordVisibleIcon)
                                }

                        )
                    },
                    autoFocus = true
                )
                Spacer(modifier = Modifier.size(100.dp))
            }
        }

        if (nextButtonVisible) {
            Button(
                onClick = {
                    viewModel.onEvent(RegisterEvent.Next)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .imePadding(),
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.hallym_blue_3351b9)
                )

            ) {
                Text(
                    text = "다음",
                    fontSize = 15.sp,
                    color = colorResource(id = R.color.hallym_white_ffffff)
                )
            }
        }
        if (registerButtonVisible) {
            Button(
                onClick = {
                    val user = User(
                        email = email,
                        name = nameState.text,
                        gender = when(genderState.gender) {
                            "남자" -> {
                                GENDER_OPTION_MALE
                            }
                            "여자" -> {
                                GENDER_OPTION_FEMALE
                            }
                            else -> {
                                GENDER_OPTION_NONE
                            }
                        }
                    )
                    onClickRegister(user, passwordState.text)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .imePadding(),
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.hallym_blue_3351b9),
                    disabledBackgroundColor = colorResource(id = R.color.hallym_grey_f5f5f5)
                ),

                ) {
                Text(
                    text = "회원가입",
                    fontSize = 15.sp,
                    color = colorResource(id = R.color.hallym_white_ffffff),
                )
            }
        }

    }
}

fun Modifier.addFocusCleaner(focusManager: FocusManager, doOnClear: () -> Unit = {}): Modifier {
    return this.pointerInput(Unit) {
        detectTapGestures(onTap = {
            doOnClear()
            focusManager.clearFocus()
        })
    }
}