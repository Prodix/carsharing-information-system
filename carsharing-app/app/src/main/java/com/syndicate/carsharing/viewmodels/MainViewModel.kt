package com.syndicate.carsharing.viewmodels

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import com.syndicate.carsharing.R
import com.syndicate.carsharing.data.Stopwatch
import com.syndicate.carsharing.models.MainModel
import com.syndicate.carsharing.data.Tag
import com.syndicate.carsharing.data.Timer
import com.syndicate.carsharing.database.HttpClient
import com.syndicate.carsharing.database.models.Rate
import com.syndicate.carsharing.database.models.Transport
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CircleMapObject
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.PolylineMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.transport.masstransit.PedestrianRouter
import com.yandex.mapkit.transport.masstransit.Route
import com.yandex.mapkit.transport.masstransit.Session
import com.yandex.mapkit.transport.masstransit.TimeOptions
import com.yandex.runtime.image.ImageProvider
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.statement.request
import io.ktor.http.HttpMethod
import io.ktor.http.parameters
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainModel(null))
    private val _currentLocation = MutableStateFlow(Point())
    private val _page = MutableStateFlow("radarIntro")
    private val _rentHours = MutableStateFlow(2)
    private val _scrimColor = MutableStateFlow(Color.Transparent)
    private val _carType = MutableStateFlow(1f)
    private val _timer = MutableStateFlow(Timer())
    private val _stopwatchOnRoad = MutableStateFlow(Stopwatch())
    private val _stopwatchChecking = MutableStateFlow(Stopwatch())
    private val _stopwatchOnParking = MutableStateFlow(Stopwatch())
    private var _mapView: MutableStateFlow<MapView?> = MutableStateFlow(null)
    private var _pedestrianRouter: MutableStateFlow<PedestrianRouter?> = MutableStateFlow(null)
    private val _isChecking = MutableStateFlow(false)
    private val _isFixed = MutableStateFlow(false)
    private val _points = MutableStateFlow(listOf(RequestPoint(), RequestPoint()))
    private var _session: MutableStateFlow<Session?> = MutableStateFlow(null)
    private var _route: MutableStateFlow<PolylineMapObject?> = MutableStateFlow(null)
    private val _listTags = MutableStateFlow(listOf(
        Tag(
            "child1",
            false
        ),
        Tag(
            "child2",
            false
        ),
        Tag(
            "child3",
            false
        ),
    ))
    private val _circle: MutableStateFlow<CircleMapObject?> = MutableStateFlow(null)
    private val _mem = MutableStateFlow(1f)
    private val _isGesturesEnabled = MutableStateFlow(true)
    private val _walkMinutes = MutableStateFlow(1)
    private val _isReserving = MutableStateFlow(false)
    private val _lastSelectedRate = MutableStateFlow<Rate?>(null)
    private val _isRenting = MutableStateFlow(false)
    private val _transport = MutableStateFlow(listOf<Transport>())
    private val _lastSelectedPlacemark = MutableStateFlow<PlacemarkMapObject?>(null)
    private val _transportPlacemarkList = MutableStateFlow(listOf<PlacemarkMapObject>())

    val options = TimeOptions()

    val mapView = _mapView.asStateFlow()
    val transport = _transport.asStateFlow()
    val transportPlacemarkList = _transportPlacemarkList.asStateFlow()
    val pedestrianRouter = _pedestrianRouter.asStateFlow()
    val points = _points.asStateFlow()
    val session = _session.asStateFlow()
    val route = _route.asStateFlow()
    val uiState = _uiState.asStateFlow()
    val lastSelectedPlacemark = _lastSelectedPlacemark.asStateFlow()
    val isReserving = _isReserving.asStateFlow()
    val listTags = _listTags.asStateFlow()
    val currentLocation = _currentLocation.asStateFlow()
    val page = _page.asStateFlow()
    val carType = _carType.asStateFlow()
    val timer = _timer.asStateFlow()
    val stopwatchOnRoad = _stopwatchOnRoad.asStateFlow()
    val stopwatchChecking = _stopwatchChecking.asStateFlow()
    val stopwatchOnParking = _stopwatchOnParking.asStateFlow()
    val lastSelectedRate = _lastSelectedRate.asStateFlow()
    val mem = _mem.asStateFlow()
    val circle = _circle.asStateFlow()
    val isGesturesEnabled = _isGesturesEnabled.asStateFlow()
    val walkMinutes = _walkMinutes.asStateFlow()
    val scrimColor = _scrimColor.asStateFlow()
    val isChecking = _isChecking.asStateFlow()
    val isRenting = _isRenting.asStateFlow()
    val isFixed = _isFixed.asStateFlow()
    val rentHours = _rentHours.asStateFlow()

    val routeListener = object : Session.RouteListener {
        override fun onMasstransitRoutes(p0: MutableList<Route>) {
            if (p0.size > 0) {
                var validRoute: Polyline? = null

                validRoute = try {
                    p0[0].geometry
                } catch (e: Exception) {
                    p0[1].geometry
                }

                if (_route.value == null) {
                    _route.update {
                        mapView.value!!.mapWindow.map.mapObjects.addPolyline(validRoute!!).apply {
                            dashLength = 8f
                            dashOffset = 8f
                            gapLength = 8f
                            strokeWidth = 4f
                            setStrokeColor(Color(0xFF99CC99).toArgb())
                        }
                    }
                } else {
                    _route.value!!.geometry = validRoute!!
                }
            }
        }

        override fun onMasstransitRoutesError(p0: com.yandex.runtime.Error) {

        }
    }

    init {
        fillTags()
    }

    fun updateChecking(isChecking: Boolean) {
        _isChecking.update {
            isChecking
        }
    }

    fun updateSelectedRate(rate: Rate) {
        _lastSelectedRate.update {
            rate
        }
    }

    fun updateLastSelectedPlacemark(placemark: PlacemarkMapObject?) {
        _lastSelectedPlacemark.update {
            placemark
        }
    }

    fun updateRentHours(hours: Int) {
        _rentHours.update {
            hours
        }
    }

    fun updateIsFixed(isFixed: Boolean) {
        _isFixed.update {
            isFixed
        }
    }

    fun updatePoints(index: Int, point: RequestPoint) {

        val list = _points.value.toMutableList()
        list[index] = point

        _points.update {
            list.toList()
        }
    }

    fun fillTags() {
        //TODO: Написать заполнение тегов
    }

    fun updateSession(session: Session?) {
        if (session == null) {
            _route.value?.let { _mapView.value!!.mapWindow.map.mapObjects.remove(it) }
            _route.update {
                null
            }
        }
        _session.update {
            session
        }
    }

    fun updateReserving(isReserving: Boolean) {
        _isReserving.update {
            isReserving
        }
    }

    fun updateRenting(isRenting: Boolean) {
        _isRenting.update {
            isRenting
        }
    }

    fun updateCircle(circle: CircleMapObject?) {
        _circle.update {
            circle
        }
    }

    fun updateScrim(color: Color) {
        _scrimColor.update {
            color
        }
    }

    fun updateRouter(router: PedestrianRouter) {
        _pedestrianRouter.update {
            router
        }
    }

    fun updateCarType(carType: Float) {
        _carType.update {
            carType
        }
    }

    fun updateMem(mem: Float) {
        _mem.update {
            mem
        }
    }

    fun updateIsGesturesEnabled(isGesturesEnabled: Boolean) {
        _isGesturesEnabled.update {
            isGesturesEnabled
        }
    }

    fun updateWalkMinutes(walkMinutes: Int) {
        _walkMinutes.update {
            walkMinutes
        }
    }

    fun updateLocation(location: Point) {
        _currentLocation.update {
            location
        }
    }

    fun updatePage(page: String) {
        _page.update {
            page
        }
    }

    fun setMap(map: MapView) {
        _mapView.update {
            map
        }
    }

    suspend fun getTransport() {
        val response = HttpClient.client.request(
            "${HttpClient.url}/transport/get"
        ) {
            method = HttpMethod.Get
        }

        if (response.status.value == 200) {
            _transport.update {
                response.body()
            }
        }
    }

    fun updatePlacemarks(list: List<PlacemarkMapObject>) {
        _transportPlacemarkList.update {
            list
        }
    }

    fun updateTags(index: Int) {
        _listTags.update {

            val newList = it.toMutableList()

            newList[index] = Tag(
                newList[index].tag,
                !newList[index].isSelected
            )

            newList
        }
    }
}