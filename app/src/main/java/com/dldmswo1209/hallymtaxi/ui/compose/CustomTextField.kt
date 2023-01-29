package com.dldmswo1209.hallymtaxi.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dldmswo1209.hallymtaxi.R


@Composable
fun CustomEditText(
    value: String,
    onValueChange: (String) -> Unit,
    modifier : Modifier = Modifier,
    hintVisibility: Boolean,
    hint: String = "",
    textStyle: TextStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
    label: String = "",
    labelStyle: TextStyle = TextStyle(fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Bold),
    subLabel: String = "",
    subLabelStyle: TextStyle = TextStyle(fontSize = 11.sp, color = colorResource(id = R.color.hallym_black_000000)),
    isVisibleText : Boolean = true,
    trailingIcon: @Composable (()->Unit)? = null,// 오른쪽 아이콘
    trailingText: @Composable (()->Unit)? = null,// 오른쪽 텍스트
    errorVisibility: Boolean = false,
    errorText: String = "",
    autoFocus: Boolean = false,
    maxLines: Int = 1
){
    val focusRequester = remember { FocusRequester() }
    if(autoFocus) {
        LaunchedEffect(key1 = Unit) {
            focusRequester.requestFocus()
        }
    }


    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.Bottom
        ){
            Text(
                text = label,
                style = labelStyle
            )
            Text(
                text = subLabel,
                style = subLabelStyle
            )
            AnimatedVisibility(visible = errorVisibility) {
                Text(
                    text = errorText,
                    fontSize = 11.sp,
                    color = colorResource(id = R.color.hallym_red_E43429),
                )
            }
        }

        BasicTextField(
            modifier = Modifier
                .focusRequester(focusRequester)
            ,
            value = value,
            onValueChange = { onValueChange(it) },
            textStyle = textStyle,
            keyboardOptions = keyboardOptions,
            visualTransformation = if(isVisibleText) VisualTransformation.None else PasswordVisualTransformation(),
            maxLines = maxLines,
            decorationBox = { innerTextField->
                Row(modifier = modifier){

                    Box(modifier = Modifier
                        .weight(1f)
                    ) {
                        innerTextField()
                        if(hintVisibility) {
                            Text(
                                text = hint,
                                fontSize = 16.sp,
                                color = colorResource(id = R.color.hallym_grey_D9D9D9),
                            )
                        }
                    }
                    trailingIcon?.let {
                        if(errorVisibility){
                            Icon(
                                painter = painterResource(id = R.drawable.alert_circle),
                                contentDescription = null,
                                modifier = Modifier
                                    .clip(CircleShape),
                                tint = colorResource(id = R.color.hallym_red_E43429)
                            )
                        }else {
                            it()
                        }
                    }
                    trailingText?.let {
                        it()
                    }

                }

            }
        )

    }
}