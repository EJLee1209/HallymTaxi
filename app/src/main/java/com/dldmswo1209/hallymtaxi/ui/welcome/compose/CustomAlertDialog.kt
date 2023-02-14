package com.dldmswo1209.hallymtaxi.ui.welcome.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dldmswo1209.hallymtaxi.R


@Composable
fun CustomAlertDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit
){
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ){
        content()
    }
}

@Composable
fun MyAlertDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    content: String,
    description: String? = null,
    positiveText: String
){
    if(visible){
        CustomAlertDialog(onDismissRequest = { onDismissRequest() }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color = colorResource(id = R.color.hallym_white_ffffff))
                    .padding(vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = content,
                    fontSize = 14.sp,
                    color = Color.Black,
                )
                description?.let {
                    Spacer(modifier = Modifier.size(18.dp))
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = Color.Black,
                    )
                }
                Spacer(modifier = Modifier.size(18.dp))
                Divider(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.size(18.dp))
                Text(
                    text = positiveText,
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.hallym_blue_3351b9),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            onDismissRequest()
                        }
                )
            }

        }
    }

}

@Preview
@Composable
fun MyAlertDialog(){
    MyAlertDialog(
        visible = true,
        onDismissRequest = {  },
        title = "재학생 인증",
        content = "이미 계정이 존재합니다.",
        positiveText = "확인"
    )
}