package com.dldmswo1209.hallymtaxi.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VerifyCodeTextField(
    text: String,
    onTextChanged: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    BasicTextField(
        value = text,
        onValueChange = onTextChanged,
        modifier = Modifier.focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                text.forEachIndexed { index, char ->
                    TextFieldCharContainer(
                        text = char,
                        isFocused = index == text.lastIndex,
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                    )
                }
                repeat(8-text.length) {
                    TextFieldCharContainer(
                        text = ' ',
                        isFocused = false,
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                    )
                }
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            }
        },
    )
}

@Composable
private fun TextFieldCharContainer(
    modifier: Modifier = Modifier,
    text: Char,
    isFocused: Boolean,
) {
    val shape = remember { RoundedCornerShape(10.dp) }

    Box(
        modifier = modifier
            .background(
                color = Color(0xFFF5F5F5),
                shape = shape,
            )
            .run {
                if (isFocused) {
                    border(
                        width = 1.dp,
                        color = Color(0xFF3351B9),
                        shape = shape,
                    )
                } else {
                    this
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.toString(),
            fontSize = 24.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )
    }
}