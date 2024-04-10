package com.syndicate.carsharing.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.syndicate.carsharing.R
import com.syndicate.carsharing.data.Timer
import com.syndicate.carsharing.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun CheckContent(
    mainViewModel: MainViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val timer = mainViewModel.timer.collectAsState()

    DisposableEffect(key1 = context) {
        mainViewModel.updateScrim(Color(0xB5000000))

        if (timer.value.defaultMinutes != 5) {
            timer.value.changeStartTime(5,0)
        }

        if (!timer.value.isStarted) {
            mainViewModel.viewModelScope.launch {
                timer.value.start()
            }
        }

        onDispose {
            mainViewModel.updateScrim(Color.Transparent)
        }
    }

    Column (
        modifier = Modifier
            .padding(horizontal = 15.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Недостатки, о которых мы уже знаем"
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(3) {
                Image(
                    painter = BitmapPainter(ImageBitmap.imageResource(R.drawable.passport)),
                    contentDescription = null,
                    modifier = Modifier
                        .width(190.dp)
                        .height(115.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.FillWidth
                )
            }
        }
        Box (
            modifier = Modifier
                .padding(4.dp)
        ){
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0x666699CC),
                        RoundedCornerShape(10.dp)
                    )
                    .drawBehind {
                        drawRoundRect(
                            color = Color(0xFF6699CC),
                            style = Stroke(
                                width = 4f,
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(10f, 10f)
                                )
                            ),
                            cornerRadius = CornerRadius(x = 10.dp.toPx(), y = 10.dp.toPx())
                        )
                    }
                    .clickable { }
                    .padding(vertical = 25.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.Center)
                ) {
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.camera),
                        contentDescription = null
                    )
                    Text(
                        text = "Добавить фото"
                    )
                }
            }
        }
        Text(
            text = "Если вы не обнаружили новые повреждения и ознакомились с правилами пользователя, то можно отправляться в путь",
            modifier = Modifier
                .fillMaxWidth()
        )
        Text(
            text = "Правила пользователя",
            modifier = Modifier
                .fillMaxWidth()
        )
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Время бесплатного осмотра закончится через"
            )
            Text(
                text = "${timer.value}"
            )
        }
        Button(
            onClick = { mainViewModel.updatePage("rentPage") },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Начать аренду"
            )
        }
    }
}