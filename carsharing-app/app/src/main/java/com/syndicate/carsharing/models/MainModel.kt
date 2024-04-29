package com.syndicate.carsharing.models

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.ui.graphics.Color
import com.syndicate.carsharing.data.Stopwatch
import com.syndicate.carsharing.data.Tag
import com.syndicate.carsharing.data.Timer
import com.syndicate.carsharing.database.models.Rate
import com.syndicate.carsharing.database.models.Transport
import com.syndicate.carsharing.database.models.User
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CircleMapObject
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.PolylineMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.transport.masstransit.PedestrianRouter
import com.yandex.mapkit.transport.masstransit.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalMaterialApi::class)
data class MainModel (
    val tags: List<Tag>? = null,
    val currentLocation: Point = Point(),
    val page: String = "radarIntro",
    val rentHours: Int = 2,
    val isFiltered: Boolean = false,
    val scrimColor: Color = Color.Transparent,
    val carType: Float = 1f,
    val timer: Timer = Timer(),
    val stopwatchOnRoad: Stopwatch = Stopwatch(),
    val stopwatchChecking: Stopwatch = Stopwatch(),
    val stopwatchOnParking: Stopwatch = Stopwatch(),
    var mapView: MapView? = null,
    var modalBottomSheetState: ModalBottomSheetState? = null,
    var isClosed: Boolean = true,
    var pedestrianRouter: PedestrianRouter? = null,
    val isChecking: Boolean = false,
    val isFixed: Boolean = false,
    val points: List<RequestPoint> = listOf(RequestPoint(), RequestPoint()),
    var session: Session? = null,
    var route: PolylineMapObject? = null,
    var user: User = User(),
    var token: String = "",
    val listTags: List<Tag> = listOf(),
    val circle: CircleMapObject? = null,
    val mem: Float = 1f,
    val isGesturesEnabled: Boolean = true,
    val walkMinutes: Int = 1,
    val isReserving: Boolean = false,
    val lastSelectedRate: Rate? = null,
    val isRenting: Boolean = false,
    val transport: List<Transport> = listOf(),
    val lastSelectedPlacemark: PlacemarkMapObject? = null,
    val transportPlacemarkList: List<PlacemarkMapObject> = listOf<PlacemarkMapObject>(),
    val tapListener: MapObjectTapListener? = null,
    val mainViewScope: CoroutineScope? = null
)

