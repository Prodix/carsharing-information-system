package com.syndicate.carsharing.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun BottomSheetWithPages(
    sheetState: ModalBottomSheetState,
    isGesturesEnabled: MutableState<Boolean>,
    page: MutableState<String>,
    sheetComposableList: Map<String, @Composable () -> Unit>,
    walkMinutes: MutableState<Int>
) {
    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        scrimColor = Color.Transparent,
        sheetGesturesEnabled = isGesturesEnabled.value,
        sheetContent = {
            Box {
                sheetComposableList[page.value]?.invoke()
            }
        }
    ) {
    }
    Box(
        modifier = Modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .then(
                    if (!isGesturesEnabled.value) {
                        Modifier
                            .clickable(
                                indication = null,
                                interactionSource = MutableInteractionSource()
                            ) { }
                    } else {
                        Modifier
                    }
                )
        ) {
        }
    }
}