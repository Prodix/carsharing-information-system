package com.syndicate.carsharing.utility

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

@Immutable
data class Shadow(
    @Stable val offsetX: Dp,
    @Stable val offsetY: Dp,
    @Stable val radius: Dp,
    @Stable val color: Color,
)