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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.syndicate.carsharing.BuildConfig
import com.syndicate.carsharing.R
import com.syndicate.carsharing.UserStore
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
import com.syndicate.carsharing.components.ResultContent
import com.syndicate.carsharing.components.TimerBox
import com.syndicate.carsharing.components.UserCursorButton
import com.syndicate.carsharing.database.models.Transport
import com.syndicate.carsharing.modifiers.withShadow
import com.syndicate.carsharing.utility.Shadow
import com.syndicate.carsharing.viewmodels.MainViewModel
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.transport.TransportFactory
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

//TODO: Рисовать маршрут при нажатии на точку автомобиля


@SuppressLint("UnrememberedMutableInteractionSource")
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Main(
    navigation: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel(),
    listener: MapObjectTapListener
) {
    lateinit var userPlacemark: PlacemarkMapObject
    val mainState by mainViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val location = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    mainViewModel.updateScope(scope)

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

            userPlacemark = mainState.mapView!!.mapWindow.map.mapObjects.addPlacemark()
            userPlacemark.setIcon(ImageProvider.fromResource(context, R.drawable.userpoint))
            userPlacemark.geometry = mainState.currentLocation

            if (mainState.currentLocation.latitude == 0.0 && mainState.currentLocation.longitude == 0.0) {
                userPlacemark.isVisible = false
            }
            else {
                mainState.mapView!!.setNoninteractive(true)
                mainState.mapView!!.mapWindow.map.move(CameraPosition(mainState.currentLocation, 13f, 0f, 0f), Animation(Animation.Type.SMOOTH, 0.5f))
                { mainState.mapView!!.setNoninteractive(false) }
            }

            location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f) {
                mainViewModel.updateLocation(Point(it.latitude, it.longitude))
                userPlacemark.geometry = mainState.currentLocation
                mainViewModel.updatePoints(0, RequestPoint(userPlacemark.geometry, RequestPointType.WAYPOINT, null, null))
                if (mainState.session != null && mainState.isReserving) {
                    mainViewModel.updateSession(
                        mainState.pedestrianRouter!!.requestRoutes(
                            mainState.points,
                            mainViewModel.options,
                            mainViewModel.routeListener
                        )
                    )
                }

                if (!mainState.modalBottomSheetState!!.isVisible)
                    mainState.circle?.geometry = Circle(mainState.currentLocation, 400f * mainState.walkMinutes)

                if (!userPlacemark.isVisible) {
                    userPlacemark.isVisible = true
                    mainState.mapView!!.setNoninteractive(true)
                    mainState.mapView!!.mapWindow.map.move(CameraPosition(mainState.currentLocation, 13f, 0f, 0f), Animation(Animation.Type.SMOOTH, 0.5f))
                    { mainState.mapView!!.setNoninteractive(false) }
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

    LaunchedEffect(key1 = Unit) {
        activityResultLauncher.launch(locationPermissions)
        mainViewModel.updatePlacemarks(listOf())
    }
    
    LaunchedEffect(key1 = mainState.mapView) {
        while (true) {
            val oldList = mainState.transport
            val oldListNumbers = mainState.transport.map { x -> x.id }
            mainViewModel.getTransport()
            val newList = mainState.transport
            val newListNumbers = mainState.transport.map { x -> x.id }

            if (mainState.transportPlacemarkList.isEmpty()) {
                val list = mutableListOf<PlacemarkMapObject>()
                for (i in mainState.transport) {
                    val placemarkMapObject = mainState.mapView!!.mapWindow.map.mapObjects.addPlacemark().apply {
                        geometry = Point(i.latitude, i.longitude)
                        setIcon(ImageProvider.fromResource(context, R.drawable.carpoint))
                        addTapListener(listener)
                        userData = i
                    }
                    list.add(placemarkMapObject)
                }
                mainViewModel.updatePlacemarks(list.toList())
            } else {
                val replacedList = mainState.transportPlacemarkList.toMutableList()

                if (oldListNumbers.size != newListNumbers.size) {
                    val doubleList = oldListNumbers.toMutableList() + newListNumbers
                    val difference = doubleList.filter { x -> doubleList.count { y -> y == x } == 1 }
                    if (oldListNumbers.size > newListNumbers.size) {
                        val needToRemove = mainState.transportPlacemarkList.filter { x ->
                            (x.userData as Transport).id in difference
                        }
                        for (i in needToRemove) {
                            mainState.mapView!!.mapWindow.map.mapObjects.remove(i)
                            replacedList.remove(i)
                        }
                    } else {
                        for (i in mainState.transport.filter { x -> x.id in difference }) {
                            val placemarkMapObject = mainState.mapView!!.mapWindow.map.mapObjects.addPlacemark().apply {
                                geometry = Point(i.latitude, i.longitude)
                                setIcon(ImageProvider.fromResource(context, R.drawable.carpoint))
                                addTapListener(listener)
                                userData = i
                            }
                            replacedList.add(placemarkMapObject)
                        }
                    }
                }

                for (i in newList) {
                    val placemark = replacedList.first {
                            x -> (x.userData as Transport).id == i.id
                    }
                    if (placemark.geometry !== Point(i.latitude, i.longitude)) {
                        placemark.geometry = Point(i.latitude, i.longitude)
                    }
                }
                mainViewModel.updatePlacemarks(replacedList.toList())
            }

            delay(3000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(factory = {
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
                    mainState.mapView!!.mapWindow.map.move(CameraPosition(Point(mainState.currentLocation.latitude, mainState.currentLocation.longitude), 13f, 0f, 0f), Animation(Animation.Type.SMOOTH, 0.5f))
                    { }
                    mainViewModel.updateCircle(mainState.mapView!!.mapWindow.map.mapObjects.addCircle(Circle(mainState.currentLocation, 400f * mainState.walkMinutes)))
                    scope.launch {
                        while (mainState.circle == null) {
                            delay(100)
                        }
                        mainState.circle?.fillColor = Color(0x4A92D992).toArgb()
                        mainState.circle?.strokeColor = Color(0xFF99CC99).toArgb()
                        mainState.circle?.strokeWidth = 1.5f
                    }
                    mainState.modalBottomSheetState!!.show()
                }
            },
            onClickFilter = {
                mainViewModel.updatePage("filter")
                scope.launch {
                    mainState.modalBottomSheetState!!.show()
                }
            }
        )

        LeftMenu(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart),
            sheetState = mainState.modalBottomSheetState!!,
            onClick = {
                mainViewModel.updatePage("mainMenu")
                scope.launch {
                    mainState.modalBottomSheetState!!.show()
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
                sheetState = mainState.modalBottomSheetState!!
            )
            TimerBox(
                modifier = Modifier
                    .padding(end = 16.dp),
                mainViewModel = mainViewModel
            )
        }

        UserCursorButton(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterEnd),
            sheetState = mainState.modalBottomSheetState!!
        ) {
            mainState.mapView!!.mapWindow.map.move(CameraPosition(Point(mainState.currentLocation.latitude, mainState.currentLocation.longitude), 13f, 0f, 0f), Animation(Animation.Type.SMOOTH, 0.5f))
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
            mainViewModel = mainViewModel,
            navigation = navigation
        )},
        "rentPage" to { RentContent(
            mainViewModel = mainViewModel
        )},
        "resultPage" to { ResultContent(
            mainViewModel = mainViewModel
        )}
    )

    BottomSheetWithPages(
        sheetComposableList = bottomSheetPages,
        mainViewModel = mainViewModel
    )
}
