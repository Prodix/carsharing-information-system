package com.syndicate.carsharing.views

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Environment
import android.os.Looper
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
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.syndicate.carsharing.MainActivity
import com.syndicate.carsharing.R
import com.syndicate.carsharing.database.HttpClient
import com.syndicate.carsharing.database.models.DefaultResponse
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
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.http.HttpMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.lang.Exception
import java.util.concurrent.Executors
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


    val locationListener = LocationListener {
        scope.launch {
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
                            MainActivity.fusedLocationClient.requestLocationUpdates(LocationRequest.Builder(10)
                                .setIntervalMillis(100)
                                .setMaxUpdateDelayMillis(10)
                                .setPriority(Priority.PRIORITY_HIGH_ACCURACY).build(), Executors.newSingleThreadExecutor(), locationListener)

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
        mainState.mapView!!.mapWindow.map.mapObjects.clear()
        mainViewModel.updateTransport(listOf())
        mainViewModel.updatePlacemarks(listOf())
        MainActivity.fusedLocationClient.removeLocationUpdates(locationListener)
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
        val response = HttpClient.client.request(
            "${HttpClient.url}/account/message/get?id=${mainViewModel.userStore.getUser().first().id}"
        ) {
            method = HttpMethod.Get
        }.body<DefaultResponse>()

        if (response.status_code == 200) {
            AlertDialog.Builder(context)
                .setMessage(response.message)
                .setPositiveButton("ok") { _, _ -> run { } }
                .show()
        }
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
        AndroidView(
            factory = { mainState.mapView!! },
            modifier = Modifier
        ) {

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
