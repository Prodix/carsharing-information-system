package com.syndicate.carsharing.components

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.widget.Toast
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
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
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.syndicate.carsharing.R
import com.syndicate.carsharing.data.Timer
import com.syndicate.carsharing.database.HttpClient
import com.syndicate.carsharing.database.models.DefaultResponse
import com.syndicate.carsharing.database.models.Transport
import com.syndicate.carsharing.modifiers.withShadow
import com.syndicate.carsharing.utility.Shadow
import com.syndicate.carsharing.viewmodels.MainViewModel
import com.yandex.runtime.image.ImageProvider
import io.ktor.client.call.body
import io.ktor.client.request.post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class DragAnchors {
    Start,
    End
}

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun ReservationContent(
    mainViewModel: MainViewModel
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val token by mainViewModel.userStore.getToken().collectAsState(initial = "")
    val mainState by mainViewModel.uiState.collectAsState()
    val transportInfo = mainState.lastSelectedPlacemark?.userData as Transport

    LaunchedEffect(key1 = context) {
        if (!mainState.timer.isStarted) {
            mainState.timer.changeStartTime(20,0)
            mainState.lastSelectedPlacemark!!.setIcon(ImageProvider.fromResource(context, R.drawable.routedcar))
            mainViewModel.viewModelScope.launch {
                mainState.timer.start()
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
                    "${HttpClient.url}/transport/check?transportId=${mainState.lastSelectedRate!!.transportId}&rateId=${mainState.lastSelectedRate!!.id}"
                ) {
                    headers["Authorization"] = "Bearer $token"
                }.body<DefaultResponse>()

                if (response.status_code != 200) {
                    AlertDialog.Builder(context)
                        .setMessage(response.message)
                        .setPositiveButton("ok") { _, _ -> run { } }
                        .show()
                } else {
                    mainState.timer.changeStartTime(0,5,0)
                    scope.launch(Dispatchers.IO) {
                        mainState.timer.start()
                    }
                    mainViewModel.updateReserving(false)
                    mainViewModel.updatePage("checkPage")
                }
            }
        }
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
                    .clickable {
                        scope.launch {
                            val response = HttpClient.client.post(
                                "${HttpClient.url}/transport/flash?transportId=${mainState.lastSelectedRate!!.transportId}&rateId=${mainState.lastSelectedRate!!.id}"
                            ) {
                                headers["Authorization"] = "Bearer $token"
                            }.body<DefaultResponse>()

                            if (response.status_code != 200) {
                                AlertDialog.Builder(context)
                                    .setMessage(response.message)
                                    .setPositiveButton("ok") { _, _ -> run { } }
                                    .show()
                            } else {
                                Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
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
                    .clickable {
                        scope.launch {
                            val response = HttpClient.client.post(
                                "${HttpClient.url}/transport/beep?transportId=${mainState.lastSelectedRate!!.transportId}&rateId=${mainState.lastSelectedRate!!.id}"
                            ) {
                                headers["Authorization"] = "Bearer $token"
                            }.body<DefaultResponse>()

                            if (response.status_code != 200) {
                                AlertDialog.Builder(context)
                                    .setMessage(response.message)
                                    .setPositiveButton("ok") { _, _ -> run { } }
                                    .show()
                            } else {
                                Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
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
                text = "${mainState.timer}"
            )
        }
        Button(
            onClick = {
                scope.launch {
                    val response = HttpClient.client.post(
                        "${HttpClient.url}/transport/cancel_reserve?transportId=${mainState.lastSelectedRate!!.transportId}&rateId=${mainState.lastSelectedRate!!.id}"
                    ) {
                        headers["Authorization"] = "Bearer $token"
                    }.body<DefaultResponse>()

                    if (response.status_code != 200) {
                        AlertDialog.Builder(context)
                            .setMessage(response.message)
                            .setPositiveButton("ok") { _, _ -> run { } }
                            .show()
                    } else {
                        mainViewModel.updateReserving(false)
                        mainViewModel.updateSession(null)
                        mainState.lastSelectedPlacemark!!.setIcon(ImageProvider.fromResource(context, R.drawable.carpoint))
                        mainState.timer.stop()
                        mainState.modalBottomSheetState!!.hide()
                    }
                }
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