package com.dldmswo1209.hallymtaxi.ui.welcome.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dldmswo1209.hallymtaxi.R
@Composable
fun MultiToggleButton(
    toggleItems: List<String> = listOf("남자", "여자", "선택 안함"),
    toggleStates: GenderState,
    onClick: (String) -> Unit,
    alertText: String = "같은 성별 매칭에서 제외될 수 있어요",
    alertVisibility: Boolean = false
){
    Column(
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalAlignment = Alignment.End,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(29.dp)
        ) {
            toggleItems.forEachIndexed{ index, item->
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(46.dp))
                        .clickable { onClick(item) },
                    backgroundColor = if(item != toggleStates.gender) colorResource(id = R.color.hallym_white_ffffff) else colorResource(id = R.color.hallym_blue_3351b9),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, colorResource(id = R.color.hallym_blue_3351b9))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = toggleItems[index],
                            fontSize = 16.sp,
                            color = if(item != toggleStates.gender) Color.Black else Color.White,
                            modifier = Modifier
                                .align(Alignment.Center)
                        )
                    }
                }
            }
        }
        AnimatedVisibility(visible = alertVisibility) {
            Text(
                text = alertText,
                fontSize = 10.sp,
                color = Color.Black,
            )
        }
    }
}