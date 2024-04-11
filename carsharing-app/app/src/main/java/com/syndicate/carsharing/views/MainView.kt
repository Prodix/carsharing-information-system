package com.syndicate.carsharing.views

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.syndicate.carsharing.BuildConfig
import com.syndicate.carsharing.R
import com.syndicate.carsharing.components.BalanceMenu
import com.syndicate.carsharing.components.BottomMenu
import com.syndicate.carsharing.components.BottomSheetWithPages
import com.syndicate.carsharing.components.CarContent
import com.syndicate.carsharing.components.CheckContent
import com.syndicate.carsharing.components.FilterCarsContent
import com.syndicate.carsharing.components.LeftMenu
import com.syndicate.carsharing.components.MainMenuContent
import com.syndicate.carsharing.components.ProfileContent
import com.syndicate.carsharing.components.RadarContent
import com.syndicate.carsharing.components.RadarFindingContent
import com.syndicate.carsharing.components.RateContent
import com.syndicate.carsharing.components.RentContent
import com.syndicate.carsharing.components.ReservationContent
import com.syndicate.carsharing.components.UserCursorButton
import com.syndicate.carsharing.models.MainModel
import com.syndicate.carsharing.data.Tag
import com.syndicate.carsharing.data.Timer
import com.syndicate.carsharing.modifiers.withShadow
import com.syndicate.carsharing.utility.Shadow
import com.syndicate.carsharing.viewmodels.MainViewModel
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.layers.GeoObjectTapListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CircleMapObject
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.PolylineMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.transport.TransportFactory
import com.yandex.mapkit.transport.masstransit.PedestrianRouter
import com.yandex.mapkit.transport.masstransit.Route
import com.yandex.mapkit.transport.masstransit.Session
import com.yandex.mapkit.transport.masstransit.Session.RouteListener
import com.yandex.mapkit.transport.masstransit.TimeOptions
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.reflect.typeOf
import kotlin.system.exitProcess

//TODO: Рисовать маршрут при нажатии на точку автомобиля


@SuppressLint("UnrememberedMutableInteractionSource")
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Main(
    navigation: NavHostController,
    mainViewModel: MainViewModel = viewModel()
) {
    lateinit var userPlacemark: PlacemarkMapObject
    val mainState by mainViewModel.uiState.collectAsState()
    val mapView by mainViewModel.mapView.collectAsState()
    val router by mainViewModel.pedestrianRouter.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val location = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmValueChange = {
            if (it == ModalBottomSheetValue.Hidden) {
                if (mainViewModel.circle.value != null)
                    mapView!!.mapWindow.map.mapObjects.remove(mainViewModel.circle.value!!)
                mainViewModel.updateCircle(null)
            }

            true
        },
        skipHalfExpanded = true
    )

    val listener: MapObjectTapListener = MapObjectTapListener { _, point: Point ->
        mainViewModel.updatePoints(1, RequestPoint(point, RequestPointType.WAYPOINT, null, null))

        if (mainViewModel.isRenting.value)
            mainViewModel.updatePage("reservationPage")
        else if (mainViewModel.isChecking.value)
            mainViewModel.updatePage("checkPage")
        else
            mainViewModel.updatePage("car")

        scope.launch {
            sheetState.show()
        }
        true
    }

    // TODO: Добавить проверку интернета и геолокации
    fun enableLocation() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val loc = location.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            mainViewModel.updateLocation(Point(loc?.latitude ?: 0.0, loc?.longitude ?: 0.0))

            val testPlacemark = mapView!!.mapWindow.map.mapObjects.addPlacemark()
            testPlacemark.setIcon(ImageProvider.fromResource(context, R.drawable.carpoint))
            testPlacemark.geometry = Point(37.3935, -122.0478)
            testPlacemark.addTapListener(listener)

            userPlacemark = mapView!!.mapWindow.map.mapObjects.addPlacemark()
            userPlacemark.setIcon(ImageProvider.fromResource(context, R.drawable.userpoint))
            userPlacemark.geometry = mainViewModel.currentLocation.value

            if (mainViewModel.currentLocation.value.latitude == 0.0 && mainViewModel.currentLocation.value.longitude == 0.0) {
                userPlacemark.isVisible = false
            }
            else {
                mapView!!.setNoninteractive(true)
                mapView!!.mapWindow.map.move(CameraPosition(mainViewModel.currentLocation.value, 13f, 0f, 0f), Animation(Animation.Type.SMOOTH, 0.5f))
                { mapView!!.setNoninteractive(false) }
            }

            location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f) {
                mainViewModel.updateLocation(Point(it.latitude, it.longitude))
                userPlacemark.geometry = mainViewModel.currentLocation.value
                mainViewModel.updatePoints(0, RequestPoint(userPlacemark.geometry, RequestPointType.WAYPOINT, null, null))
                if (mainViewModel.session.value != null && mainViewModel.isRenting.value) {
                    mainViewModel.updateSession(
                        router!!.requestRoutes(
                            mainViewModel.points.value,
                            mainViewModel.options,
                            mainViewModel.routeListener
                        )
                    )
                }

                if (!sheetState.isVisible)
                    mainViewModel.circle.value?.geometry = Circle(mainViewModel.currentLocation.value, 400f * mainViewModel.walkMinutes.value)

                if (!userPlacemark.isVisible) {
                    userPlacemark.isVisible = true
                    mapView!!.setNoninteractive(true)
                    mapView!!.mapWindow.map.move(CameraPosition(mainViewModel.currentLocation.value, 13f, 0f, 0f), Animation(Animation.Type.SMOOTH, 0.5f))
                    { mapView!!.setNoninteractive(false) }
                }
            }
        }
    }

    val locationPermissions =
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    val activityResultLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in locationPermissions && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                AlertDialog.Builder(context)
                    .setMessage("Вы должны выдать разрешения использования геолокации чтобы пользоваться приложением!")
                    .setPositiveButton("ok") { _, _ -> exitProcess(0) }
                    .show()
            } else {
                enableLocation()
            }
        }

    LaunchedEffect(key1 = context) {
        activityResultLauncher.launch(locationPermissions)
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(factory = {
            MapKitFactory.setApiKey(BuildConfig.MAPKIT_KEY)
            MapView(it).apply {
                mainViewModel.setMap(this)
                MapKitFactory.initialize(it)
                MapKitFactory.getInstance().onStart()
                this.onStart()
                mainViewModel.updateRouter(TransportFactory.getInstance().createPedestrianRouter())
            }

        }, modifier = Modifier) {

        }

        BottomMenu(
            modifier = Modifier
                .padding(25.dp)
                .withShadow(
                    Shadow(
                        offsetX = 0.dp,
                        offsetY = 0.dp,
                        radius = 4.dp,
                        color = Color(0, 0, 0, 40)
                    ),
                    RoundedCornerShape(16.dp)
                )
                .background(Color.White, RoundedCornerShape(16.dp))
                .align(Alignment.BottomCenter),
            onClickRadar = {
                mainViewModel.updatePage("radarIntro")
                scope.launch {
                    mapView!!.mapWindow.map.move(CameraPosition(Point(mainViewModel.currentLocation.value.latitude, mainViewModel.currentLocation.value.longitude), 13f, 0f, 0f), Animation(Animation.Type.SMOOTH, 0.5f))
                    { }
                    mainViewModel.updateCircle(mapView!!.mapWindow.map.mapObjects.addCircle(Circle(mainViewModel.currentLocation.value, 400f * mainViewModel.walkMinutes.value)))
                    mainViewModel.circle.value?.fillColor = Color(0x4A92D992).toArgb()
                    mainViewModel.circle.value?.strokeColor = Color(0xFF99CC99).toArgb()
                    mainViewModel.circle.value?.strokeWidth = 1.5f
                    sheetState.show()
                }
            },
            onClickFilter = {
                mainViewModel.updatePage("filter")
                scope.launch {
                    sheetState.show()
                }
            }
        )

        LeftMenu(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart),
            sheetState = sheetState,
            onClick = {
                mainViewModel.updatePage("mainMenu")
                scope.launch {
                    sheetState.show()
                }
            }
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End
        ) {
            BalanceMenu(
                modifier = Modifier
                    .padding(16.dp),
                sheetState = sheetState
            )
            TimerBox(
                modifier = Modifier
                    .padding(end = 16.dp),
                mainViewModel = mainViewModel,
                sheetState = sheetState
            )
        }

        UserCursorButton(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterEnd),
            sheetState = sheetState
        ) {
            mapView!!.mapWindow.map.move(CameraPosition(Point(mainViewModel.currentLocation.value.latitude, mainViewModel.currentLocation.value.longitude), 13f, 0f, 0f), Animation(Animation.Type.SMOOTH, 0.5f))
            { }
        }
    }

    val bottomSheetPages: Map<String, @Composable () -> Unit> = mapOf(
        "radarIntro" to { RadarContent(
            mainViewModel = mainViewModel
        )},
        "radar" to { RadarFindingContent(
            mainViewModel = mainViewModel
        )},
        "filter" to { FilterCarsContent(
            mainViewModel = mainViewModel
        )},
        "mainMenu" to { MainMenuContent(
            mainViewModel = mainViewModel
        )},
        "profile" to { ProfileContent(
            mainViewModel = mainViewModel
        )},
        "car" to { CarContent(
            mainViewModel = mainViewModel
        )},
        "rateInfo" to { RateContent(
            mainViewModel = mainViewModel
        )},
        "reservationPage" to { ReservationContent(
            mainViewModel = mainViewModel
        )},
        "checkPage" to { CheckContent(
            mainViewModel = mainViewModel
        )},
        "rentPage" to { RentContent(
            mainViewModel = mainViewModel
        )}
    )

    BottomSheetWithPages(
        sheetState = sheetState,
        sheetComposableList = bottomSheetPages,
        mainViewModel = mainViewModel
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TimerBox(
    modifier: Modifier,
    mainViewModel: MainViewModel,
    sheetState: ModalBottomSheetState
) {
    val timer = mainViewModel.timer.collectAsState()
    Text(
        text = "${timer.value}",
        modifier = modifier
            .then(
                if (timer.value.isStarted && sheetState.targetValue != ModalBottomSheetValue.Expanded) {
                    Modifier.alpha(1f)
                } else {
                    Modifier.alpha(0f)
                }
            )
            .withShadow(
                Shadow(0.dp, 0.dp, 4.dp, Color(0x40000000)),
                RoundedCornerShape(10.dp)
            )
            .background(
                Color.White,
                RoundedCornerShape(10.dp)
            )
            .padding(
                horizontal = 10.dp,
                vertical = 5.dp
            )
    )
}