package com.syndicate.carsharing.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.syndicate.carsharing.modifiers.withShadow
import com.syndicate.carsharing.utility.Shadow
import com.syndicate.carsharing.viewmodels.MainViewModel


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TimerBox(
    modifier: Modifier,
    mainViewModel: MainViewModel
) {
    val mainState by mainViewModel.uiState.collectAsState()

    Text(
        text = if (mainState.timer.isStarted) mainState.timer.toString() else if (mainState.stopwatchChecking.isStarted) mainState.stopwatchChecking.toString() else (mainState.stopwatchOnRoad + mainState.stopwatchOnParking).toString(),
        modifier = modifier
            .then(
                if ((mainState.timer.isStarted ||
                            mainState.stopwatchOnRoad.isStarted ||
                            mainState.stopwatchOnParking.isStarted ||
                                    mainState.stopwatchChecking.isStarted
                            ) && mainState.modalBottomSheetState!!.targetValue !=
                    ModalBottomSheetValue.Expanded) {
                    Modifier.alpha(1f)
                } else {
                    Modifier.alpha(0f)
                }
            )
            .withShadow(
                Shadow(0.dp, 0.dp, 4.dp, Color(0x40000000)),
                RoundedCornerShape(10.dp)
            )
            .background(
                Color.White,
                RoundedCornerShape(10.dp)
            )
            .padding(
                horizontal = 10.dp,
                vertical = 5.dp
            )
    )
}