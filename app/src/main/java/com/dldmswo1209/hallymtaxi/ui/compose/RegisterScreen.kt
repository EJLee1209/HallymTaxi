package com.dldmswo1209.hallymtaxi.ui.compose


import android.util.Log
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
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.model.User

@Composable
fun RegisterScreen(
    email: String,
    isCreated: Boolean,
    onClickRegister: (User, String)->(Unit),
    onDismissRequest: () -> Unit
) {
    val scrollState = rememberScrollState()
    var dialogVisibility by remember { mutableStateOf(false) }

    if(dialogVisibility){
        MyAlertDialog(
            visible = dialogVisibility,
            onDismissRequest = { onDismissRequest() },
            title = "회원가입",
            content = "회원가입이 완료되었습니다.",
            description = "새 정보로 로그인 해주세요.",
            positiveText = "확인"
        )
    }

    if(isCreated) {
        Log.d("testt", "회원가입 성공")
        dialogVisibility = true
    }else{
        Log.d("testt", "회원가입 실패")
    }

    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }

    var passwordHintVisibility by remember { mutableStateOf(true) }
    var passwordConfirmHintVisibility by remember { mutableStateOf(true) }
    var passwordConfirmErrorVisibility by remember { mutableStateOf(true) }
    var nameHintVisibility by remember { mutableStateOf(true) }
    var nameErrorVisibility by remember { mutableStateOf(true) }
    var nextButtonVisibility by remember { mutableStateOf(false) }
    var registerButtonVisibility by remember { mutableStateOf(false) }
    var registerButtonEnabled by remember { mutableStateOf(false) }

    passwordHintVisibility = password == ""
    passwordConfirmHintVisibility = passwordConfirm == ""
    nameHintVisibility = name == ""
    passwordConfirmErrorVisibility = password != passwordConfirm
    nameErrorVisibility = name.length < 2

    var passwordOk by remember { mutableStateOf(false) }
    var passwordConfirmOk by remember { mutableStateOf(false) }
    var nameOk by remember { mutableStateOf(false) }
    var genderOk by remember { mutableStateOf(false) }
    var privacyPolicyAgreeOk by remember { mutableStateOf(false) }
    var isPasswordShow by remember { mutableStateOf(false) }
    var isPasswordConfirmShow by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val passwordVisibilityIcon = if(isPasswordShow){
        R.drawable.ic_visible_on
    }else{
        R.drawable.ic_visible_off
    }

    val passwordConfirmVisibilityIcon = if(isPasswordConfirmShow){
        R.drawable.ic_visible_on
    }else{
        R.drawable.ic_visible_off
    }

    // 성별 토글 버튼 상태
    var isMale by remember { mutableStateOf(false) }
    var isFemale by remember { mutableStateOf(false) }
    var isNoneBinary by remember { mutableStateOf(false) }
    var genderAlertVisibility by remember { mutableStateOf(false) }

    // 성별 선택 토글 버튼 클릭 이벤트
    val clickMale : () -> Unit = {
        if(!isMale){
            isMale = true
            isFemale = false
            isNoneBinary = false
            genderAlertVisibility = false
            gender = "male"
        }
    }
    val clickFeMale : () -> Unit = {
        if(!isFemale){
            isFemale = true
            isMale = false
            isNoneBinary = false
            genderAlertVisibility = false
            gender = "female"
        }
    }
    val clickNoneBinary : () -> Unit = {
        if(!isNoneBinary){
            isNoneBinary = true
            isMale = false
            isFemale = false
            genderAlertVisibility = true
            gender = "none"
        }
    }

    // 상단에 나오는 안내 문구
    var guideText by remember { mutableStateOf("비밀번호를 입력해주세요.") }

    // 다음 버튼을 누른 횟수
    var nextCount by remember { mutableStateOf(0) }

    if(password.length >= 8 && !passwordOk) {
        nextButtonVisibility = true
    }
    if(password != "" && password == passwordConfirm && !passwordConfirmOk) {
        nextButtonVisibility = true
    }
    if(name.length >= 2 && !nameOk) {
        nextButtonVisibility = true
    }
    if(isMale || isFemale || isNoneBinary){ // 성별 선택완료
        genderOk = true
        guideText = "개인정보처리방침을 확인해주세요"
    }
    if(privacyPolicyAgreeOk){
        registerButtonVisibility = true
        guideText = "${name}님, 입력한 정보가 모두 확실한가요?"
    }

    // 다음 버튼 클릭 이벤트
    val onNextClick : ()->Unit = {
        when(nextCount){
            0 -> {
                passwordOk = true
                guideText = "비밀번호를 확인해주세요"
            }
            1 -> {
                passwordConfirmOk = true
                guideText = "이름(실명)을 입력해주세요"
            }
            2 -> {
                nameOk = true
                guideText = "성별을 선택해주세요"
                focusManager.clearFocus()
            }
            3 -> {
                genderOk = true
            }
        }
        nextCount++
        nextButtonVisibility = false
    }

    registerButtonEnabled = password.length >= 8 && password == passwordConfirm && name.length >= 2 && privacyPolicyAgreeOk

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                color = colorResource(id = R.color.white))
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
                    visible = genderOk,
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
                    ){
                        Row(){
                            Text(
                                text="개인정보처리방침",
                                color = colorResource(id = R.color.hallym_black_000000),
                                fontSize = 14.sp,
                                textDecoration = TextDecoration.Underline
                            )
                            Text(
                                text="에 동의합니다",
                                color = colorResource(id = R.color.hallym_black_000000),
                                fontSize = 14.sp,
                            )
                        }
                        Checkbox(
                            checked = privacyPolicyAgreeOk,
                            onCheckedChange = { privacyPolicyAgreeOk = it } ,
                            colors = CheckboxDefaults.colors(
                                checkedColor = colorResource(id = R.color.hallym_blue_3351b9),
                                uncheckedColor = colorResource(id = R.color.hallym_grey_f5f5f5)
                            ),
                            modifier = Modifier.size(20.dp)
                        )

                    }
                }

                AnimatedVisibility(
                    visible = nameOk,
                    enter = expandVertically(
                        expandFrom = Alignment.Top
                    ) + fadeIn(
                        initialAlpha = 0.1f,
                        animationSpec = spring(stiffness = Spring.StiffnessVeryLow)
                    ),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)){
                        Text(
                            text = "성별",
                            style = TextStyle(fontSize = 14.sp, color = colorResource(id = R.color.hallym_black_000000), fontWeight = FontWeight.Bold)
                        )
                        MultiToggleButton(
                            toggleItems = listOf("남자", "여자", "선택 안함"),
                            toggleStates = listOf(isMale, isFemale, isNoneBinary),
                            onClicks = listOf(clickMale, clickFeMale, clickNoneBinary),
                            alertText = "같은 성별 매칭에서 제외될 수 있어요",
                            alertVisibility = genderAlertVisibility
                        )
                    }

                }

                AnimatedVisibility(
                    visible = passwordConfirmOk,
                    enter = expandVertically(
                        expandFrom = Alignment.Top
                    ) + fadeIn(
                        initialAlpha = 0.1f,
                        animationSpec = spring(stiffness = Spring.StiffnessVeryLow)
                    ),
                ) {
                    CustomEditText(
                        value = name,
                        onValueChange = { name = it },
                        hintVisibility = nameHintVisibility,
                        hint = "실명기재",
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                shape = RoundedCornerShape(10.dp),
                                color = colorResource(id = R.color.hallym_grey_f5f5f5)
                            )
                            .run {
                                if (nameErrorVisibility) {
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
                        errorVisibility = nameErrorVisibility,
                        errorText = "잘못된 이름 형식입니다.",
                        autoFocus = true,
                        trailingIcon = {}
                    )
                }

                AnimatedVisibility(
                    visible = passwordOk,
                    enter = expandVertically(
                        expandFrom = Alignment.Top
                    ) + fadeIn(
                        initialAlpha = 0.1f,
                        animationSpec = spring(stiffness = Spring.StiffnessVeryLow)
                    ),
                ) {
                    CustomEditText(
                        value = passwordConfirm,
                        onValueChange = { passwordConfirm = it },
                        hintVisibility = passwordConfirmHintVisibility,
                        hint = "비밀번호",
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                shape = RoundedCornerShape(10.dp),
                                color = colorResource(id = R.color.hallym_grey_f5f5f5)
                            )
                            .run {
                                if (passwordConfirmErrorVisibility) {
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
                        isVisibleText = isPasswordConfirmShow,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = passwordConfirmVisibilityIcon),
                                contentDescription = null,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        isPasswordConfirmShow = !isPasswordConfirmShow
                                    }

                            )
                        },
                        errorVisibility = passwordConfirmErrorVisibility,
                        errorText = "비밀번호가 일치하지 않습니다.",
                        autoFocus = true
                    )
                }

                CustomEditText(
                    value = password,
                    onValueChange = { password = it },
                    hintVisibility = passwordHintVisibility,
                    hint = "비밀번호",
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(shape = RoundedCornerShape(10.dp),
                            color = colorResource(id = R.color.hallym_grey_f5f5f5))
                        .padding(horizontal = 13.dp, vertical = 8.dp)
                        .focusTarget(),
                    label = "비밀번호",
                    subLabel = "(영문 대/소문자, 숫자, 특수문자 포함 8~16자)",
                    isVisibleText = isPasswordShow,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = passwordVisibilityIcon),
                            contentDescription = null,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    isPasswordShow = !isPasswordShow
                                }

                        )
                    },
                    autoFocus = true
                )
                Spacer(modifier = Modifier.size(100.dp))
            }
        }

        if(nextButtonVisibility) {
            Button(
                onClick = { onNextClick() },
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
        if(registerButtonVisibility) {
            Button(
                onClick = {
                    val user = User(email=email, name = name, gender = gender)
                    onClickRegister(user, password)
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
                enabled = registerButtonEnabled

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
    return this.pointerInput(Unit){
        detectTapGestures(onTap = {
            doOnClear()
            focusManager.clearFocus()
        })
    }
}