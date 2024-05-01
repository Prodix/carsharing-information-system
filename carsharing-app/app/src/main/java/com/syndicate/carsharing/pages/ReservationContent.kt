package com.syndicate.carsharing.pages

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.syndicate.carsharing.R
import com.syndicate.carsharing.shared_components.AutoShareButton
import com.syndicate.carsharing.pages.components.CarPresenter
import com.syndicate.carsharing.pages.components.DoorSlider
import com.syndicate.carsharing.database.HttpClient
import com.syndicate.carsharing.database.models.DefaultResponse
import com.syndicate.carsharing.database.models.Rate
import com.syndicate.carsharing.database.models.Transport
import com.syndicate.carsharing.modifiers.withShadow
import com.syndicate.carsharing.utility.Shadow
import com.syndicate.carsharing.viewmodels.MainViewModel
import com.yandex.runtime.image.ImageProvider
import io.ktor.client.call.body
import io.ktor.client.request.post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class DragAnchors {
    Start,
    End
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
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
            style = MaterialTheme.typography.displayMedium,
            color = Color(0xFF9CA6B0),
            text = "Откройте двери, чтобы перейти к осмотру автомобиля"
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(30.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            ActionButton(
                modifier = Modifier
                    .weight(1f),
                image = R.drawable.flash,
                text = "Поморгать"
            ) {
                scope.launch {
                    val response = HttpClient.client
                        .post(
                            "${HttpClient.url}/transport/flash?transportId=${mainState.lastSelectedRate!!.transportId}&rateId=${mainState.lastSelectedRate!!.id}"
                        ) {
                            headers["Authorization"] = "Bearer $token"
                        }
                        .body<DefaultResponse>()

                    if (response.status_code != 200) {
                        AlertDialog
                            .Builder(context)
                            .setMessage(response.message)
                            .setPositiveButton("ok") { _, _ -> run { } }
                            .show()
                    } else {
                        Toast
                            .makeText(context, response.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            ActionButton(
                modifier = Modifier
                    .weight(1f),
                image = R.drawable.volume,
                text = "Посигналить"
            ) {
                scope.launch {
                    val response = HttpClient.client
                        .post(
                            "${HttpClient.url}/transport/beep?transportId=${mainState.lastSelectedRate!!.transportId}&rateId=${mainState.lastSelectedRate!!.id}"
                        ) {
                            headers["Authorization"] = "Bearer $token"
                        }
                        .body<DefaultResponse>()

                    if (response.status_code != 200) {
                        AlertDialog
                            .Builder(context)
                            .setMessage(response.message)
                            .setPositiveButton("ok") { _, _ -> run { } }
                            .show()
                    } else {
                        Toast
                            .makeText(context, response.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                style = MaterialTheme.typography.displayMedium,
                text = "Бронирование закончится через"
            )
            Text(
                style = MaterialTheme.typography.displayMedium,
                text = "${mainState.timer}"
            )
        }
        AutoShareButton(
            isNegative = true,
            text = "Отменить бронирование"
        ) {
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
                    mainViewModel.updateSelectedRate(Rate())
                    mainState.lastSelectedPlacemark!!.setIcon(ImageProvider.fromResource(context, R.drawable.carpoint))
                    mainState.timer.stop()
                    mainState.sheetState!!.hide()
                }
            }
        }
    }
}

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    @DrawableRes image: Int,
    text: String,
    onClick: () -> Unit
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
            .clip(RoundedCornerShape(15.dp))
            .clickable {
                onClick()
            }
            .padding(
                horizontal = 10.dp
            )
            .height(60.dp)
            .then(modifier),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            imageVector = ImageVector.vectorResource(image),
            contentDescription = null
        )
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            style = MaterialTheme.typography.displayMedium,
            text = text
        )
    }
}