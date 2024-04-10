package com.syndicate.carsharing.components

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewModelScope
import com.syndicate.carsharing.R
import com.syndicate.carsharing.data.Timer
import com.syndicate.carsharing.modifiers.withShadow
import com.syndicate.carsharing.utility.Shadow
import com.syndicate.carsharing.viewmodels.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class DragAnchors {
    Start,
    End
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DoorSlider(
    isClosed: MutableState<Boolean>,
    mainViewModel: MainViewModel
) {
    val density = LocalDensity.current
    var isInitialized by remember {
        mutableStateOf(false)
    }

    var sliderSize by remember {
        mutableStateOf(IntSize.Zero)
    }

    var thumbSize by remember {
        mutableStateOf(IntSize.Zero)
    }

    val swipeableState = remember {
        AnchoredDraggableState(
            initialValue = DragAnchors.Start,
            positionalThreshold = { totalDistance: Float -> totalDistance * 0.5f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            animationSpec = tween(),
            confirmValueChange = {
                if (it == DragAnchors.End) {
                    isClosed.value = !isClosed.value
                    true
                }
                else {
                    true
                }
            }
        ).apply {
            updateAnchors(
                DraggableAnchors {
                    DragAnchors.Start at 0f
                    DragAnchors.End at with(density) { sliderSize.width.toDp().toPx() - thumbSize.width.toDp().toPx() }
                }
            )
        }
    }

    LaunchedEffect(key1 = isClosed.value) {
        if (isInitialized) {
            while (swipeableState.isAnimationRunning)
                delay(200)
            swipeableState.anchoredDrag(
                targetValue = DragAnchors.Start
            ) { _, _ ->
                animate(
                    initialValue = swipeableState.requireOffset(),
                    initialVelocity = 0f,
                    targetValue = 0f,
                    animationSpec = tween()
                ) { value, velocity ->
                    dragTo(value, velocity)
                }
            }
            mainViewModel.updatePage("checkPage")
        }
        else {
            isInitialized = true
        }
    }

    SideEffect {
        swipeableState.updateAnchors(
            DraggableAnchors {
                DragAnchors.Start at 0f
                DragAnchors.End at with(density) { sliderSize.width.toDp().toPx() - thumbSize.width.toDp().toPx() }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0x1A6699CC),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(5.dp)
            .onSizeChanged {
                sliderSize = it
            }
            .anchoredDraggable(
                state = swipeableState,
                orientation = Orientation.Horizontal
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box (
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = swipeableState
                            .requireOffset()
                            .roundToInt(),
                        y = 0
                    )
                }
                .onSizeChanged {
                    thumbSize = it
                }
                .zIndex(10f)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = Color(0xFF6699CC),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(
                        horizontal = 25.dp,
                        vertical = 15.dp
                    )

            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.double_arrow),
                    contentDescription = null
                )
            }
        }
        Text(
            text = if (isClosed.value) "Открыть двери" else "Закрыть двери",
            modifier = Modifier
                .zIndex(9f)
        )
        Image(
            imageVector = ImageVector.vectorResource(if (isClosed.value) R.drawable.opened_lock else R.drawable.closed_lock),
            contentDescription = null,
            modifier = Modifier
                .padding(end = 20.dp)
                .zIndex(9f)
        )
    }
}

@Composable
fun ReservationContent(
    mainViewModel: MainViewModel
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val timer = mainViewModel.timer.collectAsState()

    LaunchedEffect(key1 = context) {
        if (!timer.value.isStarted) {
            timer.value.changeStartTime(20,0)
            mainViewModel.viewModelScope.launch {
                timer.value.start()
            }
        }
    }

    val isClosed = remember {
        mutableStateOf(true)
    }
    Column (
        modifier = Modifier
            .padding(horizontal = 15.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ){
            Text(
                text = "Daewoo Nexia"
            )
            Box(
                modifier = Modifier
                    .background(Color(0x80A3C2E0), RoundedCornerShape(5.dp))
            ) {
                Text(
                    modifier = Modifier
                        .padding(5.dp),
                    text = "C 409 MM 797"
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.fuel),
                    contentDescription = null
                )
                Text(
                    text = "234 км • 55%"
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.car),
                    contentDescription = null
                )
                Text(
                    text = "Базовая"
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.insurance),
                    contentDescription = null
                )
                Text(
                    text = "Без каско"
                )
            }
        }
        Image(
            painter = BitmapPainter(
                image = ImageBitmap.imageResource(id = R.drawable.nexia)
            ),
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth(),
            contentDescription = null
        )
        DoorSlider(
            isClosed = isClosed,
            mainViewModel = mainViewModel
        )
        Text(
            text = "Откройте двери, чтобы перейти к осмотру автомобиля"
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(30.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .withShadow(
                        Shadow(0.dp, 0.dp, 4.dp, Color(0x40000000)),
                        RoundedCornerShape(15.dp)
                    )
                    .background(
                        Color.White,
                        RoundedCornerShape(15.dp)
                    )
                    .padding(
                        horizontal = 10.dp,
                        vertical = 15.dp
                    )
                    .weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.flash),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = "Поморгать"
                )
            }

            Row(
                modifier = Modifier
                    .withShadow(
                        Shadow(0.dp, 0.dp, 4.dp, Color(0x40000000)),
                        RoundedCornerShape(15.dp)
                    )
                    .background(
                        Color.White,
                        RoundedCornerShape(15.dp)
                    )
                    .padding(
                        horizontal = 10.dp,
                        vertical = 15.dp
                    )
                    .weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.volume),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = "Посигналить"
                )
            }
        }
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Бронирование закончится через"
            )
            Text(
                text = "${timer.value}"
            )
        }
        Button(
            onClick = {
                      /* TODO */
            },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Отменить бронирование"
            )
        }
    }
}