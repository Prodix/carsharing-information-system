package com.syndicate.carsharing.pages

import android.annotation.SuppressLint
import android.app.AlertDialog
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.syndicate.carsharing.shared_components.AutoShareButton
import com.syndicate.carsharing.pages.components.CarPresenter
import com.syndicate.carsharing.pages.components.DoorSlider
import com.syndicate.carsharing.database.HttpClient
import com.syndicate.carsharing.database.models.DefaultResponse
import com.syndicate.carsharing.database.models.Transport
import com.syndicate.carsharing.viewmodels.MainViewModel
import io.ktor.client.call.body
import io.ktor.client.request.post
import kotlinx.coroutines.launch

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun RentContent(
    mainViewModel: MainViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val token by mainViewModel.userStore.getToken().collectAsState(initial = "")
    val mainState by mainViewModel.uiState.collectAsState()
    val transportInfo = mainState.lastSelectedPlacemark?.userData as Transport

    val isClosed = remember {
        mutableStateOf(mainState.isClosed)
    }

    LaunchedEffect(key1 = Unit) {
        mainViewModel.updateRenting(true)
        if (!mainState.stopwatchOnParking.isStarted &&
            !mainState.stopwatchOnRoad.isStarted &&
            !mainState.timer.isStarted) {
            mainState.stopwatchOnParking.clear()
            if (mainState.isFixed) {
                mainState.timer.changeStartTime(mainState.rentHours, 0, 0)
                mainState.timer.start()
            } else {
                mainState.timer.stop()
                mainViewModel.viewModelScope.launch {
                    mainState.stopwatchOnRoad.restart()
                }
            }
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
        DoorSlider(
            isClosed = isClosed,
            states = Pair("Разблокировать автомобиль", "Заблокировать автомобиль"),
        ) {
            scope.launch {
                val response = HttpClient.client.post(
                    "${HttpClient.url}/transport/${(if (isClosed.value) "lock" else "unlock")}?transportId=${mainState.lastSelectedRate!!.transportId}&rateId=${mainState.lastSelectedRate!!.id}"
                ) {
                    headers["Authorization"] = "Bearer $token"
                }.body<DefaultResponse>()

                if (response.status_code != 200) {
                    AlertDialog.Builder(context)
                        .setMessage(response.message)
                        .setPositiveButton("ok") { _, _ -> run { } }
                        .show()
                } else {
                    if (!mainState.isFixed) {
                        scope.launch {
                            if (isClosed.value) {
                                mainState.stopwatchOnRoad.stop()
                                mainState.stopwatchOnParking.start()
                            } else {
                                mainState.stopwatchOnParking.stop()
                                mainState.stopwatchOnRoad.start()
                            }
                        }
                    }
                }
            }
        }
        if (!mainState.isFixed) {
            Text(
                style = MaterialTheme.typography.displayMedium,
                text = "Закройте двери, чтобы перейти в режим ожидания"
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    style = MaterialTheme.typography.displayMedium,
                    text = "Аренда"
                )
                Text(
                    style = MaterialTheme.typography.displayMedium,
                    text = "${String.format("%.2f", mainState.lastSelectedRate!!.onRoadPrice * ((mainState.stopwatchOnRoad.minutes + mainState.stopwatchOnRoad.hours * 60) + (if (mainState.stopwatchOnRoad.minutes > 0 || mainState.stopwatchOnRoad.seconds > 0) 1 else 0)) + mainState.lastSelectedRate!!.parkingPrice * ((mainState.stopwatchOnParking.minutes + mainState.stopwatchOnParking.hours * 60) + (if (mainState.stopwatchOnParking.minutes > 0 || mainState.stopwatchOnParking.seconds > 0) 1 else 0)))} Р"
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
            AutoShareButton(
                isNegative = true,
                text = "Закончить аренду"
            ) {
                scope.launch {
                    val response = HttpClient.client.post(
                        "${HttpClient.url}/transport/cancel_rent?transportId=${mainState.lastSelectedRate!!.transportId}&rateId=${mainState.lastSelectedRate!!.id}"
                    ) {
                        headers["Authorization"] = "Bearer $token"
                    }.body<DefaultResponse>()

                    mainViewModel.updateUser()

                    if (response.status_code != 200) {
                        AlertDialog.Builder(context)
                            .setMessage(response.message)
                            .setPositiveButton("ok") { _, _ -> run { } }
                            .show()
                    } else {
                        mainState.stopwatchOnRoad.stop()
                        mainState.stopwatchOnParking.stop()
                        mainState.stopwatchChecking.stop()
                        mainViewModel.updatePage("resultPage")
                    }
                }
            }
        }
    }
}