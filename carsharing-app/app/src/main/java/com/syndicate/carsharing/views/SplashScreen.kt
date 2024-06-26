package com.syndicate.carsharing.views

import android.app.AlertDialog
import android.content.Context
import android.location.LocationManager
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.syndicate.carsharing.R
import com.syndicate.carsharing.UserStore
import com.syndicate.carsharing.data.Tag
import com.syndicate.carsharing.database.HttpClient
import com.syndicate.carsharing.database.models.DefaultResponse
import com.syndicate.carsharing.database.models.Function
import com.syndicate.carsharing.database.models.Rate
import com.syndicate.carsharing.database.models.RentHistory
import com.syndicate.carsharing.database.models.TransportLog
import com.syndicate.carsharing.fromUnixMilli
import com.syndicate.carsharing.viewmodels.MainViewModel
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.apache.commons.net.ntp.NTPUDPClient
import java.net.InetAddress
import java.time.OffsetDateTime

@OptIn(UnstableApi::class)
@Composable
fun SplashScreen(
    navigateToDestination: (String) -> Unit,
    userStore: UserStore,
    mainViewModel: MainViewModel
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        val user = mainViewModel.userStore.getUser().first()

        val isConnected = try {
            val response = HttpClient.client.get("https://google.com")
            response.status.value == 200
        } catch (e: Exception) {
            false
        }

        if (!isConnected) {
            navigateToDestination("permission")
            return@LaunchedEffect
        }

        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            navigateToDestination("permission")
            return@LaunchedEffect
        }

        mainViewModel.fillTags()

        initialize(
            mainViewModel = mainViewModel,
            userStore = userStore,
            scope = scope,
            context = context
        )

        if (user.email == "") {
            navigateToDestination("signIn")
        } else if (!user.isEmailVerified) {
            HttpClient.client.post(
                "${HttpClient.url}/account/generate_code?email=${user.email}"
            )
            navigateToDestination("code/true/${user.email}")
        } else if (user.passportId == 0) {
            navigateToDestination("documentIntro/true/false")
        } else if (user.driverLicenseId == 0) {
            navigateToDestination("documentIntro/false/false")
        } else if (user.selfieId == 0) {
            navigateToDestination("documentIntro/false/true")
        } else {
            navigateToDestination("main")
        }
    }
    Box(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
    ) {
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.logo),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .height(100.dp)
                .width(100.dp)
        );
    }
}

@kotlin.OptIn(ExperimentalMaterialApi::class)
suspend fun initialize(
    mainViewModel: MainViewModel,
    userStore: UserStore,
    scope: CoroutineScope,
    context: Context
) {
    val user = mainViewModel.userStore.getUser().first()
    val token = mainViewModel.userStore.getToken().first()

    if (user.email != "") {
        mainViewModel.updateUser()
        val log = HttpClient.client.get(
            "${HttpClient.url}/account/history/get"
        ) {
            headers["Authorization"] = "Bearer $token"
        }.body<List<TransportLog>>()

        if (log.isNotEmpty()) {
            val lastAction = log.filter { x ->
                x.action == "RESERVE"               ||
                        x.action == "RENT"                  ||
                        x.action == "UNLOCK"                ||
                        x.action == "LOCK"                  ||
                        x.action == "CANCEL_RENT"           ||
                        x.action == "CANCEL_CHECK"          ||
                        x.action == "CANCEL_RESERVE"        ||
                        x.action == "CHECK"
            }.last()

            userStore.clearCarStates()

            when (lastAction.action) {
                "RESERVE" -> userStore.setReserving(true)
                "RENT" -> userStore.setRenting(true)
                "UNLOCK" -> userStore.setRenting(true)
                "LOCK" -> userStore.setRenting(true)
                "CHECK" -> userStore.setChecking(true)
                "CANCEL_RENT" -> userStore.setRenting(false)
                "CANCEL_CHECK" -> userStore.setChecking(false)
                "CANCEL_RESERVE" -> userStore.setReserving(false)
                else -> { }
            }

            val isReserving = mainViewModel.userStore.getReserving().first()
            val isRenting = mainViewModel.userStore.getRenting().first()
            val isChecking = mainViewModel.userStore.getChecking().first()
            var rentHours = mainViewModel.userStore.getRentHours().first()
            val lastSelectedRate = mainViewModel.userStore.getLastSelectedRate().first()


            if (!isReserving && !isRenting && !isChecking) {
                mainViewModel.updateSelectedRate(Rate())
            } else {
                mainViewModel.updateSelectedRate(lastSelectedRate)
                val lastAction = log.filter { x ->
                    x.action == "RENT" || x.action == "RESERVE" || x.action == "CHECK"
                }.maxByOrNull { x -> x.id }
                val transport = mainViewModel.getNewTransport()
                if (lastAction != null) {
                    val rate = transport.first {
                            x -> x.rates.any { y -> y.id == lastAction.rateId }
                    }.rates.first {
                        x -> x.id == lastAction.rateId
                    }
                    mainViewModel.updateSelectedRate(rate)
                }
                if (lastSelectedRate.onRoadPrice == lastSelectedRate.parkingPrice) {
                    mainViewModel.updateIsFixed(true)
                } else {
                    mainViewModel.updateIsFixed(false)
                }
            }

            mainViewModel.updateRenting(isRenting)
            mainViewModel.updateReserving(isReserving)
            mainViewModel.updateChecking(isChecking)
            mainViewModel.updateRentHours(rentHours)
            mainViewModel.updateIsClosed(lastAction.action == "LOCK")

            scope.launch {
                var ntpTime: OffsetDateTime = OffsetDateTime.MIN
                scope.launch(Dispatchers.IO) {
                    ntpTime = fromUnixMilli(NTPUDPClient().getTime(InetAddress.getByName("time.google.com")).returnTime)
                }
                while (ntpTime == OffsetDateTime.MIN) {
                    delay(10)
                }
                if (isReserving) {
                    if (lastAction.dateTime.dayOfYear == ntpTime.dayOfYear) {
                        val time = ntpTime.toEpochSecond() - lastAction.dateTime.toEpochSecond()
                        if (time / 60 < 20) {
                            mainViewModel.viewModelScope.launch(Dispatchers.IO) {
                                mainViewModel.uiState.value.timer.changeStartTime(0, 19 - (time / 60).toInt(), 60 - (time % 60).toInt())
                                mainViewModel.uiState.value.timer.onTimerEnd = {
                                    mainViewModel.uiState.value.mainViewScope!!.launch {
                                        mainViewModel.uiState.value.sheetState!!.hide()
                                    }
                                    mainViewModel.updateIsFixed(false)
                                    mainViewModel.updateReserving(false)
                                }
                                mainViewModel.uiState.value.timer.start()
                            }
                        }
                    }
                } else if (isChecking) {
                    if (lastAction.dateTime.dayOfYear == ntpTime.dayOfYear) {
                        mainViewModel.uiState.value.stopwatchChecking.clear()
                        val time = ntpTime.toEpochSecond() - lastAction.dateTime.toEpochSecond()
                        if (time / 60 < 5) {
                            mainViewModel.viewModelScope.launch(Dispatchers.IO) {
                                mainViewModel.uiState.value.timer.onTimerEnd = {
                                    mainViewModel.updateIsFixed(false)
                                    mainViewModel.viewModelScope.launch(Dispatchers.IO) {
                                        mainViewModel.uiState.value.stopwatchChecking.start()
                                    }
                                }
                                mainViewModel.uiState.value.timer.changeStartTime(0, 5 - (if (time < 60) 1 else (time / 60).toInt()), 60 - (time % 60).toInt())
                                mainViewModel.uiState.value.timer.start()
                            }
                        } else if (time / 60 < 35) {
                            mainViewModel.uiState.value.stopwatchChecking.minutes = ((time - 300) / 60).toInt()
                            mainViewModel.uiState.value.stopwatchChecking.seconds = ((time - 300) % 60).toInt()
                            mainViewModel.viewModelScope.launch(Dispatchers.IO) {
                                mainViewModel.uiState.value.stopwatchChecking.timeToStop = Triple(0,30,0)
                                mainViewModel.uiState.value.stopwatchChecking.action = {
                                    mainViewModel.uiState.value.mainViewScope!!.launch {
                                        mainViewModel.uiState.value.sheetState!!.hide()
                                    }
                                    mainViewModel.updateIsFixed(false)
                                    mainViewModel.uiState.value.stopwatchChecking.stop()
                                    mainViewModel.updateChecking(false)
                                }
                                mainViewModel.uiState.value.stopwatchChecking.start()
                            }
                        }
                    }
                } else if (isRenting) {
                    if (ntpTime.dayOfYear - lastAction.dateTime.dayOfYear < 2) {
                        val time = ntpTime.toEpochSecond() - lastAction.dateTime.toEpochSecond()
                        val reversed = log.reversed()
                        for (i in reversed.indices) {
                            if (reversed[i].action == "CHECK") {
                                mainViewModel.userStore.saveCheckingTime((reversed[i - 1].dateTime.toEpochSecond() - reversed[i].dateTime.toEpochSecond()).toInt())
                                break
                            }
                        }
                        if (lastAction.action == "RENT") {
                            mainViewModel.viewModelScope.launch {
                                if (mainViewModel.uiState.value.isFixed) {
                                    val response = HttpClient.client.get("${HttpClient.url}/account/rent_history/get") {
                                        headers["Authorization"] = "Bearer ${mainViewModel.userStore.getToken().first()}"
                                    }.body<List<RentHistory>>()
                                    val lastRent = response.maxByOrNull { x -> x.id }
                                    val rentHours = lastRent?.rentTime?.hour ?: 0
                                    val seconds = (rentHours * 3600) - time
                                    mainViewModel.uiState.value.timer.changeStartTime((seconds / 3600).toInt(), (seconds % 3600 / 60).toInt(), (seconds % 60).toInt())
                                    mainViewModel.uiState.value.timer.onTimerEnd = {
                                        mainViewModel.uiState.value.mainViewScope!!.launch {
                                            mainViewModel.uiState.value.sheetState!!.hide()
                                        }
                                        mainViewModel.updateIsFixed(false)
                                        mainViewModel.updateRenting(false)
                                        mainViewModel.updateRentHours(0)
                                    }
                                    mainViewModel.uiState.value.timer.start()
                                } else {
                                    mainViewModel.uiState.value.stopwatchOnRoad.minutes = (time / 60).toInt()
                                    mainViewModel.uiState.value.stopwatchOnRoad.seconds = (time % 60).toInt()
                                    mainViewModel.uiState.value.stopwatchOnRoad.start()
                                }
                            }
                        } else if (lastAction.action == "LOCK") {
                            var rentTime: Long = 0L
                            var summaryTime: Long = 0L
                            var parkingTime: Long = 0L
                            for (i in log.indices.reversed()) {
                                if (log[i].action == "RENT") {
                                    summaryTime = ntpTime.toEpochSecond() - log[i].dateTime.toEpochSecond()
                                    parkingTime = summaryTime
                                    var lock = 0L
                                    for (j in log.indices.reversed()) {
                                        if (log[j].action == "LOCK") {
                                            lock = log[j].dateTime.toEpochSecond()
                                        } else if (log[j].action == "UNLOCK") {
                                            lock -= log[j].dateTime.toEpochSecond()
                                            parkingTime -= lock
                                        } else if (log[j].action == "RENT") {
                                            lock -= log[j].dateTime.toEpochSecond()
                                            parkingTime -= lock
                                            rentTime = summaryTime - parkingTime
                                            break
                                        }
                                    }
                                    break
                                }
                            }
                            mainViewModel.viewModelScope.launch {
                                if (mainViewModel.uiState.value.isFixed) {
                                    val response = HttpClient.client.get("${HttpClient.url}/account/rent_history/get") {
                                        headers["Authorization"] = "Bearer ${mainViewModel.userStore.getToken().first()}"
                                    }.body<List<RentHistory>>()
                                    val lastRent = response.maxByOrNull { x -> x.id }
                                    val rentHours = lastRent?.rentTime?.hour ?: 0
                                    val seconds = (rentHours * 3600) - summaryTime
                                    mainViewModel.uiState.value.timer.changeStartTime((seconds / 3600).toInt(), (seconds % 3600 / 60).toInt(), (seconds % 60).toInt())
                                    mainViewModel.uiState.value.timer.onTimerEnd = {
                                        mainViewModel.uiState.value.mainViewScope!!.launch {
                                            mainViewModel.uiState.value.sheetState!!.hide()
                                        }
                                        mainViewModel.updateIsFixed(false)
                                        mainViewModel.updateRenting(false)
                                        mainViewModel.updateRentHours(0)
                                    }
                                    mainViewModel.uiState.value.timer.start()
                                } else {
                                    mainViewModel.uiState.value.stopwatchOnRoad.minutes = (rentTime / 60).toInt()
                                    mainViewModel.uiState.value.stopwatchOnRoad.seconds = (rentTime % 60).toInt()
                                    mainViewModel.uiState.value.stopwatchOnParking.minutes = (parkingTime / 60).toInt()
                                    mainViewModel.uiState.value.stopwatchOnParking.seconds = (parkingTime % 60).toInt()
                                    mainViewModel.uiState.value.stopwatchOnParking.start()
                                }
                            }
                        } else if (lastAction.action == "UNLOCK") {
                            var rentTime: Long = 0L
                            var summaryTime: Long = 0L
                            var parkingTime: Long = 0L
                            for (i in log.indices.reversed()) {
                                if (log[i].action == "RENT") {
                                    summaryTime = ntpTime.toEpochSecond() - log[i].dateTime.toEpochSecond()
                                    rentTime = summaryTime
                                    var unlock = 0L
                                    for (j in log.indices.reversed()) {
                                        if (log[j].action == "UNLOCK") {
                                            unlock = log[j].dateTime.toEpochSecond()
                                        } else if (log[j].action == "LOCK") {
                                            unlock -= log[j].dateTime.toEpochSecond()
                                            rentTime -= unlock
                                        } else if (log[j].action == "RENT") {
                                            parkingTime = summaryTime - rentTime
                                            break
                                        }
                                    }
                                    break
                                }
                            }
                            mainViewModel.viewModelScope.launch {
                                if (mainViewModel.uiState.value.isFixed) {
                                    val response = HttpClient.client.get("${HttpClient.url}/account/rent_history/get") {
                                        headers["Authorization"] = "Bearer ${mainViewModel.userStore.getToken().first()}"
                                    }.body<List<RentHistory>>()
                                    val lastRent = response.maxByOrNull { x -> x.id }
                                    val rentHours = lastRent?.rentTime?.hour ?: 0
                                    val seconds = (rentHours * 3600) - summaryTime
                                    mainViewModel.uiState.value.timer.changeStartTime((seconds / 3600).toInt(), (seconds % 3600 / 60).toInt(), (seconds % 60).toInt())
                                    mainViewModel.uiState.value.timer.onTimerEnd = {
                                        mainViewModel.uiState.value.mainViewScope!!.launch {
                                            mainViewModel.uiState.value.sheetState!!.hide()
                                        }
                                        mainViewModel.updateIsFixed(false)
                                        mainViewModel.updateRenting(false)
                                        mainViewModel.updateRentHours(0)
                                    }
                                    mainViewModel.uiState.value.timer.start()
                                } else {
                                    mainViewModel.viewModelScope.launch {
                                        mainViewModel.uiState.value.stopwatchOnRoad.minutes = (rentTime / 60).toInt()
                                        mainViewModel.uiState.value.stopwatchOnRoad.seconds = (rentTime % 60).toInt()
                                        mainViewModel.uiState.value.stopwatchOnParking.minutes = (parkingTime / 60).toInt()
                                        mainViewModel.uiState.value.stopwatchOnParking.seconds = (parkingTime % 60).toInt()
                                        mainViewModel.uiState.value.stopwatchOnRoad.start()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}