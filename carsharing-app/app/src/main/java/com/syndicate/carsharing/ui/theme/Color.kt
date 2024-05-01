package com.syndicate.carsharing.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SelectableChipColors
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ChipColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val sliderColors: @Composable () -> SliderColors =  {
    SliderDefaults.colors(
        activeTickColor = Color(0xFF6699CC),
        inactiveTickColor = Color.Transparent,
        inactiveTrackColor = Color(0x806699CC),
        activeTrackColor = Color(0xFF6699CC),
        thumbColor = Color(0xFF34699D)
    )
}

@SuppressLint("UnrememberedMutableInteractionSource")
@OptIn(ExperimentalMaterial3Api::class)
val sliderThumb: @Composable (SliderState) -> Unit = {
    SliderDefaults.Thumb(
        interactionSource = MutableInteractionSource(),
        thumbSize = DpSize(30.dp, 30.dp)
    )
}

val buttonColors: @Composable () -> ButtonColors = {
    ButtonDefaults.buttonColors(
        containerColor = Color(0xFF6699CC),
        contentColor = Color.White,
        disabledContainerColor = Color.Transparent,
        disabledContentColor = Color(0xFFB5B5B5)
    )
}

val buttonColorsNegative: @Composable () -> ButtonColors = {
    ButtonDefaults.buttonColors(
        containerColor = Color(0xFFFAF0F0),
        contentColor = Color(0xFFBB3E3E),
        disabledContainerColor = Color.Transparent,
        disabledContentColor = Color(0xFFB5B5B5)
    )
}

@OptIn(ExperimentalMaterialApi::class)
val filterChipColors: @Composable () -> SelectableChipColors = {
    ChipDefaults.filterChipColors(
        backgroundColor = Color.Transparent,
        selectedBackgroundColor = Color(0x266699CC)
    )
}