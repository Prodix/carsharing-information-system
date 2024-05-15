package com.syndicate.carsharing.pages

import android.annotation.SuppressLint
import android.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.syndicate.carsharing.R
import com.syndicate.carsharing.shared_components.AutoShareButton
import com.syndicate.carsharing.database.HttpClient
import com.syndicate.carsharing.database.models.DefaultResponse
import com.syndicate.carsharing.database.models.Penalty
import com.syndicate.carsharing.database.models.Transport
import com.syndicate.carsharing.database.models.TransportLog
import com.syndicate.carsharing.fromUnixMilli
import com.syndicate.carsharing.viewmodels.MainViewModel
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.apache.commons.net.ntp.NTPUDPClient
import java.net.InetAddress


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun RateContent(
    mainViewModel: MainViewModel
) {
    val mainState by mainViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        mainViewModel.updateUser()
        mainViewModel.updateRentHours(2)
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                style = MaterialTheme.typography.titleMedium,
                text = mainState.lastSelectedRate!!.rateName
            )
        }
        if (String.format(
                "%.2f",
                mainState.lastSelectedRate!!.parkingPrice
            ) == String.format("%.2f", mainState.lastSelectedRate!!.onRoadPrice)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    style = MaterialTheme.typography.displaySmall,
                    text = "Стоимость в час"
                )
                Text(
                    style = MaterialTheme.typography.displaySmall,
                    text = "${
                        String.format(
                            "%.2f",
                            mainState.lastSelectedRate!!.parkingPrice * 60
                        )
                    } ₽/час"
                )
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    style = MaterialTheme.typography.displaySmall,
                    text = "Итого"
                )
                Text(
                    style = MaterialTheme.typography.displaySmall,
                    text = "${
                        String.format(
                            "%.2f",
                            mainState.lastSelectedRate!!.parkingPrice * mainState.rentHours * 60
                        )
                    } ₽"
                )
            }
            Column {
                Slider(
                    value = mainState.rentHours.toFloat(),
                    steps = 2,
                    valueRange = 2f..8f,
                    onValueChange = {
                        mainViewModel.updateRentHours(it.toInt())
                    },
                    colors = SliderDefaults.colors(
                        activeTickColor = Color(0xFF6699CC),
                        inactiveTickColor = Color.Transparent,
                        inactiveTrackColor = Color(0x806699CC),
                        activeTrackColor = Color(0xFF6699CC),
                        thumbColor = Color(0xFF34699D)
                    ),
                    thumb = {
                        SliderDefaults.Thumb(
                            interactionSource = MutableInteractionSource(),
                            thumbSize = DpSize(30.dp, 30.dp)
                        )
                    }
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "2 часа",
                        style = MaterialTheme.typography.displaySmall,
                        color = if (mainState.rentHours.toFloat() == 2f) Color.Black else Color(
                            0xFFC2C2C2
                        )
                    )
                    Text(
                        text = "4 часа",
                        style = MaterialTheme.typography.displaySmall,
                        color = if (mainState.rentHours.toFloat() == 4f) Color.Black else Color(
                            0xFFC2C2C2
                        )
                    )
                    Text(
                        text = "6 часов",
                        style = MaterialTheme.typography.displaySmall,
                        color = if (mainState.rentHours.toFloat() == 6f) Color.Black else Color(
                            0xFFC2C2C2
                        )
                    )
                    Text(
                        text = "8 часов",
                        style = MaterialTheme.typography.displaySmall,
                        color = if (mainState.rentHours.toFloat() == 8f) Color.Black else Color(
                            0xFFC2C2C2
                        )
                    )
                }
            }
        } else {

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.wheel),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color(0xFF6699CC))
                    )
                    Text(
                        style = MaterialTheme.typography.displaySmall,
                        text = "Стоимость в пути"
                    )
                }
                Text(
                    style = MaterialTheme.typography.displaySmall,
                    text = "${
                        String.format(
                            "%.2f",
                            mainState.lastSelectedRate!!.onRoadPrice
                        )
                    } ₽/мин"
                )
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.parking),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color(0xFF6699CC))
                    )
                    Text(
                        style = MaterialTheme.typography.displaySmall,
                        text = "Парковка"
                    )
                }
                Text(
                    style = MaterialTheme.typography.displaySmall,
                    text = "${
                        String.format(
                            "%.2f",
                            mainState.lastSelectedRate!!.parkingPrice
                        )
                    } ₽/мин"
                )
            }
        }
        AutoShareButton(
            text = "Забронировать"
        ) {
             scope.launch(Dispatchers.IO) outer@ {
                val token = mainViewModel.userStore.getToken().first()
                val user = mainViewModel.userStore.getUser().first()
                val currentTime = NTPUDPClient().getTime(InetAddress.getByName("time.google.com")).returnTime
                scope.launch inner@ {
                    val actions = HttpClient.client.get(
                        "${HttpClient.url}/account/history/get"
                    ) {
                        headers["Authorization"] = "Bearer $token"
                    }.body<List<TransportLog>>().filter { x ->
                        x.userId == user.id
                    }
                    if (actions.isNotEmpty()) {
                        val lastActionTime = actions.maxBy { x -> x.id }.dateTime
                        if (fromUnixMilli(currentTime).toEpochSecond() - lastActionTime.toEpochSecond() < 30 * 60) {
                            AlertDialog.Builder(context)
                                .setMessage("Арендовать автомобиль можно раз в 30 минут")
                                .setPositiveButton("ok") { _, _ -> run { } }
                                .show()
                            return@inner
                        }
                    }

                    if (!mainState.user.isVerified) {
                        AlertDialog.Builder(context)
                            .setMessage("Ваш аккаунт не верифицирован")
                            .setPositiveButton("ok") { _, _ -> run { } }
                            .show()
                        return@inner
                    }

                    val transportInfo = mainState.lastSelectedPlacemark?.userData as Transport

                    if (mainState.user.rating <= 30 && transportInfo.transportType != "base" ) {
                        AlertDialog.Builder(context)
                            .setMessage("Вам не хватает рейтинга для данного транспорта")
                            .setPositiveButton("ok") { _, _ -> run { } }
                            .show()
                        return@inner
                    }

                    val penalty = HttpClient.client.get(
                        "${HttpClient.url}/account/penalty/get?id=${user.id}"
                    ) {
                        headers["Authorization"] = "Bearer $token"
                    }.body<List<Penalty>>().filter { x -> !x.isPaid }

                    if (penalty.isNotEmpty()) {
                        AlertDialog.Builder(context)
                            .setMessage("У вас есть неоплаченные штрафы")
                            .setPositiveButton("ok") { _, _ -> run { } }
                            .show()
                        return@inner
                    }
                    if (mainState.user.balance < 1000.0) {
                        AlertDialog.Builder(context)
                            .setMessage("На вашем балансе должна быть минимум 1000 Рублей")
                            .setPositiveButton("ok") { _, _ -> run { } }
                            .show()
                        return@inner
                    }
                    if (String.format(
                            "%.2f",
                            mainState.lastSelectedRate!!.parkingPrice
                        ) == String.format("%.2f", mainState.lastSelectedRate!!.onRoadPrice)
                    ) {
                        if (mainState.user.balance < mainState.lastSelectedRate!!.parkingPrice * mainState.rentHours * 60) {
                            AlertDialog.Builder(context)
                                .setMessage("На вашем балансе недостаточно средств для аренды")
                                .setPositiveButton("ok") { _, _ -> run { } }
                                .show()
                            return@inner
                        }
                        mainViewModel.updateIsFixed(true)
                    } else {
                        mainViewModel.updateIsFixed(false)
                    }
                    mainViewModel.updateSession(
                        mainState.pedestrianRouter?.requestRoutes(
                            listOf(
                                RequestPoint(mainState.currentLocation, RequestPointType.WAYPOINT, null, null),
                                RequestPoint(mainState.lastSelectedPlacemark!!.geometry, RequestPointType.WAYPOINT, null, null),
                            ),
                            mainViewModel.options,
                            true,
                            mainViewModel.routeListener
                        )
                    )
                    val response = HttpClient.client.post(
                        "${HttpClient.url}/transport/reserve?transportId=${mainState.lastSelectedRate!!.transportId}&rateId=${mainState.lastSelectedRate!!.id}"
                    ) {
                        headers["Authorization"] = "Bearer ${mainState.token}"
                    }.body<DefaultResponse>()

                    if (response.status_code != 200) {
                        AlertDialog.Builder(context)
                            .setMessage(response.message)
                            .setPositiveButton("ok") { _, _ -> run { } }
                            .show()
                    } else {
                        mainViewModel.updateReserving(true)
                        mainViewModel.updatePage("reservationPage")
                    }
                }
            }
        }
    }
}