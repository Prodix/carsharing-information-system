package com.syndicate.carsharing.views

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.SelectableChipColors
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
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
import com.syndicate.carsharing.components.FilterCarsContent
import com.syndicate.carsharing.components.LeftMenu
import com.syndicate.carsharing.components.MainMenuContent
import com.syndicate.carsharing.components.ProfileContent
import com.syndicate.carsharing.components.RadarContent
import com.syndicate.carsharing.components.RadarFindingContent
import com.syndicate.carsharing.components.RateContent
import com.syndicate.carsharing.components.UserCursorButton
import com.syndicate.carsharing.models.MainModel
import com.syndicate.carsharing.data.Tag
import com.syndicate.carsharing.modifiers.withShadow
import com.syndicate.carsharing.utility.Shadow
import com.syndicate.carsharing.viewmodels.MainViewModel
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.GeoObjectTapListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CircleMapObject
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.reflect.typeOf
import kotlin.system.exitProcess



@SuppressLint("UnrememberedMutableInteractionSource")
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Main(
    navigation: NavHostController,
    mainViewModel: MainViewModel = viewModel()
) {
    lateinit var map: MapView
    lateinit var userPlacemark: PlacemarkMapObject
    val mainState by mainViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val location = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmValueChange = {
            if (it == ModalBottomSheetValue.Hidden) {
                if (mainViewModel.circle.value != null)
                    map.mapWindow.map.mapObjects.remove(mainViewModel.circle.value!!)
                mainViewModel.updateCircle(null)
            }

            true
        },
        skipHalfExpanded = true
    )
    val listener: MapObjectTapListener = MapObjectTapListener { _, point: Point ->
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

            val testPlacemark = map.mapWindow.map.mapObjects.addPlacemark()
            testPlacemark.setIcon(ImageProvider.fromResource(context, R.drawable.carpoint))
            testPlacemark.geometry = Point(37.3935, -122.0478)
            testPlacemark.addTapListener(listener)

            userPlacemark = map.mapWindow.map.mapObjects.addPlacemark()
            userPlacemark.setIcon(ImageProvider.fromResource(context, R.drawable.userpoint))
            userPlacemark.geometry = mainViewModel.currentLocation.value

            if (mainViewModel.currentLocation.value.latitude == 0.0 && mainViewModel.currentLocation.value.longitude == 0.0) {
                userPlacemark.isVisible = false
            }
            else {
                map.setNoninteractive(true)
                map.mapWindow.map.move(CameraPosition(mainViewModel.currentLocation.value, 13f, 0f, 0f), Animation(Animation.Type.SMOOTH, 0.5f))
                { map.setNoninteractive(false) }
            }

            location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f) {
                mainViewModel.updateLocation(Point(it.latitude, it.longitude))
                userPlacemark.geometry = mainViewModel.currentLocation.value

                if (!sheetState.isVisible)
                    mainViewModel.circle.value?.geometry = Circle(mainViewModel.currentLocation.value, 400f * mainViewModel.walkMinutes.value)

                if (!userPlacemark.isVisible) {
                    userPlacemark.isVisible = true
                    map.setNoninteractive(true)
                    map.mapWindow.map.move(CameraPosition(mainViewModel.currentLocation.value, 13f, 0f, 0f), Animation(Animation.Type.SMOOTH, 0.5f))
                    { map.setNoninteractive(false) }
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
                map = this
                MapKitFactory.initialize(it)
                MapKitFactory.getInstance().onStart()
                this.onStart()
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
                    map.mapWindow.map.move(CameraPosition(Point(mainViewModel.currentLocation.value.latitude, mainViewModel.currentLocation.value.longitude), 13f, 0f, 0f), Animation(Animation.Type.SMOOTH, 0.5f))
                    { }
                    mainViewModel.updateCircle(map.mapWindow.map.mapObjects.addCircle(Circle(mainViewModel.currentLocation.value, 400f * mainViewModel.walkMinutes.value)))
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

        BalanceMenu(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd),
            sheetState = sheetState
        )

        UserCursorButton(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterEnd),
            sheetState = sheetState
        ) {
            map.mapWindow.map.move(CameraPosition(Point(mainViewModel.currentLocation.value.latitude, mainViewModel.currentLocation.value.longitude), 13f, 0f, 0f), Animation(Animation.Type.SMOOTH, 0.5f))
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
        )}
    )

    BottomSheetWithPages(
        sheetState = sheetState,
        sheetComposableList = bottomSheetPages,
        mainViewModel = mainViewModel
    )
}