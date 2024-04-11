package com.syndicate.carsharing.viewmodels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import com.syndicate.carsharing.models.MainModel
import com.syndicate.carsharing.data.Tag
import com.syndicate.carsharing.data.Timer
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CircleMapObject
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.PolylineMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.transport.masstransit.PedestrianRouter
import com.yandex.mapkit.transport.masstransit.Route
import com.yandex.mapkit.transport.masstransit.Session
import com.yandex.mapkit.transport.masstransit.TimeOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainModel(null))
    private val _currentLocation = MutableStateFlow(Point())
    private val _page = MutableStateFlow("radarIntro")
    private val _scrimColor = MutableStateFlow(Color.Transparent)
    private val _carType = MutableStateFlow(1f)
    private val _timer = MutableStateFlow(Timer())
    private var _mapView: MutableStateFlow<MapView?> = MutableStateFlow(null)
    private var _pedestrianRouter: MutableStateFlow<PedestrianRouter?> = MutableStateFlow(null)
    private val _isChecking = MutableStateFlow(false)
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
    private val _isRenting = MutableStateFlow(false)

    val options = TimeOptions()

    val mapView = _mapView.asStateFlow()
    val pedestrianRouter = _pedestrianRouter.asStateFlow()
    val points = _points.asStateFlow()
    val session = _session.asStateFlow()
    val route = _route.asStateFlow()
    val uiState = _uiState.asStateFlow()
    val isRenting = _isRenting.asStateFlow()
    val listTags = _listTags.asStateFlow()
    val currentLocation = _currentLocation.asStateFlow()
    val page = _page.asStateFlow()
    val carType = _carType.asStateFlow()
    val timer = _timer.asStateFlow()
    val mem = _mem.asStateFlow()
    val circle = _circle.asStateFlow()
    val isGesturesEnabled = _isGesturesEnabled.asStateFlow()
    val walkMinutes = _walkMinutes.asStateFlow()
    val scrimColor = _scrimColor.asStateFlow()
    val isChecking = _isChecking.asStateFlow()

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
        if (session == null)
            _route.value?.let { _mapView.value!!.mapWindow.map.mapObjects.remove(it) }
        _session.update {
            session
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