package com.syndicate.carsharing.views

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.util.SntpClient
import com.syndicate.carsharing.UserStore
import com.syndicate.carsharing.data.Stopwatch
import com.syndicate.carsharing.database.HttpClient
import com.syndicate.carsharing.database.models.Rate
import com.syndicate.carsharing.database.models.TransportLog
import com.syndicate.carsharing.database.models.User
import com.syndicate.carsharing.fromUnixMilli
import com.syndicate.carsharing.viewmodels.MainViewModel
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.apache.commons.net.ntp.NTPUDPClient
import java.net.InetAddress
import java.time.OffsetDateTime

@kotlin.OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
@OptIn(UnstableApi::class)
@Composable
fun SplashScreen(
    navigateToDestination: (String) -> Unit,
    userStore: UserStore,
    mainViewModel: MainViewModel
) {
    val user by userStore.getUser().collectAsState(initial = User())
    val token by userStore.getToken().collectAsState(initial = "")
    val isReserving by mainViewModel.userStore.getReserving().collectAsState(initial = false)
    val isRenting by mainViewModel.userStore.getRenting().collectAsState(initial = false)
    val isChecking by mainViewModel.userStore.getChecking().collectAsState(initial = false)
    val lastSelectedRate by mainViewModel.userStore.getLastSelectedRate().collectAsState(initial = Rate())
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit) {
        delay(1500)

        if (user.email != "") {
            val log = HttpClient.client.get(
                "${HttpClient.url}/account/history/get"
            ) {
                headers["Authorization"] = "Bearer $token"
            }.body<List<TransportLog>>()

            if (log.isNotEmpty()) {
                val lastAction = log.last()

                userStore.clearCarStates()

                when (lastAction.action) {
                    "RESERVE" -> userStore.setReserving(true)
                    "RENT" -> userStore.setRenting(true)
                    "UNLOCK" -> userStore.setRenting(true)
                    "LOCK" -> userStore.setRenting(true)
                    "CHECK" -> userStore.setChecking(true)
                    else -> { }
                }

                mainViewModel.updateRenting(isRenting)
                mainViewModel.updateReserving(isReserving)
                mainViewModel.updateChecking(isChecking)
                mainViewModel.updateSelectedRate(lastSelectedRate)

                scope.launch(Dispatchers.IO) {
                    val ntpTime = fromUnixMilli(NTPUDPClient().getTime(InetAddress.getByName("time.google.com")).returnTime)
                    if (isReserving) {
                        if (lastAction.dateTime.dayOfYear == ntpTime.dayOfYear) {
                            val time = ntpTime.toEpochSecond() - lastAction.dateTime.toEpochSecond()
                            if (time / 60 < 20) {
                                mainViewModel.viewModelScope.launch(Dispatchers.IO) {
                                    mainViewModel.uiState.value.timer.changeStartTime(0, 20 - (time / 60).toInt(), 60 - (time % 60).toInt())
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
                                        mainViewModel.viewModelScope.launch(Dispatchers.IO) {
                                            mainViewModel.uiState.value.stopwatchChecking.start()
                                        }
                                    }
                                    mainViewModel.uiState.value.timer.changeStartTime(0, 5 - (time / 60).toInt(), 60 - (time % 60).toInt())
                                    mainViewModel.uiState.value.timer.start()
                                }
                            } else if (time / 60 < 35) {
                                mainViewModel.uiState.value.stopwatchChecking.minutes = ((time - 300) / 60).toInt()
                                mainViewModel.uiState.value.stopwatchChecking.seconds = ((time - 300) % 60).toInt()
                                mainViewModel.viewModelScope.launch(Dispatchers.IO) {
                                    mainViewModel.uiState.value.stopwatchChecking.start()
                                }
                                //TODO: Добавить действие при достижении 30 минут
                            }
                        }
                    } else if (isRenting) {
                        if (ntpTime.dayOfYear - lastAction.dateTime.dayOfYear < 2) {
                            val time = ntpTime.toEpochSecond() - lastAction.dateTime.toEpochSecond()
                            if (lastAction.action == "RENT") {
                                mainViewModel.viewModelScope.launch(Dispatchers.IO) {
                                    mainViewModel.uiState.value.stopwatchOnRoad.minutes = (time / 60).toInt()
                                    mainViewModel.uiState.value.stopwatchOnRoad.seconds = (time % 60).toInt()
                                    mainViewModel.uiState.value.stopwatchOnRoad.start()
                                }
                            } else if (lastAction.action == "LOCK") {
                                var rentTime: Long = 0L
                                var summaryTime: Long = 0L
                                var parkingTime: Long = 0L
                                log.reversed().indices.forEach { i ->
                                    if (log[i].action == "RENT") {
                                        summaryTime = ntpTime.toEpochSecond() - log[i].dateTime.toEpochSecond()
                                        parkingTime = summaryTime
                                        var lock = 0L
                                        for (j in log.indices.reversed()) {
                                            if (log[j].action == "LOCK") {
                                                lock = log[j].dateTime.toEpochSecond()
                                            } else if (log[j].action == "UNLOCK" || log[j].action == "RENT") {
                                                lock -= log[j].dateTime.toEpochSecond()
                                                parkingTime -= lock
                                            }
                                        }
                                        rentTime = summaryTime - parkingTime
                                        return@forEach
                                    }
                                }
                                mainViewModel.viewModelScope.launch(Dispatchers.IO) {
                                    mainViewModel.uiState.value.stopwatchOnRoad.minutes = (rentTime / 60).toInt()
                                    mainViewModel.uiState.value.stopwatchOnRoad.seconds = (rentTime % 60).toInt()
                                    mainViewModel.uiState.value.stopwatchOnParking.minutes = (parkingTime / 60).toInt()
                                    mainViewModel.uiState.value.stopwatchOnParking.seconds = (parkingTime % 60).toInt()
                                    mainViewModel.uiState.value.stopwatchOnParking.start()
                                }
                            } else if (lastAction.action == "UNLOCK") {
                                var rentTime: Long = 0L
                                var summaryTime: Long = 0L
                                var parkingTime: Long = 0L
                                log.reversed().indices.forEach { i ->
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
                                            }
                                        }
                                        parkingTime = summaryTime - rentTime
                                        return@forEach
                                    }
                                }
                                mainViewModel.viewModelScope.launch(Dispatchers.IO) {
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

        if (user.email == "") {
            navigateToDestination("signIn")
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

    }
}