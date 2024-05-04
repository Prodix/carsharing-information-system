package com.syndicate.carsharing.views

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Environment
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.syndicate.carsharing.MainActivity
import com.syndicate.carsharing.R
import com.syndicate.carsharing.views.components.BalanceMenu
import com.syndicate.carsharing.views.components.BottomSheetWithPages
import com.syndicate.carsharing.pages.CarContent
import com.syndicate.carsharing.pages.CheckContent
import com.syndicate.carsharing.pages.FilterCarsContent
import com.syndicate.carsharing.views.components.LeftMenu
import com.syndicate.carsharing.pages.ProfileContent
import com.syndicate.carsharing.pages.RateContent
import com.syndicate.carsharing.pages.RentContent
import com.syndicate.carsharing.pages.ReservationContent
import com.syndicate.carsharing.pages.ResultContent
import com.syndicate.carsharing.views.components.TimerBox
import com.syndicate.carsharing.views.components.IconButton
import com.syndicate.carsharing.database.models.Transport
import com.syndicate.carsharing.viewmodels.MainViewModel
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
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
import java.io.File
import java.lang.Exception
import kotlin.system.exitProcess



@SuppressLint("UnrememberedMutableInteractionSource")
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
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

    suspend fun enableLocation() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val source = CancellationTokenSource()
            var loc = MainActivity.fusedLocationClient.getCurrentLocation(
                CurrentLocationRequest.Builder()
                    .setDurationMillis(1000)
                    .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                    .setMaxUpdateAgeMillis(Long.MAX_VALUE)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .build(),
                source.token
            )
            scope.launch(Dispatchers.IO) {
                while (true) {
                    if (loc.isComplete && loc.isSuccessful)  {
                        val result = loc.result
                        if (result == null) {
                            loc = MainActivity.fusedLocationClient.getCurrentLocation(
                                CurrentLocationRequest.Builder()
                                    .setDurationMillis(1000)
                                    .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                                    .setMaxUpdateAgeMillis(Long.MAX_VALUE)
                                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                                    .build(),
                                source.token
                            )
                            continue
                        }
                        scope.launch {
                            mainViewModel.updateLocation(Point(result.latitude, result.longitude))
                            userPlacemark = mainState.mapView!!.mapWindow.map.mapObjects.addPlacemark()
                            userPlacemark.setIcon(ImageProvider.fromResource(context, R.drawable.userpoint))
                            userPlacemark.geometry = Point(result.latitude, result.longitude)
                            mainState.mapView!!.setNoninteractive(true)
                            mainState.mapView!!.mapWindow.map.move(
                                CameraPosition(
                                    Point(result.latitude, result.longitude),
                                    13f,
                                    0f,
                                    0f
                                ), Animation(Animation.Type.SMOOTH, 0.5f)
                            )
                            { mainState.mapView!!.setNoninteractive(false) }
                            location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f) {
                                mainViewModel.updateLocation(Point(it.latitude, it.longitude))
                                if (mainState.currentLocation.latitude != 0.0 && mainState.currentLocation.longitude != 0.0) {
                                    userPlacemark.geometry = mainState.currentLocation
                                    if (mainState.session != null && mainState.isReserving) {
                                        mainViewModel.updateSession(
                                            mainState.pedestrianRouter?.requestRoutes(
                                                listOf(
                                                    RequestPoint(userPlacemark.geometry, RequestPointType.WAYPOINT, null, null),
                                                    RequestPoint(mainState.lastSelectedPlacemark!!.geometry, RequestPointType.WAYPOINT, null, null),
                                                ),
                                                mainViewModel.options,
                                                true,
                                                mainViewModel.routeListener
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        break
                    }
                    else if (loc.isComplete && !loc.isSuccessful) {
                        loc = MainActivity.fusedLocationClient.getCurrentLocation(
                            CurrentLocationRequest.Builder()
                                .setDurationMillis(1000)
                                .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                                .setMaxUpdateAgeMillis(Long.MAX_VALUE)
                                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                                .build(),
                            source.token
                        )
                    } else {
                        delay(10)
                    }
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
                scope.launch {
                    enableLocation()
                }
            }
        }

    LaunchedEffect(key1 = Unit) {
        launch(Dispatchers.IO) {
            delay(10000)
            val dir = File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}/AutoShare")
            if (dir.isDirectory) {
                dir.deleteRecursively()
            }
        }
        activityResultLauncher.launch(locationPermissions)
        mainViewModel.updatePlacemarks(listOf())
    }
    
    LaunchedEffect(key1 = Unit) {
        while (true) {
            val oldListNumbers = mainState.transport.map { x -> x.id }
            val newList = mainViewModel.getNewTransport()
            val newListNumbers = newList.map { x -> x.id }

            if (mainState.transportPlacemarkList.isEmpty()) {
                val list = mutableListOf<PlacemarkMapObject>()
                for (i in newList) {
                    val placemarkMapObject = mainState.mapView!!.mapWindow.map.mapObjects.addPlacemark().apply {
                        geometry = Point(i.latitude, i.longitude)
                        setIcon(ImageProvider.fromResource(context, R.drawable.carpoint))
                        addTapListener(listener)
                        userData = i
                    }
                    list.add(placemarkMapObject)
                }
                mainViewModel.updatePlacemarks(list.toList())
                mainViewModel.updateTransport(newList)
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
                        for (i in newList.filter { x -> x.id in difference }) {
                            val placemarkMapObject = mainState.mapView!!.mapWindow.map.mapObjects.addPlacemark().apply {
                                geometry = Point(i.latitude, i.longitude)
                                setIcon(ImageProvider.fromResource(context, R.drawable.carpoint))
                                addTapListener(listener)
                                userData = i
                            }
                            replacedList.add(placemarkMapObject)
                        }
                    }
                    mainViewModel.updateTransport(newList)
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

            delay(1000)
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

                this.mapWindow.map.setMapStyle("""[
    {
        "tags": "country",
        "elements": "geometry.fill",
        "stylers": [
            {
                "color": "#8599ad"
            },
            {
                "opacity": 0.8,
                "zoom": 0
            },
            {
                "opacity": 0.8,
                "zoom": 1
            },
            {
                "opacity": 0.8,
                "zoom": 2
            },
            {
                "opacity": 0.8,
                "zoom": 3
            },
            {
                "opacity": 0.8,
                "zoom": 4
            },
            {
                "opacity": 1,
                "zoom": 5
            },
            {
                "opacity": 1,
                "zoom": 6
            },
            {
                "opacity": 1,
                "zoom": 7
            },
            {
                "opacity": 1,
                "zoom": 8
            },
            {
                "opacity": 1,
                "zoom": 9
            },
            {
                "opacity": 1,
                "zoom": 10
            },
            {
                "opacity": 1,
                "zoom": 11
            },
            {
                "opacity": 1,
                "zoom": 12
            },
            {
                "opacity": 1,
                "zoom": 13
            },
            {
                "opacity": 1,
                "zoom": 14
            },
            {
                "opacity": 1,
                "zoom": 15
            },
            {
                "opacity": 1,
                "zoom": 16
            },
            {
                "opacity": 1,
                "zoom": 17
            },
            {
                "opacity": 1,
                "zoom": 18
            },
            {
                "opacity": 1,
                "zoom": 19
            },
            {
                "opacity": 1,
                "zoom": 20
            },
            {
                "opacity": 1,
                "zoom": 21
            }
        ]
    },
    {
        "tags": "country",
        "elements": "geometry.outline",
        "stylers": [
            {
                "color": "#b3cbe6"
            },
            {
                "opacity": 0.15,
                "zoom": 0
            },
            {
                "opacity": 0.15,
                "zoom": 1
            },
            {
                "opacity": 0.15,
                "zoom": 2
            },
            {
                "opacity": 0.15,
                "zoom": 3
            },
            {
                "opacity": 0.15,
                "zoom": 4
            },
            {
                "opacity": 0.15,
                "zoom": 5
            },
            {
                "opacity": 0.25,
                "zoom": 6
            },
            {
                "opacity": 0.5,
                "zoom": 7
            },
            {
                "opacity": 0.47,
                "zoom": 8
            },
            {
                "opacity": 0.44,
                "zoom": 9
            },
            {
                "opacity": 0.41,
                "zoom": 10
            },
            {
                "opacity": 0.38,
                "zoom": 11
            },
            {
                "opacity": 0.35,
                "zoom": 12
            },
            {
                "opacity": 0.33,
                "zoom": 13
            },
            {
                "opacity": 0.3,
                "zoom": 14
            },
            {
                "opacity": 0.28,
                "zoom": 15
            },
            {
                "opacity": 0.25,
                "zoom": 16
            },
            {
                "opacity": 0.25,
                "zoom": 17
            },
            {
                "opacity": 0.25,
                "zoom": 18
            },
            {
                "opacity": 0.25,
                "zoom": 19
            },
            {
                "opacity": 0.25,
                "zoom": 20
            },
            {
                "opacity": 0.25,
                "zoom": 21
            }
        ]
    },
    {
        "tags": "region",
        "elements": "geometry.fill",
        "stylers": [
            {
                "color": "#a3b3c2",
                "opacity": 0.5,
                "zoom": 0
            },
            {
                "color": "#a3b3c2",
                "opacity": 0.5,
                "zoom": 1
            },
            {
                "color": "#a3b3c2",
                "opacity": 0.5,
                "zoom": 2
            },
            {
                "color": "#a3b3c2",
                "opacity": 0.5,
                "zoom": 3
            },
            {
                "color": "#a3b3c2",
                "opacity": 0.5,
                "zoom": 4
            },
            {
                "color": "#a3b3c2",
                "opacity": 0.5,
                "zoom": 5
            },
            {
                "color": "#a3b3c2",
                "opacity": 1,
                "zoom": 6
            },
            {
                "color": "#a3b3c2",
                "opacity": 1,
                "zoom": 7
            },
            {
                "color": "#8599ad",
                "opacity": 1,
                "zoom": 8
            },
            {
                "color": "#8599ad",
                "opacity": 1,
                "zoom": 9
            },
            {
                "color": "#8599ad",
                "opacity": 1,
                "zoom": 10
            },
            {
                "color": "#8599ad",
                "opacity": 1,
                "zoom": 11
            },
            {
                "color": "#8599ad",
                "opacity": 1,
                "zoom": 12
            },
            {
                "color": "#8599ad",
                "opacity": 1,
                "zoom": 13
            },
            {
                "color": "#8599ad",
                "opacity": 1,
                "zoom": 14
            },
            {
                "color": "#8599ad",
                "opacity": 1,
                "zoom": 15
            },
            {
                "color": "#8599ad",
                "opacity": 1,
                "zoom": 16
            },
            {
                "color": "#8599ad",
                "opacity": 1,
                "zoom": 17
            },
            {
                "color": "#8599ad",
                "opacity": 1,
                "zoom": 18
            },
            {
                "color": "#8599ad",
                "opacity": 1,
                "zoom": 19
            },
            {
                "color": "#8599ad",
                "opacity": 1,
                "zoom": 20
            },
            {
                "color": "#8599ad",
                "opacity": 1,
                "zoom": 21
            }
        ]
    },
    {
        "tags": "region",
        "elements": "geometry.outline",
        "stylers": [
            {
                "color": "#b3cbe6"
            },
            {
                "opacity": 0.15,
                "zoom": 0
            },
            {
                "opacity": 0.15,
                "zoom": 1
            },
            {
                "opacity": 0.15,
                "zoom": 2
            },
            {
                "opacity": 0.15,
                "zoom": 3
            },
            {
                "opacity": 0.15,
                "zoom": 4
            },
            {
                "opacity": 0.15,
                "zoom": 5
            },
            {
                "opacity": 0.25,
                "zoom": 6
            },
            {
                "opacity": 0.5,
                "zoom": 7
            },
            {
                "opacity": 0.47,
                "zoom": 8
            },
            {
                "opacity": 0.44,
                "zoom": 9
            },
            {
                "opacity": 0.41,
                "zoom": 10
            },
            {
                "opacity": 0.38,
                "zoom": 11
            },
            {
                "opacity": 0.35,
                "zoom": 12
            },
            {
                "opacity": 0.33,
                "zoom": 13
            },
            {
                "opacity": 0.3,
                "zoom": 14
            },
            {
                "opacity": 0.28,
                "zoom": 15
            },
            {
                "opacity": 0.25,
                "zoom": 16
            },
            {
                "opacity": 0.25,
                "zoom": 17
            },
            {
                "opacity": 0.25,
                "zoom": 18
            },
            {
                "opacity": 0.25,
                "zoom": 19
            },
            {
                "opacity": 0.25,
                "zoom": 20
            },
            {
                "opacity": 0.25,
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "any": "admin",
            "none": [
                "country",
                "region",
                "locality",
                "district",
                "address"
            ]
        },
        "elements": "geometry.fill",
        "stylers": [
            {
                "color": "#8599ad"
            },
            {
                "opacity": 0.5,
                "zoom": 0
            },
            {
                "opacity": 0.5,
                "zoom": 1
            },
            {
                "opacity": 0.5,
                "zoom": 2
            },
            {
                "opacity": 0.5,
                "zoom": 3
            },
            {
                "opacity": 0.5,
                "zoom": 4
            },
            {
                "opacity": 0.5,
                "zoom": 5
            },
            {
                "opacity": 1,
                "zoom": 6
            },
            {
                "opacity": 1,
                "zoom": 7
            },
            {
                "opacity": 1,
                "zoom": 8
            },
            {
                "opacity": 1,
                "zoom": 9
            },
            {
                "opacity": 1,
                "zoom": 10
            },
            {
                "opacity": 1,
                "zoom": 11
            },
            {
                "opacity": 1,
                "zoom": 12
            },
            {
                "opacity": 1,
                "zoom": 13
            },
            {
                "opacity": 1,
                "zoom": 14
            },
            {
                "opacity": 1,
                "zoom": 15
            },
            {
                "opacity": 1,
                "zoom": 16
            },
            {
                "opacity": 1,
                "zoom": 17
            },
            {
                "opacity": 1,
                "zoom": 18
            },
            {
                "opacity": 1,
                "zoom": 19
            },
            {
                "opacity": 1,
                "zoom": 20
            },
            {
                "opacity": 1,
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "any": "admin",
            "none": [
                "country",
                "region",
                "locality",
                "district",
                "address"
            ]
        },
        "elements": "geometry.outline",
        "stylers": [
            {
                "color": "#b3cbe6"
            },
            {
                "opacity": 0.15,
                "zoom": 0
            },
            {
                "opacity": 0.15,
                "zoom": 1
            },
            {
                "opacity": 0.15,
                "zoom": 2
            },
            {
                "opacity": 0.15,
                "zoom": 3
            },
            {
                "opacity": 0.15,
                "zoom": 4
            },
            {
                "opacity": 0.15,
                "zoom": 5
            },
            {
                "opacity": 0.25,
                "zoom": 6
            },
            {
                "opacity": 0.5,
                "zoom": 7
            },
            {
                "opacity": 0.47,
                "zoom": 8
            },
            {
                "opacity": 0.44,
                "zoom": 9
            },
            {
                "opacity": 0.41,
                "zoom": 10
            },
            {
                "opacity": 0.38,
                "zoom": 11
            },
            {
                "opacity": 0.35,
                "zoom": 12
            },
            {
                "opacity": 0.33,
                "zoom": 13
            },
            {
                "opacity": 0.3,
                "zoom": 14
            },
            {
                "opacity": 0.28,
                "zoom": 15
            },
            {
                "opacity": 0.25,
                "zoom": 16
            },
            {
                "opacity": 0.25,
                "zoom": 17
            },
            {
                "opacity": 0.25,
                "zoom": 18
            },
            {
                "opacity": 0.25,
                "zoom": 19
            },
            {
                "opacity": 0.25,
                "zoom": 20
            },
            {
                "opacity": 0.25,
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "any": "landcover",
            "none": "vegetation"
        },
        "stylers": [
            {
                "hue": "#cedeee"
            }
        ]
    },
    {
        "tags": "vegetation",
        "elements": "geometry",
        "stylers": [
            {
                "color": "#a8c4e1",
                "opacity": 0.1,
                "zoom": 0
            },
            {
                "color": "#a8c4e1",
                "opacity": 0.1,
                "zoom": 1
            },
            {
                "color": "#a8c4e1",
                "opacity": 0.1,
                "zoom": 2
            },
            {
                "color": "#a8c4e1",
                "opacity": 0.1,
                "zoom": 3
            },
            {
                "color": "#a8c4e1",
                "opacity": 0.1,
                "zoom": 4
            },
            {
                "color": "#a8c4e1",
                "opacity": 0.1,
                "zoom": 5
            },
            {
                "color": "#a8c4e1",
                "opacity": 0.2,
                "zoom": 6
            },
            {
                "color": "#cedeee",
                "opacity": 0.3,
                "zoom": 7
            },
            {
                "color": "#cedeee",
                "opacity": 0.4,
                "zoom": 8
            },
            {
                "color": "#cedeee",
                "opacity": 0.6,
                "zoom": 9
            },
            {
                "color": "#cedeee",
                "opacity": 0.8,
                "zoom": 10
            },
            {
                "color": "#cedeee",
                "opacity": 1,
                "zoom": 11
            },
            {
                "color": "#cedeee",
                "opacity": 1,
                "zoom": 12
            },
            {
                "color": "#cedeee",
                "opacity": 1,
                "zoom": 13
            },
            {
                "color": "#d5e3f0",
                "opacity": 1,
                "zoom": 14
            },
            {
                "color": "#dde8f3",
                "opacity": 1,
                "zoom": 15
            },
            {
                "color": "#dde8f3",
                "opacity": 1,
                "zoom": 16
            },
            {
                "color": "#dde8f3",
                "opacity": 1,
                "zoom": 17
            },
            {
                "color": "#dde8f3",
                "opacity": 1,
                "zoom": 18
            },
            {
                "color": "#dde8f3",
                "opacity": 1,
                "zoom": 19
            },
            {
                "color": "#dde8f3",
                "opacity": 1,
                "zoom": 20
            },
            {
                "color": "#dde8f3",
                "opacity": 1,
                "zoom": 21
            }
        ]
    },
    {
        "tags": "park",
        "elements": "geometry",
        "stylers": [
            {
                "color": "#cedeee",
                "opacity": 0.1,
                "zoom": 0
            },
            {
                "color": "#cedeee",
                "opacity": 0.1,
                "zoom": 1
            },
            {
                "color": "#cedeee",
                "opacity": 0.1,
                "zoom": 2
            },
            {
                "color": "#cedeee",
                "opacity": 0.1,
                "zoom": 3
            },
            {
                "color": "#cedeee",
                "opacity": 0.1,
                "zoom": 4
            },
            {
                "color": "#cedeee",
                "opacity": 0.1,
                "zoom": 5
            },
            {
                "color": "#cedeee",
                "opacity": 0.2,
                "zoom": 6
            },
            {
                "color": "#cedeee",
                "opacity": 0.3,
                "zoom": 7
            },
            {
                "color": "#cedeee",
                "opacity": 0.4,
                "zoom": 8
            },
            {
                "color": "#cedeee",
                "opacity": 0.6,
                "zoom": 9
            },
            {
                "color": "#cedeee",
                "opacity": 0.8,
                "zoom": 10
            },
            {
                "color": "#cedeee",
                "opacity": 1,
                "zoom": 11
            },
            {
                "color": "#cedeee",
                "opacity": 1,
                "zoom": 12
            },
            {
                "color": "#cedeee",
                "opacity": 1,
                "zoom": 13
            },
            {
                "color": "#d5e3f0",
                "opacity": 1,
                "zoom": 14
            },
            {
                "color": "#dde8f3",
                "opacity": 1,
                "zoom": 15
            },
            {
                "color": "#dde8f3",
                "opacity": 0.9,
                "zoom": 16
            },
            {
                "color": "#dde8f3",
                "opacity": 0.8,
                "zoom": 17
            },
            {
                "color": "#dde8f3",
                "opacity": 0.7,
                "zoom": 18
            },
            {
                "color": "#dde8f3",
                "opacity": 0.7,
                "zoom": 19
            },
            {
                "color": "#dde8f3",
                "opacity": 0.7,
                "zoom": 20
            },
            {
                "color": "#dde8f3",
                "opacity": 0.7,
                "zoom": 21
            }
        ]
    },
    {
        "tags": "national_park",
        "elements": "geometry",
        "stylers": [
            {
                "color": "#cedeee",
                "opacity": 0.1,
                "zoom": 0
            },
            {
                "color": "#cedeee",
                "opacity": 0.1,
                "zoom": 1
            },
            {
                "color": "#cedeee",
                "opacity": 0.1,
                "zoom": 2
            },
            {
                "color": "#cedeee",
                "opacity": 0.1,
                "zoom": 3
            },
            {
                "color": "#cedeee",
                "opacity": 0.1,
                "zoom": 4
            },
            {
                "color": "#cedeee",
                "opacity": 0.1,
                "zoom": 5
            },
            {
                "color": "#cedeee",
                "opacity": 0.2,
                "zoom": 6
            },
            {
                "color": "#cedeee",
                "opacity": 0.3,
                "zoom": 7
            },
            {
                "color": "#cedeee",
                "opacity": 0.4,
                "zoom": 8
            },
            {
                "color": "#cedeee",
                "opacity": 0.6,
                "zoom": 9
            },
            {
                "color": "#cedeee",
                "opacity": 0.8,
                "zoom": 10
            },
            {
                "color": "#cedeee",
                "opacity": 1,
                "zoom": 11
            },
            {
                "color": "#cedeee",
                "opacity": 1,
                "zoom": 12
            },
            {
                "color": "#cedeee",
                "opacity": 1,
                "zoom": 13
            },
            {
                "color": "#d5e3f0",
                "opacity": 1,
                "zoom": 14
            },
            {
                "color": "#dde8f3",
                "opacity": 1,
                "zoom": 15
            },
            {
                "color": "#dde8f3",
                "opacity": 0.7,
                "zoom": 16
            },
            {
                "color": "#dde8f3",
                "opacity": 0.7,
                "zoom": 17
            },
            {
                "color": "#dde8f3",
                "opacity": 0.7,
                "zoom": 18
            },
            {
                "color": "#dde8f3",
                "opacity": 0.7,
                "zoom": 19
            },
            {
                "color": "#dde8f3",
                "opacity": 0.7,
                "zoom": 20
            },
            {
                "color": "#dde8f3",
                "opacity": 0.7,
                "zoom": 21
            }
        ]
    },
    {
        "tags": "cemetery",
        "elements": "geometry",
        "stylers": [
            {
                "color": "#cedeee",
                "zoom": 0
            },
            {
                "color": "#cedeee",
                "zoom": 1
            },
            {
                "color": "#cedeee",
                "zoom": 2
            },
            {
                "color": "#cedeee",
                "zoom": 3
            },
            {
                "color": "#cedeee",
                "zoom": 4
            },
            {
                "color": "#cedeee",
                "zoom": 5
            },
            {
                "color": "#cedeee",
                "zoom": 6
            },
            {
                "color": "#cedeee",
                "zoom": 7
            },
            {
                "color": "#cedeee",
                "zoom": 8
            },
            {
                "color": "#cedeee",
                "zoom": 9
            },
            {
                "color": "#cedeee",
                "zoom": 10
            },
            {
                "color": "#cedeee",
                "zoom": 11
            },
            {
                "color": "#cedeee",
                "zoom": 12
            },
            {
                "color": "#cedeee",
                "zoom": 13
            },
            {
                "color": "#d5e3f0",
                "zoom": 14
            },
            {
                "color": "#dde8f3",
                "zoom": 15
            },
            {
                "color": "#dde8f3",
                "zoom": 16
            },
            {
                "color": "#dde8f3",
                "zoom": 17
            },
            {
                "color": "#dde8f3",
                "zoom": 18
            },
            {
                "color": "#dde8f3",
                "zoom": 19
            },
            {
                "color": "#dde8f3",
                "zoom": 20
            },
            {
                "color": "#dde8f3",
                "zoom": 21
            }
        ]
    },
    {
        "tags": "sports_ground",
        "elements": "geometry",
        "stylers": [
            {
                "color": "#bbd2e8",
                "opacity": 0,
                "zoom": 0
            },
            {
                "color": "#bbd2e8",
                "opacity": 0,
                "zoom": 1
            },
            {
                "color": "#bbd2e8",
                "opacity": 0,
                "zoom": 2
            },
            {
                "color": "#bbd2e8",
                "opacity": 0,
                "zoom": 3
            },
            {
                "color": "#bbd2e8",
                "opacity": 0,
                "zoom": 4
            },
            {
                "color": "#bbd2e8",
                "opacity": 0,
                "zoom": 5
            },
            {
                "color": "#bbd2e8",
                "opacity": 0,
                "zoom": 6
            },
            {
                "color": "#bbd2e8",
                "opacity": 0,
                "zoom": 7
            },
            {
                "color": "#bbd2e8",
                "opacity": 0,
                "zoom": 8
            },
            {
                "color": "#bbd2e8",
                "opacity": 0,
                "zoom": 9
            },
            {
                "color": "#bbd2e8",
                "opacity": 0,
                "zoom": 10
            },
            {
                "color": "#bbd2e8",
                "opacity": 0,
                "zoom": 11
            },
            {
                "color": "#bbd2e8",
                "opacity": 0,
                "zoom": 12
            },
            {
                "color": "#bbd2e8",
                "opacity": 0,
                "zoom": 13
            },
            {
                "color": "#c2d7ea",
                "opacity": 0,
                "zoom": 14
            },
            {
                "color": "#cadced",
                "opacity": 0.5,
                "zoom": 15
            },
            {
                "color": "#cbdded",
                "opacity": 1,
                "zoom": 16
            },
            {
                "color": "#ccdeee",
                "opacity": 1,
                "zoom": 17
            },
            {
                "color": "#cddeee",
                "opacity": 1,
                "zoom": 18
            },
            {
                "color": "#cfdfee",
                "opacity": 1,
                "zoom": 19
            },
            {
                "color": "#d0e0ef",
                "opacity": 1,
                "zoom": 20
            },
            {
                "color": "#d1e1ef",
                "opacity": 1,
                "zoom": 21
            }
        ]
    },
    {
        "tags": "terrain",
        "elements": "geometry",
        "stylers": [
            {
                "hue": "#e0ebf5"
            },
            {
                "opacity": 0.3,
                "zoom": 0
            },
            {
                "opacity": 0.3,
                "zoom": 1
            },
            {
                "opacity": 0.3,
                "zoom": 2
            },
            {
                "opacity": 0.3,
                "zoom": 3
            },
            {
                "opacity": 0.3,
                "zoom": 4
            },
            {
                "opacity": 0.35,
                "zoom": 5
            },
            {
                "opacity": 0.4,
                "zoom": 6
            },
            {
                "opacity": 0.6,
                "zoom": 7
            },
            {
                "opacity": 0.8,
                "zoom": 8
            },
            {
                "opacity": 0.9,
                "zoom": 9
            },
            {
                "opacity": 1,
                "zoom": 10
            },
            {
                "opacity": 1,
                "zoom": 11
            },
            {
                "opacity": 1,
                "zoom": 12
            },
            {
                "opacity": 1,
                "zoom": 13
            },
            {
                "opacity": 1,
                "zoom": 14
            },
            {
                "opacity": 1,
                "zoom": 15
            },
            {
                "opacity": 1,
                "zoom": 16
            },
            {
                "opacity": 1,
                "zoom": 17
            },
            {
                "opacity": 1,
                "zoom": 18
            },
            {
                "opacity": 1,
                "zoom": 19
            },
            {
                "opacity": 1,
                "zoom": 20
            },
            {
                "opacity": 1,
                "zoom": 21
            }
        ]
    },
    {
        "tags": "geographic_line",
        "elements": "geometry",
        "stylers": [
            {
                "color": "#4687c3"
            }
        ]
    },
    {
        "tags": "land",
        "elements": "geometry",
        "stylers": [
            {
                "color": "#e0ebf5",
                "zoom": 0
            },
            {
                "color": "#e0ebf5",
                "zoom": 1
            },
            {
                "color": "#e0ebf5",
                "zoom": 2
            },
            {
                "color": "#e0ebf5",
                "zoom": 3
            },
            {
                "color": "#e0ebf5",
                "zoom": 4
            },
            {
                "color": "#e4edf6",
                "zoom": 5
            },
            {
                "color": "#e8f0f7",
                "zoom": 6
            },
            {
                "color": "#ecf2f9",
                "zoom": 7
            },
            {
                "color": "#f0f5fa",
                "zoom": 8
            },
            {
                "color": "#f0f5fa",
                "zoom": 9
            },
            {
                "color": "#f0f5fa",
                "zoom": 10
            },
            {
                "color": "#f0f5fa",
                "zoom": 11
            },
            {
                "color": "#f0f5fa",
                "zoom": 12
            },
            {
                "color": "#f0f5fa",
                "zoom": 13
            },
            {
                "color": "#f3f7fb",
                "zoom": 14
            },
            {
                "color": "#f7fafc",
                "zoom": 15
            },
            {
                "color": "#f8fafc",
                "zoom": 16
            },
            {
                "color": "#f8fbfd",
                "zoom": 17
            },
            {
                "color": "#f9fbfd",
                "zoom": 18
            },
            {
                "color": "#fafbfd",
                "zoom": 19
            },
            {
                "color": "#fafcfe",
                "zoom": 20
            },
            {
                "color": "#fbfcfe",
                "zoom": 21
            }
        ]
    },
    {
        "tags": "residential",
        "elements": "geometry",
        "stylers": [
            {
                "color": "#e0ebf5",
                "opacity": 0.5,
                "zoom": 0
            },
            {
                "color": "#e0ebf5",
                "opacity": 0.5,
                "zoom": 1
            },
            {
                "color": "#e0ebf5",
                "opacity": 0.5,
                "zoom": 2
            },
            {
                "color": "#e0ebf5",
                "opacity": 0.5,
                "zoom": 3
            },
            {
                "color": "#e0ebf5",
                "opacity": 0.5,
                "zoom": 4
            },
            {
                "color": "#e0ebf5",
                "opacity": 0.5,
                "zoom": 5
            },
            {
                "color": "#e0ebf5",
                "opacity": 0.5,
                "zoom": 6
            },
            {
                "color": "#e0ebf5",
                "opacity": 0.5,
                "zoom": 7
            },
            {
                "color": "#e0ebf5",
                "opacity": 0.5,
                "zoom": 8
            },
            {
                "color": "#e0ebf5",
                "opacity": 0.5,
                "zoom": 9
            },
            {
                "color": "#e0ebf5",
                "opacity": 0.5,
                "zoom": 10
            },
            {
                "color": "#e0ebf5",
                "opacity": 0.5,
                "zoom": 11
            },
            {
                "color": "#e0ebf5",
                "opacity": 0.5,
                "zoom": 12
            },
            {
                "color": "#e0ebf5",
                "opacity": 1,
                "zoom": 13
            },
            {
                "color": "#e8f0f7",
                "opacity": 1,
                "zoom": 14
            },
            {
                "color": "#f0f5fa",
                "opacity": 1,
                "zoom": 15
            },
            {
                "color": "#f1f6fa",
                "opacity": 1,
                "zoom": 16
            },
            {
                "color": "#f2f7fb",
                "opacity": 1,
                "zoom": 17
            },
            {
                "color": "#f3f7fb",
                "opacity": 1,
                "zoom": 18
            },
            {
                "color": "#f5f8fc",
                "opacity": 1,
                "zoom": 19
            },
            {
                "color": "#f6f9fc",
                "opacity": 1,
                "zoom": 20
            },
            {
                "color": "#f7fafd",
                "opacity": 1,
                "zoom": 21
            }
        ]
    },
    {
        "tags": "locality",
        "elements": "geometry",
        "stylers": [
            {
                "color": "#e0ebf5",
                "zoom": 0
            },
            {
                "color": "#e0ebf5",
                "zoom": 1
            },
            {
                "color": "#e0ebf5",
                "zoom": 2
            },
            {
                "color": "#e0ebf5",
                "zoom": 3
            },
            {
                "color": "#e0ebf5",
                "zoom": 4
            },
            {
                "color": "#e0ebf5",
                "zoom": 5
            },
            {
                "color": "#e0ebf5",
                "zoom": 6
            },
            {
                "color": "#e0ebf5",
                "zoom": 7
            },
            {
                "color": "#e0ebf5",
                "zoom": 8
            },
            {
                "color": "#e0ebf5",
                "zoom": 9
            },
            {
                "color": "#e0ebf5",
                "zoom": 10
            },
            {
                "color": "#e0ebf5",
                "zoom": 11
            },
            {
                "color": "#e0ebf5",
                "zoom": 12
            },
            {
                "color": "#e0ebf5",
                "zoom": 13
            },
            {
                "color": "#e8f0f7",
                "zoom": 14
            },
            {
                "color": "#f0f5fa",
                "zoom": 15
            },
            {
                "color": "#f1f6fa",
                "zoom": 16
            },
            {
                "color": "#f2f7fb",
                "zoom": 17
            },
            {
                "color": "#f3f7fb",
                "zoom": 18
            },
            {
                "color": "#f5f8fc",
                "zoom": 19
            },
            {
                "color": "#f6f9fc",
                "zoom": 20
            },
            {
                "color": "#f7fafd",
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "any": "structure",
            "none": [
                "building",
                "fence"
            ]
        },
        "elements": "geometry",
        "stylers": [
            {
                "opacity": 0.9
            },
            {
                "color": "#e0ebf5",
                "zoom": 0
            },
            {
                "color": "#e0ebf5",
                "zoom": 1
            },
            {
                "color": "#e0ebf5",
                "zoom": 2
            },
            {
                "color": "#e0ebf5",
                "zoom": 3
            },
            {
                "color": "#e0ebf5",
                "zoom": 4
            },
            {
                "color": "#e0ebf5",
                "zoom": 5
            },
            {
                "color": "#e0ebf5",
                "zoom": 6
            },
            {
                "color": "#e0ebf5",
                "zoom": 7
            },
            {
                "color": "#e0ebf5",
                "zoom": 8
            },
            {
                "color": "#e0ebf5",
                "zoom": 9
            },
            {
                "color": "#e0ebf5",
                "zoom": 10
            },
            {
                "color": "#e0ebf5",
                "zoom": 11
            },
            {
                "color": "#e0ebf5",
                "zoom": 12
            },
            {
                "color": "#e0ebf5",
                "zoom": 13
            },
            {
                "color": "#e8f0f7",
                "zoom": 14
            },
            {
                "color": "#f0f5fa",
                "zoom": 15
            },
            {
                "color": "#f1f6fa",
                "zoom": 16
            },
            {
                "color": "#f2f7fb",
                "zoom": 17
            },
            {
                "color": "#f3f7fb",
                "zoom": 18
            },
            {
                "color": "#f5f8fc",
                "zoom": 19
            },
            {
                "color": "#f6f9fc",
                "zoom": 20
            },
            {
                "color": "#f7fafd",
                "zoom": 21
            }
        ]
    },
    {
        "tags": "building",
        "elements": "geometry.fill",
        "stylers": [
            {
                "color": "#d1e0f0"
            },
            {
                "opacity": 0.7,
                "zoom": 0
            },
            {
                "opacity": 0.7,
                "zoom": 1
            },
            {
                "opacity": 0.7,
                "zoom": 2
            },
            {
                "opacity": 0.7,
                "zoom": 3
            },
            {
                "opacity": 0.7,
                "zoom": 4
            },
            {
                "opacity": 0.7,
                "zoom": 5
            },
            {
                "opacity": 0.7,
                "zoom": 6
            },
            {
                "opacity": 0.7,
                "zoom": 7
            },
            {
                "opacity": 0.7,
                "zoom": 8
            },
            {
                "opacity": 0.7,
                "zoom": 9
            },
            {
                "opacity": 0.7,
                "zoom": 10
            },
            {
                "opacity": 0.7,
                "zoom": 11
            },
            {
                "opacity": 0.7,
                "zoom": 12
            },
            {
                "opacity": 0.7,
                "zoom": 13
            },
            {
                "opacity": 0.7,
                "zoom": 14
            },
            {
                "opacity": 0.7,
                "zoom": 15
            },
            {
                "opacity": 0.9,
                "zoom": 16
            },
            {
                "opacity": 0.6,
                "zoom": 17
            },
            {
                "opacity": 0.6,
                "zoom": 18
            },
            {
                "opacity": 0.6,
                "zoom": 19
            },
            {
                "opacity": 0.6,
                "zoom": 20
            },
            {
                "opacity": 0.6,
                "zoom": 21
            }
        ]
    },
    {
        "tags": "building",
        "elements": "geometry.outline",
        "stylers": [
            {
                "color": "#b2cce6"
            },
            {
                "opacity": 0.5,
                "zoom": 0
            },
            {
                "opacity": 0.5,
                "zoom": 1
            },
            {
                "opacity": 0.5,
                "zoom": 2
            },
            {
                "opacity": 0.5,
                "zoom": 3
            },
            {
                "opacity": 0.5,
                "zoom": 4
            },
            {
                "opacity": 0.5,
                "zoom": 5
            },
            {
                "opacity": 0.5,
                "zoom": 6
            },
            {
                "opacity": 0.5,
                "zoom": 7
            },
            {
                "opacity": 0.5,
                "zoom": 8
            },
            {
                "opacity": 0.5,
                "zoom": 9
            },
            {
                "opacity": 0.5,
                "zoom": 10
            },
            {
                "opacity": 0.5,
                "zoom": 11
            },
            {
                "opacity": 0.5,
                "zoom": 12
            },
            {
                "opacity": 0.5,
                "zoom": 13
            },
            {
                "opacity": 0.5,
                "zoom": 14
            },
            {
                "opacity": 0.5,
                "zoom": 15
            },
            {
                "opacity": 0.5,
                "zoom": 16
            },
            {
                "opacity": 1,
                "zoom": 17
            },
            {
                "opacity": 1,
                "zoom": 18
            },
            {
                "opacity": 1,
                "zoom": 19
            },
            {
                "opacity": 1,
                "zoom": 20
            },
            {
                "opacity": 1,
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "any": "urban_area",
            "none": [
                "residential",
                "industrial",
                "cemetery",
                "park",
                "medical",
                "sports_ground",
                "beach",
                "construction_site"
            ]
        },
        "elements": "geometry",
        "stylers": [
            {
                "color": "#d1e1f0",
                "opacity": 1,
                "zoom": 0
            },
            {
                "color": "#d1e1f0",
                "opacity": 1,
                "zoom": 1
            },
            {
                "color": "#d1e1f0",
                "opacity": 1,
                "zoom": 2
            },
            {
                "color": "#d1e1f0",
                "opacity": 1,
                "zoom": 3
            },
            {
                "color": "#d1e1f0",
                "opacity": 1,
                "zoom": 4
            },
            {
                "color": "#d1e1f0",
                "opacity": 1,
                "zoom": 5
            },
            {
                "color": "#d1e1f0",
                "opacity": 1,
                "zoom": 6
            },
            {
                "color": "#d1e1f0",
                "opacity": 1,
                "zoom": 7
            },
            {
                "color": "#d1e1f0",
                "opacity": 1,
                "zoom": 8
            },
            {
                "color": "#d1e1f0",
                "opacity": 1,
                "zoom": 9
            },
            {
                "color": "#d1e1f0",
                "opacity": 1,
                "zoom": 10
            },
            {
                "color": "#d1e1f0",
                "opacity": 1,
                "zoom": 11
            },
            {
                "color": "#d1e1f0",
                "opacity": 1,
                "zoom": 12
            },
            {
                "color": "#d1e1f0",
                "opacity": 1,
                "zoom": 13
            },
            {
                "color": "#dae7f3",
                "opacity": 1,
                "zoom": 14
            },
            {
                "color": "#e4edf6",
                "opacity": 1,
                "zoom": 15
            },
            {
                "color": "#edf3f9",
                "opacity": 0.67,
                "zoom": 16
            },
            {
                "color": "#f7fafd",
                "opacity": 0.33,
                "zoom": 17
            },
            {
                "color": "#f7fafd",
                "opacity": 0,
                "zoom": 18
            },
            {
                "color": "#f7fafd",
                "opacity": 0,
                "zoom": 19
            },
            {
                "color": "#f7fafd",
                "opacity": 0,
                "zoom": 20
            },
            {
                "color": "#f7fafd",
                "opacity": 0,
                "zoom": 21
            }
        ]
    },
    {
        "tags": "poi",
        "elements": "label.icon",
        "stylers": [
            {
                "color": "#6699cc"
            },
            {
                "secondary-color": "#ffffff"
            },
            {
                "tertiary-color": "#ffffff"
            }
        ]
    },
    {
        "tags": "poi",
        "elements": "label.text.fill",
        "stylers": [
            {
                "color": "#264d73"
            }
        ]
    },
    {
        "tags": "poi",
        "elements": "label.text.outline",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "opacity": 0.5
            }
        ]
    },
    {
        "tags": "outdoor",
        "elements": "label.icon",
        "stylers": [
            {
                "color": "#6699cc"
            },
            {
                "secondary-color": "#ffffff"
            },
            {
                "tertiary-color": "#ffffff"
            }
        ]
    },
    {
        "tags": "outdoor",
        "elements": "label.text.fill",
        "stylers": [
            {
                "color": "#264d73"
            }
        ]
    },
    {
        "tags": "outdoor",
        "elements": "label.text.outline",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "opacity": 0.5
            }
        ]
    },
    {
        "tags": "park",
        "elements": "label.icon",
        "stylers": [
            {
                "color": "#6699cc"
            },
            {
                "secondary-color": "#ffffff"
            },
            {
                "tertiary-color": "#ffffff"
            }
        ]
    },
    {
        "tags": "park",
        "elements": "label.text.fill",
        "stylers": [
            {
                "color": "#264d73"
            }
        ]
    },
    {
        "tags": "park",
        "elements": "label.text.outline",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "opacity": 0.5
            }
        ]
    },
    {
        "tags": "cemetery",
        "elements": "label.icon",
        "stylers": [
            {
                "color": "#6699cc"
            },
            {
                "secondary-color": "#ffffff"
            },
            {
                "tertiary-color": "#ffffff"
            }
        ]
    },
    {
        "tags": "cemetery",
        "elements": "label.text.fill",
        "stylers": [
            {
                "color": "#264d73"
            }
        ]
    },
    {
        "tags": "cemetery",
        "elements": "label.text.outline",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "opacity": 0.5
            }
        ]
    },
    {
        "tags": "beach",
        "elements": "label.icon",
        "stylers": [
            {
                "color": "#6699cc"
            },
            {
                "secondary-color": "#ffffff"
            },
            {
                "tertiary-color": "#ffffff"
            }
        ]
    },
    {
        "tags": "beach",
        "elements": "label.text.fill",
        "stylers": [
            {
                "color": "#264d73"
            }
        ]
    },
    {
        "tags": "beach",
        "elements": "label.text.outline",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "opacity": 0.5
            }
        ]
    },
    {
        "tags": "medical",
        "elements": "label.icon",
        "stylers": [
            {
                "color": "#6699cc"
            },
            {
                "secondary-color": "#ffffff"
            },
            {
                "tertiary-color": "#ffffff"
            }
        ]
    },
    {
        "tags": "medical",
        "elements": "label.text.fill",
        "stylers": [
            {
                "color": "#264d73"
            }
        ]
    },
    {
        "tags": "medical",
        "elements": "label.text.outline",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "opacity": 0.5
            }
        ]
    },
    {
        "tags": "shopping",
        "elements": "label.icon",
        "stylers": [
            {
                "color": "#6699cc"
            },
            {
                "secondary-color": "#ffffff"
            },
            {
                "tertiary-color": "#ffffff"
            }
        ]
    },
    {
        "tags": "shopping",
        "elements": "label.text.fill",
        "stylers": [
            {
                "color": "#264d73"
            }
        ]
    },
    {
        "tags": "shopping",
        "elements": "label.text.outline",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "opacity": 0.5
            }
        ]
    },
    {
        "tags": "commercial_services",
        "elements": "label.icon",
        "stylers": [
            {
                "color": "#6699cc"
            },
            {
                "secondary-color": "#ffffff"
            },
            {
                "tertiary-color": "#ffffff"
            }
        ]
    },
    {
        "tags": "commercial_services",
        "elements": "label.text.fill",
        "stylers": [
            {
                "color": "#264d73"
            }
        ]
    },
    {
        "tags": "commercial_services",
        "elements": "label.text.outline",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "opacity": 0.5
            }
        ]
    },
    {
        "tags": "food_and_drink",
        "elements": "label.icon",
        "stylers": [
            {
                "color": "#6699cc"
            },
            {
                "secondary-color": "#ffffff"
            },
            {
                "tertiary-color": "#ffffff"
            }
        ]
    },
    {
        "tags": "food_and_drink",
        "elements": "label.text.fill",
        "stylers": [
            {
                "color": "#264d73"
            }
        ]
    },
    {
        "tags": "food_and_drink",
        "elements": "label.text.outline",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "opacity": 0.5
            }
        ]
    },
    {
        "tags": "road",
        "elements": "label.icon",
        "types": "point",
        "stylers": [
            {
                "color": "#6699cc"
            },
            {
                "secondary-color": "#ffffff"
            },
            {
                "tertiary-color": "#ffffff"
            }
        ]
    },
    {
        "tags": "road",
        "elements": "label.text.fill",
        "types": "point",
        "stylers": [
            {
                "color": "#ffffff"
            }
        ]
    },
    {
        "tags": "entrance",
        "elements": "label.icon",
        "stylers": [
            {
                "color": "#6699cc"
            },
            {
                "secondary-color": "#ffffff"
            },
            {
                "hue": "#6699cc"
            }
        ]
    },
    {
        "tags": "locality",
        "elements": "label.icon",
        "stylers": [
            {
                "color": "#6699cc"
            },
            {
                "secondary-color": "#ffffff"
            }
        ]
    },
    {
        "tags": "country",
        "elements": "label.text.fill",
        "stylers": [
            {
                "opacity": 0.8
            },
            {
                "color": "#3973ac"
            }
        ]
    },
    {
        "tags": "country",
        "elements": "label.text.outline",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "opacity": 0.5
            }
        ]
    },
    {
        "tags": "region",
        "elements": "label.text.fill",
        "stylers": [
            {
                "color": "#3973ac"
            },
            {
                "opacity": 0.8
            }
        ]
    },
    {
        "tags": "region",
        "elements": "label.text.outline",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "opacity": 0.5
            }
        ]
    },
    {
        "tags": "district",
        "elements": "label.text.fill",
        "stylers": [
            {
                "color": "#3973ac"
            },
            {
                "opacity": 0.8
            }
        ]
    },
    {
        "tags": "district",
        "elements": "label.text.outline",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "opacity": 0.5
            }
        ]
    },
    {
        "tags": {
            "any": "admin",
            "none": [
                "country",
                "region",
                "locality",
                "district",
                "address"
            ]
        },
        "elements": "label.text.fill",
        "stylers": [
            {
                "color": "#3973ac"
            }
        ]
    },
    {
        "tags": {
            "any": "admin",
            "none": [
                "country",
                "region",
                "locality",
                "district",
                "address"
            ]
        },
        "elements": "label.text.outline",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "opacity": 0.5
            }
        ]
    },
    {
        "tags": "locality",
        "elements": "label.text.fill",
        "stylers": [
            {
                "color": "#264d73",
                "zoom": 0
            },
            {
                "color": "#264d73",
                "zoom": 1
            },
            {
                "color": "#264d73",
                "zoom": 2
            },
            {
                "color": "#264d73",
                "zoom": 3
            },
            {
                "color": "#264d73",
                "zoom": 4
            },
            {
                "color": "#254b70",
                "zoom": 5
            },
            {
                "color": "#24496d",
                "zoom": 6
            },
            {
                "color": "#23476a",
                "zoom": 7
            },
            {
                "color": "#224466",
                "zoom": 8
            },
            {
                "color": "#214263",
                "zoom": 9
            },
            {
                "color": "#204060",
                "zoom": 10
            },
            {
                "color": "#204060",
                "zoom": 11
            },
            {
                "color": "#204060",
                "zoom": 12
            },
            {
                "color": "#204060",
                "zoom": 13
            },
            {
                "color": "#204060",
                "zoom": 14
            },
            {
                "color": "#204060",
                "zoom": 15
            },
            {
                "color": "#204060",
                "zoom": 16
            },
            {
                "color": "#204060",
                "zoom": 17
            },
            {
                "color": "#204060",
                "zoom": 18
            },
            {
                "color": "#204060",
                "zoom": 19
            },
            {
                "color": "#204060",
                "zoom": 20
            },
            {
                "color": "#204060",
                "zoom": 21
            }
        ]
    },
    {
        "tags": "locality",
        "elements": "label.text.outline",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "opacity": 0.5
            }
        ]
    },
    {
        "tags": "road",
        "elements": "label.text.fill",
        "types": "polyline",
        "stylers": [
            {
                "color": "#2d5986"
            }
        ]
    },
    {
        "tags": "road",
        "elements": "label.text.outline",
        "types": "polyline",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "opacity": 0.5
            }
        ]
    },
    {
        "tags": "road",
        "elements": "geometry.fill.pattern",
        "types": "polyline",
        "stylers": [
            {
                "scale": 1
            },
            {
                "color": "#538cc6"
            }
        ]
    },
    {
        "tags": "road",
        "elements": "label.text.fill",
        "types": "point",
        "stylers": [
            {
                "color": "#ffffff"
            }
        ]
    },
    {
        "tags": "structure",
        "elements": "label.text.fill",
        "stylers": [
            {
                "color": "#32689a"
            },
            {
                "opacity": 0.5
            }
        ]
    },
    {
        "tags": "structure",
        "elements": "label.text.outline",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "opacity": 0.5
            }
        ]
    },
    {
        "tags": "address",
        "elements": "label.text.fill",
        "stylers": [
            {
                "color": "#32689a"
            },
            {
                "opacity": 0.9,
                "zoom": 0
            },
            {
                "opacity": 0.9,
                "zoom": 1
            },
            {
                "opacity": 0.9,
                "zoom": 2
            },
            {
                "opacity": 0.9,
                "zoom": 3
            },
            {
                "opacity": 0.9,
                "zoom": 4
            },
            {
                "opacity": 0.9,
                "zoom": 5
            },
            {
                "opacity": 0.9,
                "zoom": 6
            },
            {
                "opacity": 0.9,
                "zoom": 7
            },
            {
                "opacity": 0.9,
                "zoom": 8
            },
            {
                "opacity": 0.9,
                "zoom": 9
            },
            {
                "opacity": 0.9,
                "zoom": 10
            },
            {
                "opacity": 0.9,
                "zoom": 11
            },
            {
                "opacity": 0.9,
                "zoom": 12
            },
            {
                "opacity": 0.9,
                "zoom": 13
            },
            {
                "opacity": 0.9,
                "zoom": 14
            },
            {
                "opacity": 0.9,
                "zoom": 15
            },
            {
                "opacity": 0.9,
                "zoom": 16
            },
            {
                "opacity": 1,
                "zoom": 17
            },
            {
                "opacity": 1,
                "zoom": 18
            },
            {
                "opacity": 1,
                "zoom": 19
            },
            {
                "opacity": 1,
                "zoom": 20
            },
            {
                "opacity": 1,
                "zoom": 21
            }
        ]
    },
    {
        "tags": "address",
        "elements": "label.text.outline",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "opacity": 0.5
            }
        ]
    },
    {
        "tags": "landscape",
        "elements": "label.text.fill",
        "stylers": [
            {
                "color": "#3973ac",
                "opacity": 1,
                "zoom": 0
            },
            {
                "color": "#3973ac",
                "opacity": 1,
                "zoom": 1
            },
            {
                "color": "#3973ac",
                "opacity": 1,
                "zoom": 2
            },
            {
                "color": "#3973ac",
                "opacity": 1,
                "zoom": 3
            },
            {
                "color": "#32689a",
                "opacity": 0.5,
                "zoom": 4
            },
            {
                "color": "#32689a",
                "opacity": 0.5,
                "zoom": 5
            },
            {
                "color": "#32689a",
                "opacity": 0.5,
                "zoom": 6
            },
            {
                "color": "#32689a",
                "opacity": 0.5,
                "zoom": 7
            },
            {
                "color": "#32689a",
                "opacity": 0.5,
                "zoom": 8
            },
            {
                "color": "#32689a",
                "opacity": 0.5,
                "zoom": 9
            },
            {
                "color": "#32689a",
                "opacity": 0.5,
                "zoom": 10
            },
            {
                "color": "#32689a",
                "opacity": 0.5,
                "zoom": 11
            },
            {
                "color": "#32689a",
                "opacity": 0.5,
                "zoom": 12
            },
            {
                "color": "#32689a",
                "opacity": 0.5,
                "zoom": 13
            },
            {
                "color": "#32689a",
                "opacity": 0.5,
                "zoom": 14
            },
            {
                "color": "#32689a",
                "opacity": 0.5,
                "zoom": 15
            },
            {
                "color": "#32689a",
                "opacity": 0.5,
                "zoom": 16
            },
            {
                "color": "#32689a",
                "opacity": 0.5,
                "zoom": 17
            },
            {
                "color": "#32689a",
                "opacity": 0.5,
                "zoom": 18
            },
            {
                "color": "#32689a",
                "opacity": 0.5,
                "zoom": 19
            },
            {
                "color": "#32689a",
                "opacity": 0.5,
                "zoom": 20
            },
            {
                "color": "#32689a",
                "opacity": 0.5,
                "zoom": 21
            }
        ]
    },
    {
        "tags": "landscape",
        "elements": "label.text.outline",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "opacity": 0.5,
                "zoom": 0
            },
            {
                "opacity": 0.5,
                "zoom": 1
            },
            {
                "opacity": 0.5,
                "zoom": 2
            },
            {
                "opacity": 0.5,
                "zoom": 3
            },
            {
                "opacity": 0,
                "zoom": 4
            },
            {
                "opacity": 0,
                "zoom": 5
            },
            {
                "opacity": 0,
                "zoom": 6
            },
            {
                "opacity": 0,
                "zoom": 7
            },
            {
                "opacity": 0,
                "zoom": 8
            },
            {
                "opacity": 0,
                "zoom": 9
            },
            {
                "opacity": 0,
                "zoom": 10
            },
            {
                "opacity": 0,
                "zoom": 11
            },
            {
                "opacity": 0,
                "zoom": 12
            },
            {
                "opacity": 0,
                "zoom": 13
            },
            {
                "opacity": 0,
                "zoom": 14
            },
            {
                "opacity": 0,
                "zoom": 15
            },
            {
                "opacity": 0,
                "zoom": 16
            },
            {
                "opacity": 0,
                "zoom": 17
            },
            {
                "opacity": 0,
                "zoom": 18
            },
            {
                "opacity": 0,
                "zoom": 19
            },
            {
                "opacity": 0,
                "zoom": 20
            },
            {
                "opacity": 0,
                "zoom": 21
            }
        ]
    },
    {
        "tags": "water",
        "elements": "label.text.fill",
        "stylers": [
            {
                "color": "#4080bf"
            },
            {
                "opacity": 0.8
            }
        ]
    },
    {
        "tags": "water",
        "elements": "label.text.outline",
        "types": "polyline",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "opacity": 0.2
            }
        ]
    },
    {
        "tags": {
            "any": "road_1",
            "none": "is_tunnel"
        },
        "elements": "geometry.fill",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "scale": 0,
                "zoom": 0
            },
            {
                "scale": 0,
                "zoom": 1
            },
            {
                "scale": 0,
                "zoom": 2
            },
            {
                "scale": 0,
                "zoom": 3
            },
            {
                "scale": 0,
                "zoom": 4
            },
            {
                "scale": 0,
                "zoom": 5
            },
            {
                "scale": 2.97,
                "zoom": 6
            },
            {
                "scale": 3.19,
                "zoom": 7
            },
            {
                "scale": 3.53,
                "zoom": 8
            },
            {
                "scale": 4,
                "zoom": 9
            },
            {
                "scale": 3.61,
                "zoom": 10
            },
            {
                "scale": 3.06,
                "zoom": 11
            },
            {
                "scale": 2.64,
                "zoom": 12
            },
            {
                "scale": 2.27,
                "zoom": 13
            },
            {
                "scale": 2.03,
                "zoom": 14
            },
            {
                "scale": 1.9,
                "zoom": 15
            },
            {
                "scale": 1.86,
                "zoom": 16
            },
            {
                "scale": 1.48,
                "zoom": 17
            },
            {
                "scale": 1.21,
                "zoom": 18
            },
            {
                "scale": 1.04,
                "zoom": 19
            },
            {
                "scale": 0.94,
                "zoom": 20
            },
            {
                "scale": 0.9,
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "any": "road_1"
        },
        "elements": "geometry.outline",
        "stylers": [
            {
                "color": "#00000000",
                "scale": 1.4,
                "zoom": 0
            },
            {
                "color": "#00000000",
                "scale": 1.4,
                "zoom": 1
            },
            {
                "color": "#00000000",
                "scale": 1.4,
                "zoom": 2
            },
            {
                "color": "#00000000",
                "scale": 1.4,
                "zoom": 3
            },
            {
                "color": "#00000000",
                "scale": 1.4,
                "zoom": 4
            },
            {
                "color": "#00000000",
                "scale": 1.4,
                "zoom": 5
            },
            {
                "color": "#00000000",
                "scale": 3.05,
                "zoom": 6
            },
            {
                "color": "#00000000",
                "scale": 3.05,
                "zoom": 7
            },
            {
                "color": "#d8e6f3",
                "scale": 3.15,
                "zoom": 8
            },
            {
                "color": "#e0ebf5",
                "scale": 3.37,
                "zoom": 9
            },
            {
                "color": "#e0ebf5",
                "scale": 3.36,
                "zoom": 10
            },
            {
                "color": "#e0ebf5",
                "scale": 3.17,
                "zoom": 11
            },
            {
                "color": "#e0ebf5",
                "scale": 3,
                "zoom": 12
            },
            {
                "color": "#e0ebf5",
                "scale": 2.8,
                "zoom": 13
            },
            {
                "color": "#e8f0f8",
                "scale": 2.66,
                "zoom": 14
            },
            {
                "color": "#e8f0f8",
                "scale": 2.61,
                "zoom": 15
            },
            {
                "color": "#ebf2f9",
                "scale": 2.64,
                "zoom": 16
            },
            {
                "color": "#edf3f9",
                "scale": 2.14,
                "zoom": 17
            },
            {
                "color": "#f0f5fa",
                "scale": 1.79,
                "zoom": 18
            },
            {
                "color": "#f3f7fb",
                "scale": 1.55,
                "zoom": 19
            },
            {
                "color": "#f5f8fc",
                "scale": 1.41,
                "zoom": 20
            },
            {
                "color": "#f7fafd",
                "scale": 1.35,
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "any": "road_2",
            "none": "is_tunnel"
        },
        "elements": "geometry.fill",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "scale": 0,
                "zoom": 0
            },
            {
                "scale": 0,
                "zoom": 1
            },
            {
                "scale": 0,
                "zoom": 2
            },
            {
                "scale": 0,
                "zoom": 3
            },
            {
                "scale": 0,
                "zoom": 4
            },
            {
                "scale": 0,
                "zoom": 5
            },
            {
                "scale": 2.97,
                "zoom": 6
            },
            {
                "scale": 3.19,
                "zoom": 7
            },
            {
                "scale": 3.53,
                "zoom": 8
            },
            {
                "scale": 4,
                "zoom": 9
            },
            {
                "scale": 3.61,
                "zoom": 10
            },
            {
                "scale": 3.06,
                "zoom": 11
            },
            {
                "scale": 2.64,
                "zoom": 12
            },
            {
                "scale": 2.27,
                "zoom": 13
            },
            {
                "scale": 2.03,
                "zoom": 14
            },
            {
                "scale": 1.9,
                "zoom": 15
            },
            {
                "scale": 1.86,
                "zoom": 16
            },
            {
                "scale": 1.48,
                "zoom": 17
            },
            {
                "scale": 1.21,
                "zoom": 18
            },
            {
                "scale": 1.04,
                "zoom": 19
            },
            {
                "scale": 0.94,
                "zoom": 20
            },
            {
                "scale": 0.9,
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "any": "road_2"
        },
        "elements": "geometry.outline",
        "stylers": [
            {
                "color": "#00000000",
                "scale": 1.4,
                "zoom": 0
            },
            {
                "color": "#00000000",
                "scale": 1.4,
                "zoom": 1
            },
            {
                "color": "#00000000",
                "scale": 1.4,
                "zoom": 2
            },
            {
                "color": "#00000000",
                "scale": 1.4,
                "zoom": 3
            },
            {
                "color": "#00000000",
                "scale": 1.4,
                "zoom": 4
            },
            {
                "color": "#00000000",
                "scale": 1.4,
                "zoom": 5
            },
            {
                "color": "#00000000",
                "scale": 3.05,
                "zoom": 6
            },
            {
                "color": "#00000000",
                "scale": 3.05,
                "zoom": 7
            },
            {
                "color": "#d8e6f3",
                "scale": 3.15,
                "zoom": 8
            },
            {
                "color": "#e0ebf5",
                "scale": 3.37,
                "zoom": 9
            },
            {
                "color": "#e0ebf5",
                "scale": 3.36,
                "zoom": 10
            },
            {
                "color": "#e0ebf5",
                "scale": 3.17,
                "zoom": 11
            },
            {
                "color": "#e0ebf5",
                "scale": 3,
                "zoom": 12
            },
            {
                "color": "#e0ebf5",
                "scale": 2.8,
                "zoom": 13
            },
            {
                "color": "#e8f0f8",
                "scale": 2.66,
                "zoom": 14
            },
            {
                "color": "#e8f0f8",
                "scale": 2.61,
                "zoom": 15
            },
            {
                "color": "#ebf2f9",
                "scale": 2.64,
                "zoom": 16
            },
            {
                "color": "#edf3f9",
                "scale": 2.14,
                "zoom": 17
            },
            {
                "color": "#f0f5fa",
                "scale": 1.79,
                "zoom": 18
            },
            {
                "color": "#f3f7fb",
                "scale": 1.55,
                "zoom": 19
            },
            {
                "color": "#f5f8fc",
                "scale": 1.41,
                "zoom": 20
            },
            {
                "color": "#f7fafd",
                "scale": 1.35,
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "any": "road_3",
            "none": "is_tunnel"
        },
        "elements": "geometry.fill",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "scale": 0,
                "zoom": 0
            },
            {
                "scale": 0,
                "zoom": 1
            },
            {
                "scale": 0,
                "zoom": 2
            },
            {
                "scale": 0,
                "zoom": 3
            },
            {
                "scale": 0,
                "zoom": 4
            },
            {
                "scale": 0,
                "zoom": 5
            },
            {
                "scale": 0,
                "zoom": 6
            },
            {
                "scale": 0,
                "zoom": 7
            },
            {
                "scale": 0,
                "zoom": 8
            },
            {
                "scale": 2.51,
                "zoom": 9
            },
            {
                "scale": 2.62,
                "zoom": 10
            },
            {
                "scale": 1.68,
                "zoom": 11
            },
            {
                "scale": 1.67,
                "zoom": 12
            },
            {
                "scale": 1.38,
                "zoom": 13
            },
            {
                "scale": 1.19,
                "zoom": 14
            },
            {
                "scale": 1.08,
                "zoom": 15
            },
            {
                "scale": 1.04,
                "zoom": 16
            },
            {
                "scale": 0.91,
                "zoom": 17
            },
            {
                "scale": 0.84,
                "zoom": 18
            },
            {
                "scale": 0.82,
                "zoom": 19
            },
            {
                "scale": 0.84,
                "zoom": 20
            },
            {
                "scale": 0.9,
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "any": "road_3"
        },
        "elements": "geometry.outline",
        "stylers": [
            {
                "color": "#ffffff",
                "scale": 1.6,
                "zoom": 0
            },
            {
                "color": "#ffffff",
                "scale": 1.6,
                "zoom": 1
            },
            {
                "color": "#ffffff",
                "scale": 1.6,
                "zoom": 2
            },
            {
                "color": "#ffffff",
                "scale": 1.6,
                "zoom": 3
            },
            {
                "color": "#ffffff",
                "scale": 1.6,
                "zoom": 4
            },
            {
                "color": "#ffffff",
                "scale": 1.6,
                "zoom": 5
            },
            {
                "color": "#ffffff",
                "scale": 1.6,
                "zoom": 6
            },
            {
                "color": "#ffffff",
                "scale": 1.6,
                "zoom": 7
            },
            {
                "color": "#ffffff",
                "scale": 1.29,
                "zoom": 8
            },
            {
                "color": "#e0ebf5",
                "scale": 4.21,
                "zoom": 9
            },
            {
                "color": "#e0ebf5",
                "scale": 2.74,
                "zoom": 10
            },
            {
                "color": "#e0ebf5",
                "scale": 2.04,
                "zoom": 11
            },
            {
                "color": "#e0ebf5",
                "scale": 2.13,
                "zoom": 12
            },
            {
                "color": "#e0ebf5",
                "scale": 1.88,
                "zoom": 13
            },
            {
                "color": "#e8f0f8",
                "scale": 1.7,
                "zoom": 14
            },
            {
                "color": "#e8f0f8",
                "scale": 1.59,
                "zoom": 15
            },
            {
                "color": "#ebf2f9",
                "scale": 1.55,
                "zoom": 16
            },
            {
                "color": "#edf3f9",
                "scale": 1.37,
                "zoom": 17
            },
            {
                "color": "#f0f5fa",
                "scale": 1.27,
                "zoom": 18
            },
            {
                "color": "#f3f7fb",
                "scale": 1.23,
                "zoom": 19
            },
            {
                "color": "#f5f8fc",
                "scale": 1.26,
                "zoom": 20
            },
            {
                "color": "#f7fafd",
                "scale": 1.35,
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "any": "road_4",
            "none": "is_tunnel"
        },
        "elements": "geometry.fill",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "scale": 0,
                "zoom": 0
            },
            {
                "scale": 0,
                "zoom": 1
            },
            {
                "scale": 0,
                "zoom": 2
            },
            {
                "scale": 0,
                "zoom": 3
            },
            {
                "scale": 0,
                "zoom": 4
            },
            {
                "scale": 0,
                "zoom": 5
            },
            {
                "scale": 0,
                "zoom": 6
            },
            {
                "scale": 0,
                "zoom": 7
            },
            {
                "scale": 0,
                "zoom": 8
            },
            {
                "scale": 0,
                "zoom": 9
            },
            {
                "scale": 1.69,
                "zoom": 10
            },
            {
                "scale": 1.26,
                "zoom": 11
            },
            {
                "scale": 1.41,
                "zoom": 12
            },
            {
                "scale": 1.19,
                "zoom": 13
            },
            {
                "scale": 1.04,
                "zoom": 14
            },
            {
                "scale": 0.97,
                "zoom": 15
            },
            {
                "scale": 1.15,
                "zoom": 16
            },
            {
                "scale": 0.99,
                "zoom": 17
            },
            {
                "scale": 0.89,
                "zoom": 18
            },
            {
                "scale": 0.85,
                "zoom": 19
            },
            {
                "scale": 0.85,
                "zoom": 20
            },
            {
                "scale": 0.9,
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "any": "road_4"
        },
        "elements": "geometry.outline",
        "stylers": [
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 0
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 1
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 2
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 3
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 4
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 5
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 6
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 7
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 8
            },
            {
                "color": "#ffffff",
                "scale": 1.12,
                "zoom": 9
            },
            {
                "color": "#e0ebf5",
                "scale": 1.9,
                "zoom": 10
            },
            {
                "color": "#e0ebf5",
                "scale": 1.62,
                "zoom": 11
            },
            {
                "color": "#e0ebf5",
                "scale": 1.83,
                "zoom": 12
            },
            {
                "color": "#e0ebf5",
                "scale": 1.64,
                "zoom": 13
            },
            {
                "color": "#e8f0f8",
                "scale": 1.51,
                "zoom": 14
            },
            {
                "color": "#e8f0f8",
                "scale": 1.44,
                "zoom": 15
            },
            {
                "color": "#ebf2f9",
                "scale": 1.69,
                "zoom": 16
            },
            {
                "color": "#edf3f9",
                "scale": 1.47,
                "zoom": 17
            },
            {
                "color": "#f0f5fa",
                "scale": 1.34,
                "zoom": 18
            },
            {
                "color": "#f3f7fb",
                "scale": 1.28,
                "zoom": 19
            },
            {
                "color": "#f5f8fc",
                "scale": 1.28,
                "zoom": 20
            },
            {
                "color": "#f7fafd",
                "scale": 1.34,
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "any": "road_5",
            "none": "is_tunnel"
        },
        "elements": "geometry.fill",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "scale": 0,
                "zoom": 0
            },
            {
                "scale": 0,
                "zoom": 1
            },
            {
                "scale": 0,
                "zoom": 2
            },
            {
                "scale": 0,
                "zoom": 3
            },
            {
                "scale": 0,
                "zoom": 4
            },
            {
                "scale": 0,
                "zoom": 5
            },
            {
                "scale": 0,
                "zoom": 6
            },
            {
                "scale": 0,
                "zoom": 7
            },
            {
                "scale": 0,
                "zoom": 8
            },
            {
                "scale": 0,
                "zoom": 9
            },
            {
                "scale": 0,
                "zoom": 10
            },
            {
                "scale": 0,
                "zoom": 11
            },
            {
                "scale": 1.25,
                "zoom": 12
            },
            {
                "scale": 0.95,
                "zoom": 13
            },
            {
                "scale": 0.81,
                "zoom": 14
            },
            {
                "scale": 0.95,
                "zoom": 15
            },
            {
                "scale": 1.1,
                "zoom": 16
            },
            {
                "scale": 0.93,
                "zoom": 17
            },
            {
                "scale": 0.85,
                "zoom": 18
            },
            {
                "scale": 0.82,
                "zoom": 19
            },
            {
                "scale": 0.84,
                "zoom": 20
            },
            {
                "scale": 0.9,
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "any": "road_5"
        },
        "elements": "geometry.outline",
        "stylers": [
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 0
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 1
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 2
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 3
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 4
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 5
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 6
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 7
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 8
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 9
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 10
            },
            {
                "color": "#ffffff",
                "scale": 0.62,
                "zoom": 11
            },
            {
                "color": "#e0ebf5",
                "scale": 1.61,
                "zoom": 12
            },
            {
                "color": "#e0ebf5",
                "scale": 1.36,
                "zoom": 13
            },
            {
                "color": "#e8f0f8",
                "scale": 1.22,
                "zoom": 14
            },
            {
                "color": "#e8f0f8",
                "scale": 1.41,
                "zoom": 15
            },
            {
                "color": "#ebf2f9",
                "scale": 1.63,
                "zoom": 16
            },
            {
                "color": "#edf3f9",
                "scale": 1.4,
                "zoom": 17
            },
            {
                "color": "#f0f5fa",
                "scale": 1.27,
                "zoom": 18
            },
            {
                "color": "#f3f7fb",
                "scale": 1.23,
                "zoom": 19
            },
            {
                "color": "#f5f8fc",
                "scale": 1.25,
                "zoom": 20
            },
            {
                "color": "#f7fafd",
                "scale": 1.34,
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "any": "road_6",
            "none": "is_tunnel"
        },
        "elements": "geometry.fill",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "scale": 0,
                "zoom": 0
            },
            {
                "scale": 0,
                "zoom": 1
            },
            {
                "scale": 0,
                "zoom": 2
            },
            {
                "scale": 0,
                "zoom": 3
            },
            {
                "scale": 0,
                "zoom": 4
            },
            {
                "scale": 0,
                "zoom": 5
            },
            {
                "scale": 0,
                "zoom": 6
            },
            {
                "scale": 0,
                "zoom": 7
            },
            {
                "scale": 0,
                "zoom": 8
            },
            {
                "scale": 0,
                "zoom": 9
            },
            {
                "scale": 0,
                "zoom": 10
            },
            {
                "scale": 0,
                "zoom": 11
            },
            {
                "scale": 0,
                "zoom": 12
            },
            {
                "scale": 2.25,
                "zoom": 13
            },
            {
                "scale": 1.27,
                "zoom": 14
            },
            {
                "scale": 1.25,
                "zoom": 15
            },
            {
                "scale": 1.31,
                "zoom": 16
            },
            {
                "scale": 1.04,
                "zoom": 17
            },
            {
                "scale": 0.9,
                "zoom": 18
            },
            {
                "scale": 0.85,
                "zoom": 19
            },
            {
                "scale": 0.85,
                "zoom": 20
            },
            {
                "scale": 0.9,
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "any": "road_6"
        },
        "elements": "geometry.outline",
        "stylers": [
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 0
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 1
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 2
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 3
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 4
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 5
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 6
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 7
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 8
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 9
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 10
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 11
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 12
            },
            {
                "color": "#e0ebf5",
                "scale": 2.31,
                "zoom": 13
            },
            {
                "color": "#e8f0f8",
                "scale": 1.7,
                "zoom": 14
            },
            {
                "color": "#e8f0f8",
                "scale": 1.76,
                "zoom": 15
            },
            {
                "color": "#ebf2f9",
                "scale": 1.89,
                "zoom": 16
            },
            {
                "color": "#edf3f9",
                "scale": 1.55,
                "zoom": 17
            },
            {
                "color": "#f0f5fa",
                "scale": 1.36,
                "zoom": 18
            },
            {
                "color": "#f3f7fb",
                "scale": 1.27,
                "zoom": 19
            },
            {
                "color": "#f5f8fc",
                "scale": 1.27,
                "zoom": 20
            },
            {
                "color": "#f7fafd",
                "scale": 1.34,
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "any": "road_7",
            "none": "is_tunnel"
        },
        "elements": "geometry.fill",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "scale": 0,
                "zoom": 0
            },
            {
                "scale": 0,
                "zoom": 1
            },
            {
                "scale": 0,
                "zoom": 2
            },
            {
                "scale": 0,
                "zoom": 3
            },
            {
                "scale": 0,
                "zoom": 4
            },
            {
                "scale": 0,
                "zoom": 5
            },
            {
                "scale": 0,
                "zoom": 6
            },
            {
                "scale": 0,
                "zoom": 7
            },
            {
                "scale": 0,
                "zoom": 8
            },
            {
                "scale": 0,
                "zoom": 9
            },
            {
                "scale": 0,
                "zoom": 10
            },
            {
                "scale": 0,
                "zoom": 11
            },
            {
                "scale": 0,
                "zoom": 12
            },
            {
                "scale": 0,
                "zoom": 13
            },
            {
                "scale": 0.9,
                "zoom": 14
            },
            {
                "scale": 0.78,
                "zoom": 15
            },
            {
                "scale": 0.88,
                "zoom": 16
            },
            {
                "scale": 0.8,
                "zoom": 17
            },
            {
                "scale": 0.78,
                "zoom": 18
            },
            {
                "scale": 0.79,
                "zoom": 19
            },
            {
                "scale": 0.83,
                "zoom": 20
            },
            {
                "scale": 0.9,
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "any": "road_7"
        },
        "elements": "geometry.outline",
        "stylers": [
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 0
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 1
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 2
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 3
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 4
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 5
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 6
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 7
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 8
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 9
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 10
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 11
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 12
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 13
            },
            {
                "color": "#e8f0f8",
                "scale": 1.31,
                "zoom": 14
            },
            {
                "color": "#e8f0f8",
                "scale": 1.19,
                "zoom": 15
            },
            {
                "color": "#ebf2f9",
                "scale": 1.31,
                "zoom": 16
            },
            {
                "color": "#edf3f9",
                "scale": 1.21,
                "zoom": 17
            },
            {
                "color": "#f0f5fa",
                "scale": 1.17,
                "zoom": 18
            },
            {
                "color": "#f3f7fb",
                "scale": 1.18,
                "zoom": 19
            },
            {
                "color": "#f5f8fc",
                "scale": 1.23,
                "zoom": 20
            },
            {
                "color": "#f7fafd",
                "scale": 1.33,
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "any": "road_minor",
            "none": "is_tunnel"
        },
        "elements": "geometry.fill",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "scale": 0,
                "zoom": 0
            },
            {
                "scale": 0,
                "zoom": 1
            },
            {
                "scale": 0,
                "zoom": 2
            },
            {
                "scale": 0,
                "zoom": 3
            },
            {
                "scale": 0,
                "zoom": 4
            },
            {
                "scale": 0,
                "zoom": 5
            },
            {
                "scale": 0,
                "zoom": 6
            },
            {
                "scale": 0,
                "zoom": 7
            },
            {
                "scale": 0,
                "zoom": 8
            },
            {
                "scale": 0,
                "zoom": 9
            },
            {
                "scale": 0,
                "zoom": 10
            },
            {
                "scale": 0,
                "zoom": 11
            },
            {
                "scale": 0,
                "zoom": 12
            },
            {
                "scale": 0,
                "zoom": 13
            },
            {
                "scale": 0,
                "zoom": 14
            },
            {
                "scale": 0,
                "zoom": 15
            },
            {
                "scale": 0.9,
                "zoom": 16
            },
            {
                "scale": 0.9,
                "zoom": 17
            },
            {
                "scale": 0.9,
                "zoom": 18
            },
            {
                "scale": 0.9,
                "zoom": 19
            },
            {
                "scale": 0.9,
                "zoom": 20
            },
            {
                "scale": 0.9,
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "any": "road_minor"
        },
        "elements": "geometry.outline",
        "stylers": [
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 0
            },
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 1
            },
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 2
            },
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 3
            },
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 4
            },
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 5
            },
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 6
            },
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 7
            },
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 8
            },
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 9
            },
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 10
            },
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 11
            },
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 12
            },
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 13
            },
            {
                "color": "#e8f0f8",
                "scale": 0.4,
                "zoom": 14
            },
            {
                "color": "#e8f0f8",
                "scale": 0.4,
                "zoom": 15
            },
            {
                "color": "#ebf2f9",
                "scale": 1.4,
                "zoom": 16
            },
            {
                "color": "#edf3f9",
                "scale": 1.27,
                "zoom": 17
            },
            {
                "color": "#f0f5fa",
                "scale": 1.27,
                "zoom": 18
            },
            {
                "color": "#f3f7fb",
                "scale": 1.29,
                "zoom": 19
            },
            {
                "color": "#f5f8fc",
                "scale": 1.31,
                "zoom": 20
            },
            {
                "color": "#f7fafd",
                "scale": 1.32,
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "any": "road_unclassified",
            "none": "is_tunnel"
        },
        "elements": "geometry.fill",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "scale": 0,
                "zoom": 0
            },
            {
                "scale": 0,
                "zoom": 1
            },
            {
                "scale": 0,
                "zoom": 2
            },
            {
                "scale": 0,
                "zoom": 3
            },
            {
                "scale": 0,
                "zoom": 4
            },
            {
                "scale": 0,
                "zoom": 5
            },
            {
                "scale": 0,
                "zoom": 6
            },
            {
                "scale": 0,
                "zoom": 7
            },
            {
                "scale": 0,
                "zoom": 8
            },
            {
                "scale": 0,
                "zoom": 9
            },
            {
                "scale": 0,
                "zoom": 10
            },
            {
                "scale": 0,
                "zoom": 11
            },
            {
                "scale": 0,
                "zoom": 12
            },
            {
                "scale": 0,
                "zoom": 13
            },
            {
                "scale": 0,
                "zoom": 14
            },
            {
                "scale": 0,
                "zoom": 15
            },
            {
                "scale": 0.9,
                "zoom": 16
            },
            {
                "scale": 0.9,
                "zoom": 17
            },
            {
                "scale": 0.9,
                "zoom": 18
            },
            {
                "scale": 0.9,
                "zoom": 19
            },
            {
                "scale": 0.9,
                "zoom": 20
            },
            {
                "scale": 0.9,
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "any": "road_unclassified"
        },
        "elements": "geometry.outline",
        "stylers": [
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 0
            },
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 1
            },
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 2
            },
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 3
            },
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 4
            },
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 5
            },
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 6
            },
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 7
            },
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 8
            },
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 9
            },
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 10
            },
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 11
            },
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 12
            },
            {
                "color": "#ffffff",
                "scale": 0.4,
                "zoom": 13
            },
            {
                "color": "#e8f0f8",
                "scale": 0.4,
                "zoom": 14
            },
            {
                "color": "#e8f0f8",
                "scale": 0.4,
                "zoom": 15
            },
            {
                "color": "#ebf2f9",
                "scale": 1.4,
                "zoom": 16
            },
            {
                "color": "#edf3f9",
                "scale": 1.27,
                "zoom": 17
            },
            {
                "color": "#f0f5fa",
                "scale": 1.27,
                "zoom": 18
            },
            {
                "color": "#f3f7fb",
                "scale": 1.29,
                "zoom": 19
            },
            {
                "color": "#f5f8fc",
                "scale": 1.31,
                "zoom": 20
            },
            {
                "color": "#f7fafd",
                "scale": 1.32,
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "all": "is_tunnel",
            "none": "path"
        },
        "elements": "geometry.fill",
        "stylers": [
            {
                "color": "#d8e6f3",
                "zoom": 0
            },
            {
                "color": "#d8e6f3",
                "zoom": 1
            },
            {
                "color": "#d8e6f3",
                "zoom": 2
            },
            {
                "color": "#d8e6f3",
                "zoom": 3
            },
            {
                "color": "#d8e6f3",
                "zoom": 4
            },
            {
                "color": "#d8e6f3",
                "zoom": 5
            },
            {
                "color": "#d8e6f3",
                "zoom": 6
            },
            {
                "color": "#d8e6f3",
                "zoom": 7
            },
            {
                "color": "#d8e6f3",
                "zoom": 8
            },
            {
                "color": "#d8e6f3",
                "zoom": 9
            },
            {
                "color": "#d8e6f3",
                "zoom": 10
            },
            {
                "color": "#d8e6f3",
                "zoom": 11
            },
            {
                "color": "#d8e6f3",
                "zoom": 12
            },
            {
                "color": "#d8e6f3",
                "zoom": 13
            },
            {
                "color": "#e0ebf5",
                "zoom": 14
            },
            {
                "color": "#e8f0f8",
                "zoom": 15
            },
            {
                "color": "#e9f1f8",
                "zoom": 16
            },
            {
                "color": "#ebf2f9",
                "zoom": 17
            },
            {
                "color": "#ecf2f9",
                "zoom": 18
            },
            {
                "color": "#edf3f9",
                "zoom": 19
            },
            {
                "color": "#eff4fa",
                "zoom": 20
            },
            {
                "color": "#f0f5fa",
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "all": "path",
            "none": "is_tunnel"
        },
        "elements": "geometry.fill",
        "stylers": [
            {
                "color": "#9fbfdf"
            }
        ]
    },
    {
        "tags": {
            "all": "path",
            "none": "is_tunnel"
        },
        "elements": "geometry.outline",
        "stylers": [
            {
                "opacity": 0.7
            },
            {
                "color": "#e0ebf5",
                "zoom": 0
            },
            {
                "color": "#e0ebf5",
                "zoom": 1
            },
            {
                "color": "#e0ebf5",
                "zoom": 2
            },
            {
                "color": "#e0ebf5",
                "zoom": 3
            },
            {
                "color": "#e0ebf5",
                "zoom": 4
            },
            {
                "color": "#e0ebf5",
                "zoom": 5
            },
            {
                "color": "#e0ebf5",
                "zoom": 6
            },
            {
                "color": "#e0ebf5",
                "zoom": 7
            },
            {
                "color": "#e0ebf5",
                "zoom": 8
            },
            {
                "color": "#e0ebf5",
                "zoom": 9
            },
            {
                "color": "#e0ebf5",
                "zoom": 10
            },
            {
                "color": "#e0ebf5",
                "zoom": 11
            },
            {
                "color": "#e0ebf5",
                "zoom": 12
            },
            {
                "color": "#e0ebf5",
                "zoom": 13
            },
            {
                "color": "#e8f0f7",
                "zoom": 14
            },
            {
                "color": "#f0f5fa",
                "zoom": 15
            },
            {
                "color": "#f1f6fa",
                "zoom": 16
            },
            {
                "color": "#f2f7fb",
                "zoom": 17
            },
            {
                "color": "#f3f7fb",
                "zoom": 18
            },
            {
                "color": "#f5f8fc",
                "zoom": 19
            },
            {
                "color": "#f6f9fc",
                "zoom": 20
            },
            {
                "color": "#f7fafd",
                "zoom": 21
            }
        ]
    },
    {
        "tags": "road_construction",
        "elements": "geometry.fill",
        "stylers": [
            {
                "color": "#ffffff"
            }
        ]
    },
    {
        "tags": "road_construction",
        "elements": "geometry.outline",
        "stylers": [
            {
                "color": "#c6d9ec",
                "zoom": 0
            },
            {
                "color": "#c6d9ec",
                "zoom": 1
            },
            {
                "color": "#c6d9ec",
                "zoom": 2
            },
            {
                "color": "#c6d9ec",
                "zoom": 3
            },
            {
                "color": "#c6d9ec",
                "zoom": 4
            },
            {
                "color": "#c6d9ec",
                "zoom": 5
            },
            {
                "color": "#c6d9ec",
                "zoom": 6
            },
            {
                "color": "#c6d9ec",
                "zoom": 7
            },
            {
                "color": "#c6d9ec",
                "zoom": 8
            },
            {
                "color": "#c6d9ec",
                "zoom": 9
            },
            {
                "color": "#c6d9ec",
                "zoom": 10
            },
            {
                "color": "#c6d9ec",
                "zoom": 11
            },
            {
                "color": "#c6d9ec",
                "zoom": 12
            },
            {
                "color": "#c6d9ec",
                "zoom": 13
            },
            {
                "color": "#9fbfdf",
                "zoom": 14
            },
            {
                "color": "#c6d9ec",
                "zoom": 15
            },
            {
                "color": "#ccddee",
                "zoom": 16
            },
            {
                "color": "#d3e1f0",
                "zoom": 17
            },
            {
                "color": "#d9e5f2",
                "zoom": 18
            },
            {
                "color": "#dfeaf5",
                "zoom": 19
            },
            {
                "color": "#e6eef7",
                "zoom": 20
            },
            {
                "color": "#ecf2f9",
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "any": "ferry"
        },
        "stylers": [
            {
                "color": "#8cb3d9"
            }
        ]
    },
    {
        "tags": "transit_location",
        "elements": "label.icon",
        "stylers": [
            {
                "hue": "#6699cc"
            },
            {
                "saturation": -0.5
            }
        ]
    },
    {
        "tags": "transit_location",
        "elements": "label.text.fill",
        "stylers": [
            {
                "color": "#7a99b8"
            }
        ]
    },
    {
        "tags": "transit_location",
        "elements": "label.text.outline",
        "stylers": [
            {
                "color": "#ffffff"
            }
        ]
    },
    {
        "tags": "transit_schema",
        "elements": "geometry.fill",
        "stylers": [
            {
                "color": "#7a99b8"
            },
            {
                "scale": 0.7
            },
            {
                "opacity": 0.6,
                "zoom": 0
            },
            {
                "opacity": 0.6,
                "zoom": 1
            },
            {
                "opacity": 0.6,
                "zoom": 2
            },
            {
                "opacity": 0.6,
                "zoom": 3
            },
            {
                "opacity": 0.6,
                "zoom": 4
            },
            {
                "opacity": 0.6,
                "zoom": 5
            },
            {
                "opacity": 0.6,
                "zoom": 6
            },
            {
                "opacity": 0.6,
                "zoom": 7
            },
            {
                "opacity": 0.6,
                "zoom": 8
            },
            {
                "opacity": 0.6,
                "zoom": 9
            },
            {
                "opacity": 0.6,
                "zoom": 10
            },
            {
                "opacity": 0.6,
                "zoom": 11
            },
            {
                "opacity": 0.6,
                "zoom": 12
            },
            {
                "opacity": 0.6,
                "zoom": 13
            },
            {
                "opacity": 0.6,
                "zoom": 14
            },
            {
                "opacity": 0.5,
                "zoom": 15
            },
            {
                "opacity": 0.4,
                "zoom": 16
            },
            {
                "opacity": 0.4,
                "zoom": 17
            },
            {
                "opacity": 0.4,
                "zoom": 18
            },
            {
                "opacity": 0.4,
                "zoom": 19
            },
            {
                "opacity": 0.4,
                "zoom": 20
            },
            {
                "opacity": 0.4,
                "zoom": 21
            }
        ]
    },
    {
        "tags": "transit_schema",
        "elements": "geometry.outline",
        "stylers": [
            {
                "opacity": 0
            }
        ]
    },
    {
        "tags": "transit_line",
        "elements": "geometry.fill.pattern",
        "stylers": [
            {
                "color": "#a3b3c2"
            },
            {
                "opacity": 0,
                "zoom": 0
            },
            {
                "opacity": 0,
                "zoom": 1
            },
            {
                "opacity": 0,
                "zoom": 2
            },
            {
                "opacity": 0,
                "zoom": 3
            },
            {
                "opacity": 0,
                "zoom": 4
            },
            {
                "opacity": 0,
                "zoom": 5
            },
            {
                "opacity": 0,
                "zoom": 6
            },
            {
                "opacity": 0,
                "zoom": 7
            },
            {
                "opacity": 0,
                "zoom": 8
            },
            {
                "opacity": 0,
                "zoom": 9
            },
            {
                "opacity": 0,
                "zoom": 10
            },
            {
                "opacity": 0,
                "zoom": 11
            },
            {
                "opacity": 0,
                "zoom": 12
            },
            {
                "opacity": 1,
                "zoom": 13
            },
            {
                "opacity": 1,
                "zoom": 14
            },
            {
                "opacity": 1,
                "zoom": 15
            },
            {
                "opacity": 1,
                "zoom": 16
            },
            {
                "opacity": 1,
                "zoom": 17
            },
            {
                "opacity": 1,
                "zoom": 18
            },
            {
                "opacity": 1,
                "zoom": 19
            },
            {
                "opacity": 1,
                "zoom": 20
            },
            {
                "opacity": 1,
                "zoom": 21
            }
        ]
    },
    {
        "tags": "transit_line",
        "elements": "geometry.fill",
        "stylers": [
            {
                "color": "#a3b3c2"
            },
            {
                "scale": 0.4
            },
            {
                "opacity": 0,
                "zoom": 0
            },
            {
                "opacity": 0,
                "zoom": 1
            },
            {
                "opacity": 0,
                "zoom": 2
            },
            {
                "opacity": 0,
                "zoom": 3
            },
            {
                "opacity": 0,
                "zoom": 4
            },
            {
                "opacity": 0,
                "zoom": 5
            },
            {
                "opacity": 0,
                "zoom": 6
            },
            {
                "opacity": 0,
                "zoom": 7
            },
            {
                "opacity": 0,
                "zoom": 8
            },
            {
                "opacity": 0,
                "zoom": 9
            },
            {
                "opacity": 0,
                "zoom": 10
            },
            {
                "opacity": 0,
                "zoom": 11
            },
            {
                "opacity": 0,
                "zoom": 12
            },
            {
                "opacity": 1,
                "zoom": 13
            },
            {
                "opacity": 1,
                "zoom": 14
            },
            {
                "opacity": 1,
                "zoom": 15
            },
            {
                "opacity": 1,
                "zoom": 16
            },
            {
                "opacity": 1,
                "zoom": 17
            },
            {
                "opacity": 1,
                "zoom": 18
            },
            {
                "opacity": 1,
                "zoom": 19
            },
            {
                "opacity": 1,
                "zoom": 20
            },
            {
                "opacity": 1,
                "zoom": 21
            }
        ]
    },
    {
        "tags": "water",
        "elements": "geometry",
        "stylers": [
            {
                "color": "#b3cce6",
                "zoom": 0
            },
            {
                "color": "#b3cce6",
                "zoom": 1
            },
            {
                "color": "#b3cce6",
                "zoom": 2
            },
            {
                "color": "#b3cce6",
                "zoom": 3
            },
            {
                "color": "#b3cce6",
                "zoom": 4
            },
            {
                "color": "#b3cce6",
                "zoom": 5
            },
            {
                "color": "#b3cce6",
                "zoom": 6
            },
            {
                "color": "#b3cce6",
                "zoom": 7
            },
            {
                "color": "#b5cee7",
                "zoom": 8
            },
            {
                "color": "#b8cfe7",
                "zoom": 9
            },
            {
                "color": "#bad1e8",
                "zoom": 10
            },
            {
                "color": "#bbd2e8",
                "zoom": 11
            },
            {
                "color": "#bdd3e9",
                "zoom": 12
            },
            {
                "color": "#bed4e9",
                "zoom": 13
            },
            {
                "color": "#c0d5ea",
                "zoom": 14
            },
            {
                "color": "#c2d6ea",
                "zoom": 15
            },
            {
                "color": "#c4d8eb",
                "zoom": 16
            },
            {
                "color": "#c5d9eb",
                "zoom": 17
            },
            {
                "color": "#c7daec",
                "zoom": 18
            },
            {
                "color": "#c9dbed",
                "zoom": 19
            },
            {
                "color": "#cbdded",
                "zoom": 20
            },
            {
                "color": "#cddeee",
                "zoom": 21
            }
        ]
    },
    {
        "tags": "water",
        "elements": "geometry",
        "types": "polyline",
        "stylers": [
            {
                "opacity": 0.4,
                "zoom": 0
            },
            {
                "opacity": 0.4,
                "zoom": 1
            },
            {
                "opacity": 0.4,
                "zoom": 2
            },
            {
                "opacity": 0.4,
                "zoom": 3
            },
            {
                "opacity": 0.6,
                "zoom": 4
            },
            {
                "opacity": 0.8,
                "zoom": 5
            },
            {
                "opacity": 1,
                "zoom": 6
            },
            {
                "opacity": 1,
                "zoom": 7
            },
            {
                "opacity": 1,
                "zoom": 8
            },
            {
                "opacity": 1,
                "zoom": 9
            },
            {
                "opacity": 1,
                "zoom": 10
            },
            {
                "opacity": 1,
                "zoom": 11
            },
            {
                "opacity": 1,
                "zoom": 12
            },
            {
                "opacity": 1,
                "zoom": 13
            },
            {
                "opacity": 1,
                "zoom": 14
            },
            {
                "opacity": 1,
                "zoom": 15
            },
            {
                "opacity": 1,
                "zoom": 16
            },
            {
                "opacity": 1,
                "zoom": 17
            },
            {
                "opacity": 1,
                "zoom": 18
            },
            {
                "opacity": 1,
                "zoom": 19
            },
            {
                "opacity": 1,
                "zoom": 20
            },
            {
                "opacity": 1,
                "zoom": 21
            }
        ]
    },
    {
        "tags": "bathymetry",
        "elements": "geometry",
        "stylers": [
            {
                "hue": "#b3cce6"
            }
        ]
    },
    {
        "tags": {
            "any": [
                "industrial",
                "construction_site"
            ]
        },
        "elements": "geometry",
        "stylers": [
            {
                "color": "#d9e6f2",
                "zoom": 0
            },
            {
                "color": "#d9e6f2",
                "zoom": 1
            },
            {
                "color": "#d9e6f2",
                "zoom": 2
            },
            {
                "color": "#d9e6f2",
                "zoom": 3
            },
            {
                "color": "#d9e6f2",
                "zoom": 4
            },
            {
                "color": "#d9e6f2",
                "zoom": 5
            },
            {
                "color": "#d9e6f2",
                "zoom": 6
            },
            {
                "color": "#d9e6f2",
                "zoom": 7
            },
            {
                "color": "#d9e6f2",
                "zoom": 8
            },
            {
                "color": "#d9e6f2",
                "zoom": 9
            },
            {
                "color": "#d9e6f2",
                "zoom": 10
            },
            {
                "color": "#d9e6f2",
                "zoom": 11
            },
            {
                "color": "#d9e6f2",
                "zoom": 12
            },
            {
                "color": "#d9e6f2",
                "zoom": 13
            },
            {
                "color": "#e0ebf4",
                "zoom": 14
            },
            {
                "color": "#e8f0f7",
                "zoom": 15
            },
            {
                "color": "#e9f1f7",
                "zoom": 16
            },
            {
                "color": "#ebf2f8",
                "zoom": 17
            },
            {
                "color": "#ecf2f8",
                "zoom": 18
            },
            {
                "color": "#edf3f9",
                "zoom": 19
            },
            {
                "color": "#eff4f9",
                "zoom": 20
            },
            {
                "color": "#f0f5fa",
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "any": "transit",
            "none": [
                "transit_location",
                "transit_line",
                "transit_schema",
                "is_unclassified_transit"
            ]
        },
        "elements": "geometry",
        "stylers": [
            {
                "color": "#d9e6f2",
                "zoom": 0
            },
            {
                "color": "#d9e6f2",
                "zoom": 1
            },
            {
                "color": "#d9e6f2",
                "zoom": 2
            },
            {
                "color": "#d9e6f2",
                "zoom": 3
            },
            {
                "color": "#d9e6f2",
                "zoom": 4
            },
            {
                "color": "#d9e6f2",
                "zoom": 5
            },
            {
                "color": "#d9e6f2",
                "zoom": 6
            },
            {
                "color": "#d9e6f2",
                "zoom": 7
            },
            {
                "color": "#d9e6f2",
                "zoom": 8
            },
            {
                "color": "#d9e6f2",
                "zoom": 9
            },
            {
                "color": "#d9e6f2",
                "zoom": 10
            },
            {
                "color": "#d9e6f2",
                "zoom": 11
            },
            {
                "color": "#d9e6f2",
                "zoom": 12
            },
            {
                "color": "#d9e6f2",
                "zoom": 13
            },
            {
                "color": "#e0ebf4",
                "zoom": 14
            },
            {
                "color": "#e8f0f7",
                "zoom": 15
            },
            {
                "color": "#e9f1f7",
                "zoom": 16
            },
            {
                "color": "#ebf2f8",
                "zoom": 17
            },
            {
                "color": "#ecf2f8",
                "zoom": 18
            },
            {
                "color": "#edf3f9",
                "zoom": 19
            },
            {
                "color": "#eff4f9",
                "zoom": 20
            },
            {
                "color": "#f0f5fa",
                "zoom": 21
            }
        ]
    },
    {
        "tags": "fence",
        "elements": "geometry.fill",
        "stylers": [
            {
                "color": "#cadced"
            },
            {
                "opacity": 0.75,
                "zoom": 0
            },
            {
                "opacity": 0.75,
                "zoom": 1
            },
            {
                "opacity": 0.75,
                "zoom": 2
            },
            {
                "opacity": 0.75,
                "zoom": 3
            },
            {
                "opacity": 0.75,
                "zoom": 4
            },
            {
                "opacity": 0.75,
                "zoom": 5
            },
            {
                "opacity": 0.75,
                "zoom": 6
            },
            {
                "opacity": 0.75,
                "zoom": 7
            },
            {
                "opacity": 0.75,
                "zoom": 8
            },
            {
                "opacity": 0.75,
                "zoom": 9
            },
            {
                "opacity": 0.75,
                "zoom": 10
            },
            {
                "opacity": 0.75,
                "zoom": 11
            },
            {
                "opacity": 0.75,
                "zoom": 12
            },
            {
                "opacity": 0.75,
                "zoom": 13
            },
            {
                "opacity": 0.75,
                "zoom": 14
            },
            {
                "opacity": 0.75,
                "zoom": 15
            },
            {
                "opacity": 0.75,
                "zoom": 16
            },
            {
                "opacity": 0.45,
                "zoom": 17
            },
            {
                "opacity": 0.45,
                "zoom": 18
            },
            {
                "opacity": 0.45,
                "zoom": 19
            },
            {
                "opacity": 0.45,
                "zoom": 20
            },
            {
                "opacity": 0.45,
                "zoom": 21
            }
        ]
    },
    {
        "tags": "medical",
        "elements": "geometry",
        "stylers": [
            {
                "color": "#d9e6f2",
                "zoom": 0
            },
            {
                "color": "#d9e6f2",
                "zoom": 1
            },
            {
                "color": "#d9e6f2",
                "zoom": 2
            },
            {
                "color": "#d9e6f2",
                "zoom": 3
            },
            {
                "color": "#d9e6f2",
                "zoom": 4
            },
            {
                "color": "#d9e6f2",
                "zoom": 5
            },
            {
                "color": "#d9e6f2",
                "zoom": 6
            },
            {
                "color": "#d9e6f2",
                "zoom": 7
            },
            {
                "color": "#d9e6f2",
                "zoom": 8
            },
            {
                "color": "#d9e6f2",
                "zoom": 9
            },
            {
                "color": "#d9e6f2",
                "zoom": 10
            },
            {
                "color": "#d9e6f2",
                "zoom": 11
            },
            {
                "color": "#d9e6f2",
                "zoom": 12
            },
            {
                "color": "#d9e6f2",
                "zoom": 13
            },
            {
                "color": "#e0ebf4",
                "zoom": 14
            },
            {
                "color": "#e8f0f7",
                "zoom": 15
            },
            {
                "color": "#e9f1f7",
                "zoom": 16
            },
            {
                "color": "#ebf2f8",
                "zoom": 17
            },
            {
                "color": "#ecf2f8",
                "zoom": 18
            },
            {
                "color": "#edf3f9",
                "zoom": 19
            },
            {
                "color": "#eff4f9",
                "zoom": 20
            },
            {
                "color": "#f0f5fa",
                "zoom": 21
            }
        ]
    },
    {
        "tags": "beach",
        "elements": "geometry",
        "stylers": [
            {
                "color": "#d9e6f2",
                "opacity": 0.3,
                "zoom": 0
            },
            {
                "color": "#d9e6f2",
                "opacity": 0.3,
                "zoom": 1
            },
            {
                "color": "#d9e6f2",
                "opacity": 0.3,
                "zoom": 2
            },
            {
                "color": "#d9e6f2",
                "opacity": 0.3,
                "zoom": 3
            },
            {
                "color": "#d9e6f2",
                "opacity": 0.3,
                "zoom": 4
            },
            {
                "color": "#d9e6f2",
                "opacity": 0.3,
                "zoom": 5
            },
            {
                "color": "#d9e6f2",
                "opacity": 0.3,
                "zoom": 6
            },
            {
                "color": "#d9e6f2",
                "opacity": 0.3,
                "zoom": 7
            },
            {
                "color": "#d9e6f2",
                "opacity": 0.3,
                "zoom": 8
            },
            {
                "color": "#d9e6f2",
                "opacity": 0.3,
                "zoom": 9
            },
            {
                "color": "#d9e6f2",
                "opacity": 0.3,
                "zoom": 10
            },
            {
                "color": "#d9e6f2",
                "opacity": 0.3,
                "zoom": 11
            },
            {
                "color": "#d9e6f2",
                "opacity": 0.3,
                "zoom": 12
            },
            {
                "color": "#d9e6f2",
                "opacity": 0.65,
                "zoom": 13
            },
            {
                "color": "#e0ebf4",
                "opacity": 1,
                "zoom": 14
            },
            {
                "color": "#e8f0f7",
                "opacity": 1,
                "zoom": 15
            },
            {
                "color": "#e9f1f7",
                "opacity": 1,
                "zoom": 16
            },
            {
                "color": "#ebf2f8",
                "opacity": 1,
                "zoom": 17
            },
            {
                "color": "#ecf2f8",
                "opacity": 1,
                "zoom": 18
            },
            {
                "color": "#edf3f9",
                "opacity": 1,
                "zoom": 19
            },
            {
                "color": "#eff4f9",
                "opacity": 1,
                "zoom": 20
            },
            {
                "color": "#f0f5fa",
                "opacity": 1,
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "all": [
                "is_tunnel",
                "path"
            ]
        },
        "elements": "geometry.fill",
        "stylers": [
            {
                "color": "#98badd"
            },
            {
                "opacity": 0.3
            }
        ]
    },
    {
        "tags": {
            "all": [
                "is_tunnel",
                "path"
            ]
        },
        "elements": "geometry.outline",
        "stylers": [
            {
                "opacity": 0
            }
        ]
    },
    {
        "tags": "road_limited",
        "elements": "geometry.fill",
        "stylers": [
            {
                "color": "#b3cce6"
            },
            {
                "scale": 0,
                "zoom": 0
            },
            {
                "scale": 0,
                "zoom": 1
            },
            {
                "scale": 0,
                "zoom": 2
            },
            {
                "scale": 0,
                "zoom": 3
            },
            {
                "scale": 0,
                "zoom": 4
            },
            {
                "scale": 0,
                "zoom": 5
            },
            {
                "scale": 0,
                "zoom": 6
            },
            {
                "scale": 0,
                "zoom": 7
            },
            {
                "scale": 0,
                "zoom": 8
            },
            {
                "scale": 0,
                "zoom": 9
            },
            {
                "scale": 0,
                "zoom": 10
            },
            {
                "scale": 0,
                "zoom": 11
            },
            {
                "scale": 0,
                "zoom": 12
            },
            {
                "scale": 0.1,
                "zoom": 13
            },
            {
                "scale": 0.2,
                "zoom": 14
            },
            {
                "scale": 0.3,
                "zoom": 15
            },
            {
                "scale": 0.5,
                "zoom": 16
            },
            {
                "scale": 0.6,
                "zoom": 17
            },
            {
                "scale": 0.7,
                "zoom": 18
            },
            {
                "scale": 0.79,
                "zoom": 19
            },
            {
                "scale": 0.83,
                "zoom": 20
            },
            {
                "scale": 0.9,
                "zoom": 21
            }
        ]
    },
    {
        "tags": "road_limited",
        "elements": "geometry.outline",
        "stylers": [
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 0
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 1
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 2
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 3
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 4
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 5
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 6
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 7
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 8
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 9
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 10
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 11
            },
            {
                "color": "#ffffff",
                "scale": 1.4,
                "zoom": 12
            },
            {
                "color": "#ffffff",
                "scale": 0.1,
                "zoom": 13
            },
            {
                "color": "#e8f0f8",
                "scale": 0.2,
                "zoom": 14
            },
            {
                "color": "#e8f0f8",
                "scale": 0.3,
                "zoom": 15
            },
            {
                "color": "#ebf2f9",
                "scale": 0.5,
                "zoom": 16
            },
            {
                "color": "#edf3f9",
                "scale": 0.6,
                "zoom": 17
            },
            {
                "color": "#f0f5fa",
                "scale": 0.7,
                "zoom": 18
            },
            {
                "color": "#f3f7fb",
                "scale": 1.18,
                "zoom": 19
            },
            {
                "color": "#f5f8fc",
                "scale": 1.23,
                "zoom": 20
            },
            {
                "color": "#f7fafd",
                "scale": 1.33,
                "zoom": 21
            }
        ]
    },
    {
        "tags": {
            "any": [
                "address",
                "road_7",
                "road_limited",
                "road_unclassified",
                "road_minor",
                "road_construction",
                "path"
            ]
        },
        "elements": "label",
        "stylers": {
            "visibility": "off"
        }
    },
    {
        "tags": {
            "any": "landcover",
            "none": "vegetation"
        },
        "stylers": {
            "visibility": "off"
        }
    }
]""")
            }

        }, modifier = Modifier) {

        }

        LeftMenu(
            modifier = Modifier
                .padding(
                    top = 16.dp + WindowInsets.statusBarsIgnoringVisibility
                        .asPaddingValues()
                        .calculateTopPadding(),
                    start = 16.dp
                )
                .align(Alignment.TopStart),
            sheetState = mainState.sheetState!!
        ) {
            mainViewModel.updatePage("profile")
            scope.launch {
                mainState.sheetState!!.show()
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End
        ) {
            BalanceMenu(
                modifier = Modifier
                    .padding(
                        top = 16.dp + WindowInsets.statusBarsIgnoringVisibility.asPaddingValues().calculateTopPadding(),
                        end = 16.dp
                    ),
                mainViewModel = mainViewModel
            )
            TimerBox(
                modifier = Modifier
                    .padding(end = 16.dp),
                mainViewModel = mainViewModel
            )
        }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterEnd)
                .then(
                    if (mainState.sheetState!!.targetValue == ModalBottomSheetValue.Expanded)
                        Modifier.alpha(0f)
                    else
                        Modifier
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            IconButton(
                modifier = Modifier,
                onClick = {
                    mainState.mapView!!.mapWindow.map.move(CameraPosition(Point(mainState.currentLocation.latitude, mainState.currentLocation.longitude), 13f, 0f, 0f), Animation(Animation.Type.SMOOTH, 0.5f))
                    { }
                }
            ) {
                Image(imageVector = ImageVector.vectorResource(id = R.drawable.usercursor), contentDescription = null)
            }
            IconButton(
                modifier = Modifier,
                onClick = {
                    mainViewModel.updatePage("filter")
                    scope.launch {
                        mainState.sheetState!!.show()
                    }
                }
            ) {
                Image(imageVector = ImageVector.vectorResource(id = R.drawable.filter), contentDescription = null)
            }
        }
    }

    val bottomSheetPages: Map<String, @Composable () -> Unit> = mapOf(
        "filter" to { FilterCarsContent(
            mainViewModel = mainViewModel
        )},
        "profile" to { ProfileContent(
            mainViewModel = mainViewModel,
            navigation = navigation
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
