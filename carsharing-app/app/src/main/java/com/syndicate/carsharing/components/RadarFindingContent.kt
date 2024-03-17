package com.syndicate.carsharing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syndicate.carsharing.viewmodels.MainViewModel
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CircleMapObject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RadarFindingContent(
    mainViewModel: MainViewModel
) {
    // TODO: Реализовать поиск автомобиля

    val mem by mainViewModel.mem.collectAsState()
    val page by mainViewModel.page.collectAsState()
    val circle by mainViewModel.circle.collectAsState()
    val walkMinutes by mainViewModel.walkMinutes.collectAsState()
    val currentLocation by mainViewModel.currentLocation.collectAsState()
    val isGesturesEnabled by mainViewModel.isGesturesEnabled.collectAsState()

    val scope = rememberCoroutineScope()
    val max = mem
    var isOpen = true

    val minutes = remember {
        mutableIntStateOf(29)
    }

    LaunchedEffect(key1 = LocalContext.current) {
        var seconds = 60 * 30

        scope.launch {
            var isIncreasing = false
            while (isOpen) {
                if (isIncreasing)
                    mainViewModel.updateMem(mem + max * 0.05f)
                else
                    mainViewModel.updateMem(mem - max * 0.05f)

                if (mem <= 0.04f)
                    isIncreasing = true

                if (mem >= max)
                    isIncreasing = false


                circle?.geometry = Circle(currentLocation, 400f * mem)

                delay(170)
            }
            circle?.geometry = Circle(currentLocation, 400f * walkMinutes)
        }

        scope.launch {
            while (seconds > 0 && isOpen) {
                seconds--;
                minutes.intValue = seconds / 60
                delay(1000)
            }
            //TODO: Добавить вывод сообщения о конце поиска
        }
    }

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
            text = "Высокий спрос в вашей зоне, машин поблизости нет",
            fontSize = 12.sp,
            color = Color(0xFFC2C2C2)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Ищем ближайший автомобиль",
                fontSize = 12.sp,
                color = Color(0xFFC2C2C2)
            )
            Text(
                text = "${walkMinutes * 5} МИНУТ ПЕШКОМ",
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Осталось ${minutes.intValue} минут",
                fontSize = 12.sp,
                color = Color(0xFFC2C2C2)
            )
        }
        Spacer(modifier = Modifier.size(5.dp))
        Button(
            onClick = {
                mainViewModel.updateIsGesturesEnabled(true)
                isOpen = false
                mainViewModel.updatePage("radarIntro")
                mainViewModel.updateMem(walkMinutes.toFloat())
                circle?.geometry = Circle(currentLocation, 400f * walkMinutes)
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
            Text(text = "Назад")
        }
    }
}