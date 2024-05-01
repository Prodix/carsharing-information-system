package com.syndicate.carsharing.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.syndicate.carsharing.pages.components.CarPresenter
import com.syndicate.carsharing.database.models.Transport
import com.syndicate.carsharing.viewmodels.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun ResultContent(
    mainViewModel: MainViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mainState by mainViewModel.uiState.collectAsState()
    val transportInfo = mainState.lastSelectedPlacemark?.userData as Transport

    mainViewModel.updateRenting(false)

    LaunchedEffect(key1 = context) {
        launch(Dispatchers.IO) {
            val time = mainViewModel.userStore.getCheckingTime().first()
            if (time / 60 > 5) {
                mainState.stopwatchChecking.minutes = time / 60 - 5
                mainState.stopwatchChecking.seconds = time % 60
            }
        }
        mainViewModel.viewModelScope.launch {
            mainState.timer.stop()
        }
    }

    Column (
        modifier = Modifier
            .padding(horizontal = 15.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()) {
            Spacer(
                modifier = Modifier
                    .width(30.dp)
                    .height(4.dp)
                    .background(
                        Color(0xFFB5B5B5),
                        shape = CircleShape
                    )
                    .align(Alignment.Center)
            )
        }
        CarPresenter(transportInfo)
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                style = MaterialTheme.typography.displayMedium,
                text = "Время в пути"
            )
            Text(
                style = MaterialTheme.typography.displayMedium,
                text = mainState.stopwatchOnRoad.toString()
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                style = MaterialTheme.typography.displayMedium,
                text = "Время ожидания"
            )
            Text(
                style = MaterialTheme.typography.displayMedium,
                text = mainState.stopwatchOnParking.toString()
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                style = MaterialTheme.typography.displayMedium,
                text = "Платный осмотр"
            )
            Text(
                style = MaterialTheme.typography.displayMedium,
                text = mainState.stopwatchChecking.toString()
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                style = MaterialTheme.typography.displayMedium,
                text = "Итого"
            )
            Text(
                style = MaterialTheme.typography.displayMedium,
                text = "${String.format("%.2f", mainState.lastSelectedRate!!.onRoadPrice * 
                    (mainState.stopwatchOnRoad.minutes + (if (mainState.stopwatchOnRoad.minutes > 0 || mainState.stopwatchOnRoad.seconds > 0) 1 else 0)) 
                    + mainState.lastSelectedRate!!.parkingPrice * (mainState.stopwatchOnParking.minutes + (if (mainState.stopwatchOnParking.minutes > 0 || mainState.stopwatchOnParking.seconds > 0) 1 else 0))
                + 10 * (if (mainState.stopwatchChecking.minutes > 0 || mainState.stopwatchChecking.seconds > 0) mainState.stopwatchChecking.minutes + 1 else 0)
            )} ₽"
            )
        }
    }
}