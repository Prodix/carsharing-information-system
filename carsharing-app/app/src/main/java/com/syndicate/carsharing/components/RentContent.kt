package com.syndicate.carsharing.components

import android.annotation.SuppressLint
import android.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.syndicate.carsharing.R
import com.syndicate.carsharing.database.HttpClient
import com.syndicate.carsharing.database.models.DefaultResponse
import com.syndicate.carsharing.database.models.Rate
import com.syndicate.carsharing.database.models.Transport
import com.syndicate.carsharing.modifiers.withShadow
import com.syndicate.carsharing.utility.Shadow
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
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ){
            Text(
                text = transportInfo.carName
            )
            Box(
                modifier = Modifier
                    .background(Color(0x80A3C2E0), RoundedCornerShape(5.dp))
            ) {
                Text(
                    modifier = Modifier
                        .padding(5.dp),
                    text = "${transportInfo.carNumber[0]} ${transportInfo.carNumber.subSequence(1, 4)} ${transportInfo.carNumber.subSequence(4, 6)} ${transportInfo.carNumber.subSequence(6, transportInfo.carNumber.length)}"
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
                    text = "${((transportInfo.gasLevel / (transportInfo.gasConsumption / 100.0))).toInt()} км • ${((transportInfo.gasLevel / transportInfo.tankCapacity.toDouble()) * 100).toInt()}%"
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
                    text = when (transportInfo.transportType) {
                        "BASE" -> "Базовый"
                        "COMFORT" -> "Комфорт"
                        "BUSINESS" -> "Бизнес"
                        else -> "Неизвестно"
                    }
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
                    text = transportInfo.insuranceType
                )
            }
        }
        SubcomposeAsyncImage(
            model = "${HttpClient.url}/transport/get/image?name=${transportInfo.carImagePath}",
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth(),
        ) {
            val state = painter.state
            if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                Loader()
            } else {
                SubcomposeAsyncImageContent()
            }
        }
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
        if (mainState.rentHours == 0) {
            Text(
                text = "Закройте двери, чтобы перейти в режим ожидания"
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(text = "Аренда ${String.format("%.2f", mainState.lastSelectedRate!!.onRoadPrice)} Р/мин")
                Text(text = "${String.format("%.2f", mainState.lastSelectedRate!!.onRoadPrice * (mainState.stopwatchOnRoad.minutes + (if (mainState.stopwatchOnRoad.minutes > 0 || mainState.stopwatchOnRoad.seconds > 0) 1 else 0)) + mainState.lastSelectedRate!!.parkingPrice * (mainState.stopwatchOnParking.minutes + (if (mainState.stopwatchOnParking.minutes > 0 || mainState.stopwatchOnParking.seconds > 0) 1 else 0)))} Р")
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(text = "Время в пути")
                Text(text = mainState.stopwatchOnRoad.toString())
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(text = "Время ожидания")
                Text(text = mainState.stopwatchOnParking.toString())
            }
            Button(
                onClick = {
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
                },
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Закончить аренду"
                )
            }
        }
    }
}