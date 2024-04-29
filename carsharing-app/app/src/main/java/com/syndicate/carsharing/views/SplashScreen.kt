package com.syndicate.carsharing.views

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.material.ExperimentalMaterialApi
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.apache.commons.net.ntp.NTPUDPClient
import java.net.InetAddress
import java.time.OffsetDateTime

@kotlin.OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class,
    ExperimentalMaterialApi::class
)
@OptIn(UnstableApi::class)
@Composable
fun SplashScreen(
    navigateToDestination: (String) -> Unit,
    userStore: UserStore,
    mainViewModel: MainViewModel
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit) {
        val user = mainViewModel.userStore.getUser().first()
        val token = mainViewModel.userStore.getToken().first()

        if (user.email != "") {
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
                val rentHours = mainViewModel.userStore.getRentHours().first()
                val lastSelectedRate = mainViewModel.userStore.getLastSelectedRate().first()

                mainViewModel.updateRenting(isRenting)
                mainViewModel.updateReserving(isReserving)
                mainViewModel.updateChecking(isChecking)
                mainViewModel.updateSelectedRate(lastSelectedRate)
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
                                            mainViewModel.uiState.value.modalBottomSheetState!!.hide()
                                        }
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
                                            mainViewModel.uiState.value.modalBottomSheetState!!.hide()
                                        }
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
                                    if (rentHours != 0) {
                                        val seconds = (rentHours * 3600) - time
                                        mainViewModel.uiState.value.timer.changeStartTime((seconds / 3600).toInt(), (seconds % 3600 / 60).toInt(), (seconds % 60).toInt())
                                        mainViewModel.uiState.value.timer.onTimerEnd = {
                                            mainViewModel.uiState.value.mainViewScope!!.launch {
                                                mainViewModel.uiState.value.modalBottomSheetState!!.hide()
                                            }
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
                                mainViewModel.viewModelScope.launch {
                                    if (rentHours != 0) {
                                        val seconds = (rentHours * 3600) - summaryTime
                                        mainViewModel.uiState.value.timer.changeStartTime((seconds / 3600).toInt(), (seconds % 3600 / 60).toInt(), (seconds % 60).toInt())
                                        mainViewModel.uiState.value.timer.onTimerEnd = {
                                            mainViewModel.uiState.value.mainViewScope!!.launch {
                                                mainViewModel.uiState.value.modalBottomSheetState!!.hide()
                                            }
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
                                mainViewModel.viewModelScope.launch {
                                    if (rentHours != 0) {
                                        val seconds = (rentHours * 3600) - summaryTime
                                        mainViewModel.uiState.value.timer.changeStartTime((seconds / 3600).toInt(), (seconds % 3600 / 60).toInt(), (seconds % 60).toInt())
                                        mainViewModel.uiState.value.timer.onTimerEnd = {
                                            mainViewModel.uiState.value.mainViewScope!!.launch {
                                                mainViewModel.uiState.value.modalBottomSheetState!!.hide()
                                            }
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