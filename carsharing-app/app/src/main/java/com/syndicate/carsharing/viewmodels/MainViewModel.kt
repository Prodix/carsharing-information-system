package com.syndicate.carsharing.viewmodels

import android.util.Log
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationListener
import com.syndicate.carsharing.UserStore
import com.syndicate.carsharing.models.MainModel
import com.syndicate.carsharing.data.Tag
import com.syndicate.carsharing.database.HttpClient
import com.syndicate.carsharing.database.models.Function
import com.syndicate.carsharing.database.models.Rate
import com.syndicate.carsharing.database.models.Transport
import com.syndicate.carsharing.database.models.TransportLog
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CircleMapObject
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.transport.masstransit.PedestrianRouter
import com.yandex.mapkit.transport.masstransit.Route
import com.yandex.mapkit.transport.masstransit.Session
import com.yandex.mapkit.transport.masstransit.TimeOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.request
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterialApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    val userStore: UserStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainModel())
    val uiState = _uiState.asStateFlow()
    val options = TimeOptions()

    val routeListener = object : Session.RouteListener {
        override fun onMasstransitRoutes(p0: MutableList<Route>) {
            if (p0.size > 0) {
                var validRoute: Polyline? = null

                validRoute = try {
                    p0[0].geometry
                } catch (e: Exception) {
                    p0[1].geometry
                }

                if (_uiState.value.route == null) {
                    _uiState.update {
                        it.copy(
                            route = it.mapView!!.mapWindow.map.mapObjects.addPolyline(validRoute!!).apply {
                                dashLength = 8f
                                dashOffset = 8f
                                gapLength = 8f
                                strokeWidth = 4f
                                setStrokeColor(Color(0xFF99CC99).toArgb())
                            }
                        )
                    }
                } else {
                    _uiState.value.route!!.geometry = validRoute!!
                }
            }
        }

        override fun onMasstransitRoutesError(p0: com.yandex.runtime.Error) {
        }
    }

    fun updateFiltered(isFiltered: Boolean) {
        _uiState.update {
            it.copy(
                isFiltered = isFiltered
            )
        }
    }

    fun updateChecking(isChecking: Boolean) {
        viewModelScope.launch {
            userStore.setChecking(isChecking)
        }
        _uiState.update {
            it.copy(
                isChecking = isChecking
            )
        }
    }

    fun updateIsClosed(isClosed: Boolean) {
        _uiState.update {
            it.copy(
                isClosed = isClosed
            )
        }
    }

    fun updateSelectedRate(rate: Rate) {
        viewModelScope.launch {
            userStore.setLastSelectedRate(rate)
        }
        _uiState.update {
            it.copy(
                lastSelectedRate = rate
            )
        }
    }

    fun updateLastSelectedPlacemark(placemark: PlacemarkMapObject?) {
        _uiState.update {
            it.copy(
                lastSelectedPlacemark = placemark
            )
        }
    }

    fun updateRentHours(hours: Int) {
        viewModelScope.launch {
            userStore.saveRentHours(hours)
        }
        _uiState.update {
            it.copy(
                rentHours = hours
            )
        }
    }

    fun updateSheetState(sheetState: ModalBottomSheetState) {
        _uiState.update {
            it.copy(
                sheetState = sheetState
            )
        }
    }

    fun updateScope(scope: CoroutineScope) {
        _uiState.update {
            it.copy(
                mainViewScope = scope
            )
        }
    }

    fun updateIsFixed(isFixed: Boolean) {
        if (!isFixed) {
            viewModelScope.launch {
                userStore.saveRentHours(2)
            }
        }
        _uiState.update {
            it.copy(
                isFixed = isFixed
            )
        }
    }

    fun fillTags() {
        viewModelScope.launch {
            val response = HttpClient.client.request(
                "${HttpClient.url}/transport/functions/get"
            ) {
                method = HttpMethod.Get
            }.body<List<Function>>().map { x -> Tag(x.functionData, false) }

            _uiState.update {
                it.copy(
                    listTags = response
                )
            }
        }
    }

    fun updateUser() {
        viewModelScope.launch {
            userStore.updateToken()
            val user = userStore.getUser().first()
            val token = userStore.getToken().first()
            _uiState.update {
                it.copy(
                    user = user,
                    token = token
                )
            }
        }
    }

    fun updateSession(session: Session?) {
        if (session == null) {
            _uiState.value.route?.let { _uiState.value.mapView!!.mapWindow.map.mapObjects.remove(it) }
            _uiState.update {
                it.copy(
                    route = null
                )
            }
        }
        _uiState.update {
            it.copy(
                session = session
            )
        }
    }

    fun updateReserving(isReserving: Boolean) {
        viewModelScope.launch {
            userStore.setReserving(isReserving)
        }
        _uiState.update {
            it.copy(
                isReserving = isReserving
            )
        }
    }

    fun updateRenting(isRenting: Boolean) {
        viewModelScope.launch {
            userStore.setRenting(isRenting)
        }
        _uiState.update {
            it.copy(
                isRenting = isRenting
            )
        }
    }

    fun updateRouter(router: PedestrianRouter) {
        _uiState.update {
            it.copy(
                pedestrianRouter = router
            )
        }
    }

    fun updateCarType(carType: Float) {
        _uiState.update {
            it.copy(
                carType = carType
            )
        }
    }

    fun updateLocation(location: Point) {
        _uiState.update {
            it.copy(
                currentLocation = location
            )
        }
    }

    fun updatePage(page: String) {
        _uiState.update {
            it.copy(
                page = page
            )
        }
    }

    fun setMap(map: MapView) {
        _uiState.update {
            it.copy(
                mapView = map
            )
        }
    }

    suspend fun getNewTransport(): List<Transport> {
        val response = HttpClient.client.request(
            "${HttpClient.url}/transport/get"
        ) {
            method = HttpMethod.Get
        }

        val filterType = when (_uiState.value.carType) {
            1f -> "BASE"
            2f -> "COMFORT"
            3f -> "BUSINESS"
            else -> ""
        }

        val selectedTags = _uiState.value.listTags
            .filter { x -> x.isSelected }
            .map { x -> x.tag }

        if (response.status.value == 200) {
            val token = userStore.getToken().first()
            val lastAction = HttpClient.client.get(
                "${HttpClient.url}/account/history/get"
            ) {
                headers["Authorization"] = "Bearer $token"
            }.body<List<TransportLog>>()
                .filter { x -> x.action != "UNLOCK" && x.action != "LOCK" && x.action != "BEEP" && x.action != "FLASH" }
                .maxByOrNull { x -> x.id }
            val visibleTransport = response.body<List<Transport>>().filter { x ->
                ((if (_uiState.value.isFiltered) x.transportType == filterType && (if (selectedTags.isNotEmpty()) x.functions.map { y -> y.functionData }.intersect(
                    selectedTags.toSet()
                ).isNotEmpty() else true) else true) && !x.isReserved && (!_uiState.value.isReserving && !_uiState.value.isRenting && !_uiState.value.isChecking)) ||
                        (if (lastAction?.action == "RENT" || lastAction?.action == "RESERVE" || lastAction?.action == "CHECK") x.id == lastAction?.transportId else false)
            }

            if (visibleTransport.size == 1) {
                val rate = visibleTransport[0].rates.firstOrNull {
                        x -> x.id == lastAction?.rateId
                }
                if (rate != null) {
                    updateSelectedRate(rate)
                    updateIsFixed(rate.onRoadPrice == rate.parkingPrice)
                }
            }

            return visibleTransport
        }

        return listOf()
    }

    fun updateTransport(transport: List<Transport>) {
        _uiState.update {
            it.copy(
                transport = transport
            )
        }
    }

    fun updatePlacemarks(list: List<PlacemarkMapObject>) {
        _uiState.update {
            it.copy(
                transportPlacemarkList = list
            )
        }
    }

    fun updateTags(index: Int) {

        val newList = _uiState.value.listTags.toMutableList()

        newList[index] = Tag(
            newList[index].tag,
            !newList[index].isSelected
        )

        _uiState.update {
            it.copy(
                listTags = newList
            )
        }
    }
}