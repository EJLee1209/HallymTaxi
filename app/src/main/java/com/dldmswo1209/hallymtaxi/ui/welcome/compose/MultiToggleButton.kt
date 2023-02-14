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
    toggleItems: List<String>,
    toggleStates: List<Boolean>,
    onClicks: List<()->Unit>,
    alertText: String = "",
    alertVisibility: Boolean = false
){
    Column(
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalAlignment = Alignment.End
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
                        .clickable { onClicks[index]() },
                    backgroundColor = if(!toggleStates[index]) colorResource(id = R.color.hallym_white_ffffff) else colorResource(id = R.color.hallym_blue_3351b9),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, colorResource(id = R.color.hallym_blue_3351b9))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = toggleItems[index],
                            fontSize = 16.sp,
                            color = if(!toggleStates[index]) Color.Black else Color.White,
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