package com.syndicate.carsharing.pages.components

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.syndicate.carsharing.R
import com.syndicate.carsharing.pages.DragAnchors
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DoorSlider(
    isClosed: MutableState<Boolean>,
    states: Pair<String, String>,
    action: () -> Unit,
) {
    val density = LocalDensity.current

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
            animationSpec = tween()
        ).apply {
            updateAnchors(
                DraggableAnchors {
                    DragAnchors.Start at 0f
                    DragAnchors.End at with(density) { sliderSize.width.toDp().toPx() - thumbSize.width.toDp().toPx() }
                }
            )
        }
    }

    LaunchedEffect(key1 = swipeableState.currentValue) {
        if (swipeableState.currentValue == DragAnchors.End) {
            swipeableState.anchoredDrag {
                animate(
                    initialValue = swipeableState.requireOffset(),
                    targetValue = 0f,
                    initialVelocity = 0f,
                    animationSpec = tween()
                ) { offset, velocity ->
                    dragTo(offset, velocity)
                }
            }
            isClosed.value = !isClosed.value
            action()
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
            style = MaterialTheme.typography.displaySmall,
            text = if (isClosed.value) states.first else states.second,
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