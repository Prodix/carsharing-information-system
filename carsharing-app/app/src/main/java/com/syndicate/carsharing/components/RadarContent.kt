package com.syndicate.carsharing.components

import android.annotation.SuppressLint
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CircleMapObject

@SuppressLint("UnrememberedMutableInteractionSource")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarContent(
    circle: MutableState<CircleMapObject?>,
    currentLocation: MutableState<Point>,
    isGesturesEnabled: MutableState<Boolean>,
    page: MutableState<String>,
    mem: MutableFloatState,
    walkMinutes: MutableState<Int>
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 15.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Радар",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        )
        Text(
            text = "Сколько мне идти",
            fontSize = 12.sp,
            color = Color(0xFFC2C2C2)
        )
        Column {
            Slider(
                value = walkMinutes.value.toFloat(),
                steps = 2,
                valueRange = 1f..4f,
                onValueChange = {
                    mem.floatValue = it
                    walkMinutes.value = it.toInt()
                    circle.value?.geometry = Circle(currentLocation.value, 400f * it)
                },
                colors = SliderDefaults.colors(
                    activeTickColor = Color(0xFF6699CC),
                    inactiveTickColor = Color.Transparent,
                    inactiveTrackColor = Color(0x806699CC),
                    activeTrackColor = Color(0xFF6699CC),
                    thumbColor = Color(0xFF34699D)
                ),
                thumb = {
                    SliderDefaults.Thumb(
                        interactionSource = MutableInteractionSource(),
                        thumbSize = DpSize(30.dp, 30.dp)
                    )
                }
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ){
                Text(
                    text = "5 мин",
                    fontSize = 12.sp,
                    color = if (walkMinutes.value.toFloat() == 1f) Color.Black else Color(0xFFC2C2C2)
                )
                Text(
                    text = "10 мин",
                    fontSize = 12.sp,
                    color = if (walkMinutes.value.toFloat() == 2f) Color.Black else Color(0xFFC2C2C2)
                )
                Text(
                    text = "15 мин",
                    fontSize = 12.sp,
                    color = if (walkMinutes.value.toFloat() == 3f) Color.Black else Color(0xFFC2C2C2)
                )
                Text(
                    text = "20 мин",
                    fontSize = 12.sp,
                    color = if (walkMinutes.value.toFloat() == 4f) Color.Black else Color(0xFFC2C2C2)
                )
            }
        }
        Spacer(modifier = Modifier.size(5.dp))
        Button(
            onClick = {
                isGesturesEnabled.value = false
                page.value = "radar"
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6699CC),
                contentColor = Color.White,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color(0xFFB5B5B5)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(text = "Начать поиск")
        }
    }
}